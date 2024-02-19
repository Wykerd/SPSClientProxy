package org.koekepan.herobrineproxy.packet.behaviours.server.entity;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityEquipmentPacket;
import com.github.steveice10.packetlib.Client;
import com.github.steveice10.packetlib.packet.Packet;
import org.koekepan.herobrineproxy.Logger;
import org.koekepan.herobrineproxy.behaviour.Behaviour;
import org.koekepan.herobrineproxy.packet.PacketSender;
import org.koekepan.herobrineproxy.session.ClientProxySession;
import org.koekepan.herobrineproxy.session.ClientSession;
import org.koekepan.herobrineproxy.session.IProxySessionNew;
import org.koekepan.herobrineproxy.session.ProxySession;
import org.koekepan.herobrineproxy.sps.SPSConnection;
import org.koekepan.herobrineproxy.sps.SPSPacket;

public class ServerEntityEquipmentPacketBehaviour implements Behaviour<Packet> {
    private IProxySessionNew proxySession;
//    private IServerSession serverSession;

    private ServerEntityEquipmentPacketBehaviour() {
    }

    public ServerEntityEquipmentPacketBehaviour(IProxySessionNew proxySession) {
        this.proxySession = proxySession;
//        this.serverSession = serverSession;
    }

    @Override
    public void process(Packet packet) {
        ServerEntityEquipmentPacket serverEntityEquipmentPacket = (ServerEntityEquipmentPacket) packet;

        if (ClientProxySession.getEntityID() != serverEntityEquipmentPacket.getEntityId()){
            proxySession.sendPacketToClient(packet);
        } else if (SPSConnection.playerSpecificPacketMap.containsKey(packet)){

            if (SPSConnection.playerSpecificPacketMap.get(packet).channel.equals(proxySession.getUsername())){
                proxySession.sendPacketToClient(packet);
            } else {
                SPSConnection.playerSpecificPacketMap.remove(packet);
                PacketSender.removePacketFromQueue(packet);
                Logger.log(this, Logger.Level.WARN, new String[]{"Entity", "entityEquipment", "behaviour"}, "Entity equipment not forwarder to client.");
                return;
            }
            return;
        }
    }
}
