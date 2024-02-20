package org.koekepan.Minecraft.behaviours.server.entity;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityTeleportPacket;
import com.github.steveice10.packetlib.packet.Packet;
import org.koekepan.VAST.Connection.ClientConnectedInstance;
import org.koekepan.VAST.Packet.Behaviour;
import org.koekepan.VAST.Packet.PacketWrapper;

public class ServerEntityTeleportPacketBehaviour implements Behaviour<Packet> {
    private ClientConnectedInstance clientInstance;
//    private IServerSession serverSession;

    private ServerEntityTeleportPacketBehaviour() {
    }

    public ServerEntityTeleportPacketBehaviour(ClientConnectedInstance clientInstance) {
        this.clientInstance = clientInstance;
//        this.serverSession = serverSession;
    }

    @Override
    public void process(Packet packet) {
        ServerEntityTeleportPacket serverEntityTeleportPacket = (ServerEntityTeleportPacket) packet;

        if (clientInstance.getEntityID() <= 0) {
//            Logger.log(this, Logger.Level.ERROR, new String[]{"behaviour", "entityMovement"}, "Client is not assigned an EntityID yet.");
            System.out.println("Client is not assigned an EntityID yet.");
            return;
        } else if (serverEntityTeleportPacket.getEntityId() != clientInstance.getEntityID()) {
            PacketWrapper.setProcessed(packet, true);
//            clientInstance.sendPacketToClient(packet);
            return;
        } else {
            PacketWrapper packetWrapper = PacketWrapper.getPacketWrapper(packet);
            if (packetWrapper.PlayerSpecific() != null){

                if (packetWrapper.PlayerSpecific().equals(clientInstance.getUsername())){
                    PacketWrapper.setProcessed(packet, true);
//                    clientInstance.sendPacketToClient(packet);
                } else {
//                    SPSConnection.playerSpecificPacketMap.remove(packet);
//                    PacketSender.removePacketFromQueue(packet);
//                    Logger.log(this, Logger.Level.WARN, new String[]{"behaviour", "entityMovement"}, "EntityTeleport not forwarded to client.");
                    System.out.println("EntityTeleport not forwarded to client.");
                    clientInstance.getPacketSender().removePacket(packet);
                    return;
                }
                return;
            }
        }

    }
}
