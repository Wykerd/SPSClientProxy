package org.koekepan.Minecraft.behaviours.server.entity;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import com.github.steveice10.packetlib.packet.Packet;
import org.koekepan.VAST.Connection.ClientConnectedInstance;
import org.koekepan.VAST.Packet.Behaviour;
import org.koekepan.VAST.Packet.PacketWrapper;

public class ServerPlayerPositionPacketBehaviour implements Behaviour<Packet> {
    private final ClientConnectedInstance clientInstance;

    public ServerPlayerPositionPacketBehaviour(ClientConnectedInstance clientInstance) {
        this.clientInstance = clientInstance;
    }

    @Override
    public void process(Packet packet) {
        PacketWrapper.setProcessed(packet, true);

        ServerPlayerPositionRotationPacket serverPlayerPositionPacket = (ServerPlayerPositionRotationPacket) packet;

        this.clientInstance.set_position((int) serverPlayerPositionPacket.getX(), (int) serverPlayerPositionPacket.getZ());
        clientInstance.startPermanentSubscriptions(serverPlayerPositionPacket.getX(), serverPlayerPositionPacket.getZ());
    }
}
