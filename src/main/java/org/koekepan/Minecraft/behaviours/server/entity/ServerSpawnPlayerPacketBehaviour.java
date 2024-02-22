package org.koekepan.Minecraft.behaviours.server.entity;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnPlayerPacket;
import com.github.steveice10.packetlib.Client;
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
//            clientInstance.sendPacketToClient(packet);
            PacketWrapper.setProcessed(packet, true);
        } else {
            PacketWrapper packetWrapper = PacketWrapper.getPacketWrapper(packet);
            if (packetWrapper.PlayerSpecific() != null) {
                if (packetWrapper.PlayerSpecific().equals(clientInstance.getUsername())) {
                    PacketWrapper.setProcessed(packet, true);
//                    clientInstance.sendPacketToClient(packet);
                } else {
//                    SPSConnection.playerSpecificPacketMap.remove(packet);
                    clientInstance.getPacketSender().removePacket(packet);
                    return;
                }
                return;
            }
        }
    }
}
