package org.koekepan.herobrineproxy.packet.behaviours.server.entity;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityMovementPacket;
import com.github.steveice10.packetlib.Client;
import com.github.steveice10.packetlib.packet.Packet;
import org.koekepan.herobrineproxy.Logger;
import org.koekepan.herobrineproxy.behaviour.Behaviour;
import org.koekepan.herobrineproxy.packet.PacketSender;
import org.koekepan.herobrineproxy.session.ClientProxySession;
import org.koekepan.herobrineproxy.session.IProxySessionNew;
import org.koekepan.herobrineproxy.sps.SPSConnection;
import org.koekepan.herobrineproxy.sps.SPSPacket;

public class ServerEntityMovementPacketBehaviour implements Behaviour<Packet> {
    private IProxySessionNew proxySession;
//    private IServerSession serverSession;

    private ServerEntityMovementPacketBehaviour() {
    }

    public ServerEntityMovementPacketBehaviour(IProxySessionNew proxySession) {
        this.proxySession = proxySession;
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
        if (serverEntityMovementPacket.getEntityId() != ClientProxySession.getEntityID()) {
            proxySession.sendPacketToClient(packet);
            return;
        } else if (SPSConnection.playerSpecificPacketMap.containsKey(packet)){

            if (SPSConnection.playerSpecificPacketMap.get(packet).channel.equals(proxySession.getUsername())){
                proxySession.sendPacketToClient(packet);
            } else {
                Logger.log(this, Logger.Level.WARN, new String[]{"behaviour", "entityMovement"}, "Entity movement not forwarded to client: " + SPSConnection.playerSpecificPacketMap.get(packet).channel);
                PacketSender.removePacketFromQueue(packet);
                SPSConnection.playerSpecificPacketMap.remove(packet);
                return;
            }
            return;
        }
    }
}
