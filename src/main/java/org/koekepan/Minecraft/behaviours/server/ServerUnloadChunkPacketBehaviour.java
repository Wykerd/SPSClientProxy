package org.koekepan.Minecraft.behaviours.server;

import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerUnloadChunkPacket;
import com.github.steveice10.packetlib.packet.Packet;
import org.koekepan.Minecraft.SubscriptionAreaManager;
import org.koekepan.VAST.Connection.ClientConnectedInstance;
import org.koekepan.VAST.Packet.Behaviour;
import org.koekepan.VAST.Packet.PacketWrapper;

public class ServerUnloadChunkPacketBehaviour implements Behaviour<Packet> {
    private ClientConnectedInstance clientInstance;
//    private IServerSession serverSession;
    public ServerUnloadChunkPacketBehaviour(ClientConnectedInstance clientInstance) {
        this.clientInstance = clientInstance;
    }

    public void process(final Packet packet) {
//        clientInstance.sendPacketToClient(packet);
        PacketWrapper.setProcessed(packet, true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                ServerUnloadChunkPacket serverUnloadChunkPacket = (ServerUnloadChunkPacket) packet;

                int chunkX = serverUnloadChunkPacket.getX();
                int chunkZ = serverUnloadChunkPacket.getZ();

// Convert chunk coordinates to block coordinates
                int blockX = chunkX * 16;
                int blockZ = chunkZ * 16;

// Calculate the block coordinates of the four corners
                int x1 = blockX;
                int z1 = blockZ;
//        System.out.println("Packet received for unload-chunk with x,y: (" + x1 + ", " + z1 + ")");

//                final ClientProxySession clientProxySession = (ClientProxySession) clientInstance;
                SubscriptionAreaManager.removeChunkPosition(clientInstance, x1, z1);
            }
        }).start();


    }

}