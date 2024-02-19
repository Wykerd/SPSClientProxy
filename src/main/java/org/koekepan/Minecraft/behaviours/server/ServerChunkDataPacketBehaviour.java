package org.koekepan.herobrineproxy.packet.behaviours.server;

import com.github.steveice10.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerChunkDataPacket;
import com.github.steveice10.packetlib.packet.Packet;
import org.koekepan.herobrineproxy.ConsoleIO;
import org.koekepan.herobrineproxy.Logger;
import org.koekepan.herobrineproxy.behaviour.Behaviour;
import org.koekepan.herobrineproxy.session.*;

public class ServerChunkDataPacketBehaviour implements Behaviour<Packet> {
    private IProxySessionNew proxySession;
    private IServerSession serverSession;

    public ServerChunkDataPacketBehaviour(IProxySessionNew proxySession) {
        this.proxySession = proxySession;
    }

    public void process(final Packet packet) {
        proxySession.sendPacketToClient(packet);

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
                Logger.log(this, Logger.Level.DEBUG, new String[]{"behaviour","network"},"Packet received for chunk with x,y: (" + x1 + ", " + z1 + ")");
                System.out.println("Received chunk packet for chunk with x,y: (" + x1 + ", " + z1 + ")");

                final ClientProxySession clientProxySession = (ClientProxySession) proxySession;
                clientProxySession.receiveChunkPosition(new ChunkPosition(x1, z1));

                clientProxySession.updateIsolatedPositions();
            }
        }).start();



//        clientProxySession.updateIsolatedPositions();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                clientProxySession.updateIsolatedPositions();
//            }
//        }).start();




//        serverJoinPacket.getEntityId();
        //proxySession.setJoined(true);
//        ConsoleIO.println("player \""+proxySession.getUsername()+"\" with entityID <"+serverJoinPacket.getEntityId()+"> has successfully joined world");
//        serverSession.setJoined(true);

        //proxySession.registerForPluginChannels();
        //proxySession.getJoinedCountDownLatch().countDown();
        //MinecraftProtocol protocol = (MinecraftProtocol)(proxySession.getServer().getPacketProtocol());
        //ConsoleIO.println("ServerJoinGamePacketBehaviour::process => Protocol status <"+protocol.getSubProtocol().name()+">");
    }

}
