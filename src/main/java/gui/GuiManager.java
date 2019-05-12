package gui;


import game.Game;
import game.data.Coordinate2D;
import game.data.Coordinate3D;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class GuiManager {
    public static int WIDTH = 400;
    public static int HEIGHT = 400;

    private static GraphicsHandler graphicsHandler;

    public static void showGui() {
        SwingUtilities.invokeLater(GuiManager::createAndShowGUI);
    }

    public static void setChunkLoaded(Coordinate2D coord) {
        if (graphicsHandler != null) {
            graphicsHandler.setChunkLoaded(coord);
        }
    }

    private static void createAndShowGUI() {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(WIDTH, HEIGHT);

        graphicsHandler = new GraphicsHandler();
        f.add(graphicsHandler);
        f.pack();
        f.setVisible(true);

    }

    public static void setChunksSaved(List<Coordinate2D> saved) {
        if (graphicsHandler != null) {
            graphicsHandler.setChunksSaved(saved);
        }
    }
}

class GraphicsHandler extends JPanel implements ActionListener {
    Graphics g;

    private final int RENDER_RANGE = 200;
    private int minX, maxX, minZ, maxZ, gridSize;
    private HashMap<Coordinate2D, Boolean> chunkMap = new HashMap<>();
    private boolean boundsChanged = false;

    Timer timer;

    public GraphicsHandler() {
        timer = new Timer(1000, this);
        timer.start();
    }

    public synchronized void setChunkLoaded(Coordinate2D coord) {
        if (chunkMap.isEmpty()) {
            initMinMax(coord);
        }

        if (!chunkMap.containsKey(coord)) {
            chunkMap.put(coord, false);
            updateMinMax(coord);
            computeBounds();
        }
    }

    private void initMinMax(Coordinate2D coord) {
        maxX = coord.getX();
        minX = coord.getX();
        maxZ = coord.getZ();
        minZ = coord.getZ();
    }

    private void updateMinMax(Coordinate2D coord) {
        if (coord.getX() > maxX) {
            maxX = coord.getX();
        }
        if (coord.getX() < minX) {
            minX = coord.getX();
        }
        if (coord.getZ() > maxZ) {
            maxZ = coord.getZ();
        }
        if (coord.getZ() < minZ) {
            minZ = coord.getZ();
        }
    }

    private void computeBounds() {
        boundsChanged = true;

        Coordinate2D playerChunk = Game.getPlayerPosition().chunkPos();
        chunkMap.keySet().removeIf(el -> !playerChunk.isInRange(el, RENDER_RANGE));

        int gridWidth = GuiManager.WIDTH / (Math.abs(maxX - minX) + 1);
        int gridHeight = GuiManager.HEIGHT / (Math.abs(maxZ - minZ) + 1);
        gridSize = Math.min(gridWidth, gridHeight);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(GuiManager.WIDTH, GuiManager.HEIGHT);
    }

    @Override
    protected synchronized void paintComponent(Graphics g) {
        this.g = g;
        super.paintComponent(g);

        g.clearRect(0, 0, GuiManager.WIDTH, GuiManager.HEIGHT);
        for (Map.Entry<Coordinate2D, Boolean> e : chunkMap.entrySet()) {
            g.setColor(e.getValue() ? Color.green : Color.BLUE);
            g.fillRect((e.getKey().getX() - minX) * gridSize, (e.getKey().getZ() - minZ) * gridSize, gridSize, gridSize);
        }

        if (Game.getPlayerPosition() != null) {
            g.setColor(Color.RED);
            Coordinate3D playerPosition = Game.getPlayerPosition();


            double playerX = ((playerPosition.getX() / 16.0 - minX) * maxX) * GuiManager.HEIGHT;
            double playerZ = ((playerPosition.getZ() / 16.0 - minZ) * maxZ) * GuiManager.WIDTH;

            System.out.println("[" + minX + ", " + maxX + "] :: " + playerPosition.getX() + " == " + playerX);
            System.out.println("[" + minZ + ", " + maxZ + "] :: " + playerPosition.getZ() + " == " + playerZ);
            g.fillOval((int) playerX, (int) playerZ, 10, 10);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (boundsChanged) {
            repaint();
            boundsChanged = false;
        }
    }

    public synchronized void setChunksSaved(List<Coordinate2D> saved) {
        for (Coordinate2D coordinate : saved) {
            chunkMap.put(coordinate, true);
            updateMinMax(coordinate);
        }
        computeBounds();
    }
}