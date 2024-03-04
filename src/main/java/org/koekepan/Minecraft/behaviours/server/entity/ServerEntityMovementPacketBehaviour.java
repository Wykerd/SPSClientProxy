package org.koekepan.Minecraft.behaviours.server.entity;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityMovementPacket;
import com.github.steveice10.packetlib.packet.Packet;
import org.koekepan.VAST.Connection.ClientConnectedInstance;
import org.koekepan.VAST.Packet.Behaviour;
import org.koekepan.VAST.Packet.PacketWrapper;

public class ServerEntityMovementPacketBehaviour implements Behaviour<Packet> {
    private ClientConnectedInstance clientInstance;
//    private IServerSession serverSession;

    private ServerEntityMovementPacketBehaviour() {
    }

    public ServerEntityMovementPacketBehaviour(ClientConnectedInstance clientInstance) {
        this.clientInstance = clientInstance;
//        this.serverSession = serverSession;
    }

    @Override
    public void process(Packet packet) {
        ServerEntityMovementPacket serverEntityMovementPacket = (ServerEntityMovementPacket) packet;

        // serverEntityMovementPacket.getEntityId() should be the same as ClientProxySession.getEntityID(), if
        // they are not the same, the packet entity is the client and should not be sent to the client.
        // if ClientProxySession EntityID is 0, then the client has not yet joined the server Logger should
        // print an error message and return.
//        if (ClientProxySession.getEntityID() == 0) {
//            Logger.log(this, Logger.Level.ERROR, new String[]{"behaviour", "entityMovement"}, "Client is not assigned an EntityID yet.");
//            return;
//        } else
        if (serverEntityMovementPacket.getEntityId() != clientInstance.getEntityID()) {
            PacketWrapper.setProcessed(packet, true);
//            clientInstance.sendPacketToClient(packet);
            return;
        } else {
            PacketWrapper packetWrapper = PacketWrapper.getPacketWrapper(packet);
            if (packetWrapper.getPlayerSpecific() != null){

                if (packetWrapper.getPlayerSpecific().equals(clientInstance.getUsername())){
                    PacketWrapper.setProcessed(packet, true);
                } else {
                    clientInstance.getPacketSender().removePacket(packet);
                    return;
                }
                return;
            } else {
                clientInstance.getPacketSender().removePacket(packet);
//                System.out.println("ServerEntityMovementPacket not forwarded to client, because it is not player specific.");
            }
        }
    }
}
