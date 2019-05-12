package packets;

import game.Game;
import game.data.Coordinate2D;
import game.data.Coordinate3D;
import game.data.chunk.Chunk;
import game.data.chunk.ChunkFactory;

public class ClientBoundGamePacketBuilder extends PacketBuilder {
    private final int CHUNK_DATA = 0x20;

    private final int PLAYER_POSITION = 0x2F;

    @Override
    public boolean build(int size) {
        DataTypeProvider typeProvider = getReader().withSize(size);
        int packetId = typeProvider.readVarInt();

        switch (packetId) {
            case CHUNK_DATA:
                try {
                    ChunkFactory.addChunk(typeProvider);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                break;
            case PLAYER_POSITION:
                double x = typeProvider.readDouble();
                double y = typeProvider.readDouble();
                double z = typeProvider.readDouble();
                Game.setPlayerPosition(new Coordinate3D(x, y, z).offset());

                break;
        }

        return true;
    }
}
