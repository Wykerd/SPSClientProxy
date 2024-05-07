package org.koekepan.Minecraft.behaviours.server.entity;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnPlayerPacket;
import com.github.steveice10.packetlib.packet.Packet;
import org.koekepan.VAST.Connection.ClientConnectedInstance;
import org.koekepan.VAST.Packet.Behaviour;
import org.koekepan.VAST.Packet.PacketWrapper;

public class ServerSpawnPlayerPacketBehaviour implements Behaviour<Packet> {
    private ClientConnectedInstance clientInstance;
//    private IServerSession serverSession;

    private ServerSpawnPlayerPacketBehaviour() {
    }

    public ServerSpawnPlayerPacketBehaviour(ClientConnectedInstance clientInstance) {
        this.clientInstance = clientInstance;
//        this.serverSession = serverSession;
    }

    @Override
    public void process(Packet packet) {
        ServerSpawnPlayerPacket serverSpawnPlayerPacketBehaviour = (ServerSpawnPlayerPacket) packet;

        if (serverSpawnPlayerPacketBehaviour.getEntityId() != clientInstance.getEntityID()) { // Spawn other players
            // Basically, add to back of sending queue but mark as processed so that this does not become infinite loop
            // This will ensure that this packet is only sent to client _after_ the client has received the list entry of the spawned player

            PacketWrapper packetWrapper = PacketWrapper.getPacketWrapper(packet);
            clientInstance.getPacketSender().removePacket(packet);


            try {
                Thread.sleep(250);
            } catch (Exception e) {
                System.out.println("ServerSpawnPlayerPacketBehaviour for packet: <" + packet.getClass().getSimpleName() + "> failed setting processed: <" + e.getMessage() + ">");
            }

            PacketWrapper.packetWrapperMap.put(packet, packetWrapper);
            clientInstance.getPacketSender().addClientboundPacket(packet);
//            System.out.println("<" + clientInstance.getUsername() + "> ServerSpawnPlayerPacket set to processed != " + serverSpawnPlayerPacketBehaviour.getEntityId());
            PacketWrapper.setProcessed(packet, true);
        } else {
            PacketWrapper packetWrapper = PacketWrapper.getPacketWrapper(packet);
            if (packetWrapper.getPlayerSpecific() != null) {
                if (packetWrapper.getPlayerSpecific().equals(clientInstance.getUsername())) {
//                    System.out.println("<" + clientInstance.getUsername() + "> ServerSpawnPlayerPacket set to processed (player specific) " + serverSpawnPlayerPacketBehaviour.getEntityId());
                    PacketWrapper.setProcessed(packet, true);
//                    clientInstance.sendPacketToClient(packet);
                } else {
//                    SPSConnection.playerSpecificPacketMap.remove(packet);
//                    System.out.println("<" + clientInstance.getUsername() + "> ServerSpawnPlayerPacket removed, because player specific is not player: " + clientInstance.getUsername() + "  " + serverSpawnPlayerPacketBehaviour.getEntityId());
                    clientInstance.getPacketSender().removePacket(packet);
                    return;
                }
                return;
            } else {
//                System.out.println("<" + clientInstance.getUsername() + "> ServerSpawnPlayerPacket removed, because packetwrapper not playerspecific) " + serverSpawnPlayerPacketBehaviour.getEntityId());
                clientInstance.getPacketSender().removePacket(packet);
//                System.out.println("ServerSpawnPlayerPacket not forwarded to client, because it is not player specific.");
            }
        }
    }
}
