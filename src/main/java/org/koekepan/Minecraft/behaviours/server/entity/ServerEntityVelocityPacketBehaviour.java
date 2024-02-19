package org.koekepan.herobrineproxy.packet.behaviours.server.entity;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityVelocityPacket;
import com.github.steveice10.packetlib.packet.Packet;
import org.koekepan.herobrineproxy.Logger;
import org.koekepan.herobrineproxy.behaviour.Behaviour;
import org.koekepan.herobrineproxy.packet.PacketSender;
import org.koekepan.herobrineproxy.session.ClientProxySession;
import org.koekepan.herobrineproxy.session.IProxySessionNew;
import org.koekepan.herobrineproxy.sps.SPSConnection;
import org.koekepan.herobrineproxy.sps.SPSPacket;

public class ServerEntityVelocityPacketBehaviour implements Behaviour<Packet> {
    private IProxySessionNew proxySession;
//    private IServerSession serverSession;

    private ServerEntityVelocityPacketBehaviour() {
    }

    public ServerEntityVelocityPacketBehaviour(IProxySessionNew proxySession) {
        this.proxySession = proxySession;
//        this.serverSession = serverSession;
    }

    @Override
    public void process(Packet packet) {
        ServerEntityVelocityPacket serverEntityVelocityPacket = (ServerEntityVelocityPacket) packet;

//        ConsoleIO.println("Block changed at position: " + blockRecord.getPosition().getX() + ", " + blockRecord.getPosition().getY() + ", " + blockRecord.getPosition().getZ());        
        if (ClientProxySession.getEntityID() != serverEntityVelocityPacket.getEntityId()){
            proxySession.sendPacketToClient(packet);
        } else if (SPSConnection.playerSpecificPacketMap.containsKey(packet)){

            if (SPSConnection.playerSpecificPacketMap.get(packet).channel.equals(proxySession.getUsername())){
                proxySession.sendPacketToClient(packet);
            } else {
                SPSConnection.playerSpecificPacketMap.remove(packet);
                PacketSender.removePacketFromQueue(packet);
                Logger.log(this, Logger.Level.WARN, new String[]{"Entity", "entityVelocity", "behaviour"}, "Entity velocity not forwarded to client.");
                return;
            }
            return;
        }
    }
}
