package org.koekepan.Minecraft.behaviours.server;

import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerChunkDataPacket;
import com.github.steveice10.packetlib.packet.Packet;
import org.koekepan.Minecraft.ChunkPosition;
import org.koekepan.VAST.Connection.ClientConnectedInstance;
import org.koekepan.VAST.Packet.Behaviour;
import org.koekepan.VAST.Packet.PacketWrapper;

public class ServerChunkDataPacketBehaviour implements Behaviour<Packet> {
    private ClientConnectedInstance clientInstance;
//    private IServerSession serverSession;

    public ServerChunkDataPacketBehaviour(ClientConnectedInstance clientInstance) {
        this.clientInstance = clientInstance;
    }

    public void process(final Packet packet) {
//        clientInstance.sendPacketToClient(packet);
        PacketWrapper.setProcessed(packet, true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                ServerChunkDataPacket serverChunkDataPacket = (ServerChunkDataPacket) packet;

                int chunkX = serverChunkDataPacket.getColumn().getX();
                int chunkZ = serverChunkDataPacket.getColumn().getZ();

// Convert chunk coordinates to block coordinates
                int blockX = chunkX * 16;
                int blockZ = chunkZ * 16;

// Calculate the block coordinates of the four corners
                int x1 = blockX;
                int z1 = blockZ;
//                Logger.log(this, Logger.Level.DEBUG, new String[]{"behaviour","network"},"Packet received for chunk with x,y: (" + x1 + ", " + z1 + ")");
                System.out.println("Received chunk packet for chunk with x,y: (" + x1 + ", " + z1 + ")");


                clientInstance.receiveChunkPosition(new ChunkPosition(x1, z1));
                clientInstance.updateIsolatedPositions();
//                .subscribepolygon(new Arraylist<>(isolatedPositions))


            }
        }).start();
    }

}
