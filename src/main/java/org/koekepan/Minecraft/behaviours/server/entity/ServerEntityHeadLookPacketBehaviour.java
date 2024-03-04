package org.koekepan.Minecraft.behaviours.server.entity;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityHeadLookPacket;
import com.github.steveice10.packetlib.packet.Packet;
import org.koekepan.VAST.Connection.ClientConnectedInstance;
import org.koekepan.VAST.Packet.Behaviour;
import org.koekepan.VAST.Packet.PacketWrapper;

public class ServerEntityHeadLookPacketBehaviour implements Behaviour<Packet> {
    private ClientConnectedInstance clientInstance;
//    private IServerSession serverSession;

//    private ServerEntityMovementPacketBehaviour() {
//    }

    public ServerEntityHeadLookPacketBehaviour(ClientConnectedInstance clientInstance) {
        this.clientInstance = clientInstance;
//        this.serverSession = serverSession;
    }

    @Override
    public void process(Packet packet) {
        ServerEntityHeadLookPacket serverEntityHeadLookPacket = (ServerEntityHeadLookPacket) packet;

        if (clientInstance.getEntityID() == -1) {
//            Logger.log(this, Logger.Level.ERROR, new String[]{"behaviour", "entityMovement"}, "Client is not assigned an EntityID yet.");
            return;
        } else if (serverEntityHeadLookPacket.getEntityId() != clientInstance.getEntityID()) {
//            System.out.println("Forwarded to client");
//            clientInstance.sendPacketToClient(packet);
            PacketWrapper.setProcessed(packet, true);
            return;
        } else {
            PacketWrapper packetWrapper = PacketWrapper.getPacketWrapper(packet);
            if (packetWrapper.getPlayerSpecific() != null){

                if (packetWrapper.getPlayerSpecific().equals(clientInstance.getUsername())){
                    PacketWrapper.setProcessed(packet, true);
//                    clientInstance.sendPacketToClient(packet);
                } else {
//                    SPSConnection.playerSpecificPacketMap.remove(packet);
//                    PacketSender.removePacketFromQueue(packet);
                    clientInstance.getPacketSender().removePacket(packet);
//                    Logger.log(this, Logger.Level.INFO, new String[]{"behaviour", "entityMovement"}, "entityHeadLook not forwarded to client.");
                    return;
                }
                return;
            } else {
                clientInstance.getPacketSender().removePacket(packet);
//                System.out.println("ServerEntityHeadLookPacket not forwarded to client, because it is not player specific.");
            }
        }
    }
}

