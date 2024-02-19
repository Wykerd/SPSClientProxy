package org.koekepan.herobrineproxy.packet.behaviours.server.entity;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityHeadLookPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityMovementPacket;
import com.github.steveice10.packetlib.packet.Packet;
import org.koekepan.herobrineproxy.Logger;
import org.koekepan.herobrineproxy.behaviour.Behaviour;
import org.koekepan.herobrineproxy.packet.PacketSender;
import org.koekepan.herobrineproxy.session.ClientProxySession;
import org.koekepan.herobrineproxy.session.IProxySessionNew;
import org.koekepan.herobrineproxy.sps.SPSConnection;

public class ServerEntityHeadLookPacketBehaviour implements Behaviour<Packet> {
    private IProxySessionNew proxySession;
//    private IServerSession serverSession;

//    private ServerEntityMovementPacketBehaviour() {
//    }

    public ServerEntityHeadLookPacketBehaviour(IProxySessionNew proxySession) {
        this.proxySession = proxySession;
//        this.serverSession = serverSession;
    }

    @Override
    public void process(Packet packet) {
        ServerEntityHeadLookPacket serverEntityHeadLookPacket = (ServerEntityHeadLookPacket) packet;

        if (ClientProxySession.getEntityID() == 0) {
            Logger.log(this, Logger.Level.ERROR, new String[]{"behaviour", "entityMovement"}, "Client is not assigned an EntityID yet.");
            return;
        } else if (serverEntityHeadLookPacket.getEntityId() != ClientProxySession.getEntityID()) {
//            System.out.println("Forwarded to client");
            proxySession.sendPacketToClient(packet);
            return;
        } else if (SPSConnection.playerSpecificPacketMap.containsKey(packet)){

            if (SPSConnection.playerSpecificPacketMap.get(packet).channel.equals(proxySession.getUsername())){
                proxySession.sendPacketToClient(packet);
            } else {
                SPSConnection.playerSpecificPacketMap.remove(packet);
                PacketSender.removePacketFromQueue(packet);
                Logger.log(this, Logger.Level.INFO, new String[]{"behaviour", "entityMovement"}, "entityHeadLook not forwarded to client.");
                return;
            }
            return;
        }
    }
}

