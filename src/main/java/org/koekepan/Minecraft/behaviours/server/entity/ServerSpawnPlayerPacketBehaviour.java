package org.koekepan.herobrineproxy.packet.behaviours.server.entity;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnPlayerPacket;
import com.github.steveice10.packetlib.packet.Packet;
import org.koekepan.herobrineproxy.behaviour.Behaviour;
import org.koekepan.herobrineproxy.session.ClientProxySession;
import org.koekepan.herobrineproxy.session.IProxySessionNew;
import org.koekepan.herobrineproxy.sps.SPSConnection;

public class ServerSpawnPlayerPacketBehaviour implements Behaviour<Packet> {
    private IProxySessionNew proxySession;
//    private IServerSession serverSession;

    private ServerSpawnPlayerPacketBehaviour() {
    }

    public ServerSpawnPlayerPacketBehaviour(IProxySessionNew proxySession) {
        this.proxySession = proxySession;
//        this.serverSession = serverSession;
    }

    @Override
    public void process(Packet packet) {
        ServerSpawnPlayerPacket serverSpawnPlayerPacketBehaviour = (ServerSpawnPlayerPacket) packet;

        if (serverSpawnPlayerPacketBehaviour.getEntityId() != ClientProxySession.getEntityID()) {
            proxySession.sendPacketToClient(packet);
        } else if (SPSConnection.playerSpecificPacketMap.containsKey(packet)) {
            if (SPSConnection.playerSpecificPacketMap.get(packet).channel.equals(proxySession.getUsername())) {
                proxySession.sendPacketToClient(packet);
            } else {
                SPSConnection.playerSpecificPacketMap.remove(packet);
                return;
            }
            return;
        }
    }
}
