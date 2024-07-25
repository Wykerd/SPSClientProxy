package org.koekepan.Minecraft.behaviours.server.entity;

import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerPositionRotationPacket;
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
//        System.out.println("ServerPlayerPositionPacketBehaviour::process => Player \""+clientInstance.getUsername()+"\" received location: "+packet.toString());

        if (clientInstance.isMigrating()) {
            ServerPlayerPositionRotationPacket p = (ServerPlayerPositionRotationPacket) packet;
////            ConsoleIO.println("ServerPlayerPositionRotationPacket::process => Player \""+proxySession.getUsername()+"\" received location: "+p.toString());
//            System.out.println("ServerPlayerPositionRotationPacket::process => Player \""+clientInstance.getUsername()+"\" received location: "+p.toString());
//            ClientPlayerPositionRotationPacket responsePacket = new ClientPlayerPositionRotationPacket(true, p.getX(), p.getY(), p.getZ(), p.getYaw(), p.getPitch());

            clientInstance.setMigarting(false);
            PacketWrapper.setProcessed(packet, true);
            System.out.println("ServerPlayerPositionPacketBehaviour::process => Player \""+clientInstance.getUsername()+"\" has finished migrating to the new server");
            clientInstance.addChannelRegistration("Koekepan|migrate");

//            ClientPlayerPositionRotationPacket responsePacket = new ClientPlayerPositionRotationPacket(true, p.getX(), p.getY(), p.getZ(), p.getYaw(), p.getPitch());
//            clientInstance.getPacketSender().addServerboundPacket(responsePacket);
//            clientInstance.getPacketSender().removePacket(packet);
            return;
        }


        PacketWrapper.setProcessed(packet, true);
        clientInstance.addChannelRegistration("Koekepan|migrate");
//        clientInstance.addChannelRegistration("Koekepan|kick");

        ServerPlayerPositionRotationPacket serverPlayerPositionPacket = (ServerPlayerPositionRotationPacket) packet;

        this.clientInstance.set_position((int) serverPlayerPositionPacket.getX(), (int) serverPlayerPositionPacket.getZ(), true);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        System.out.println("Starting permanent subscription at x: "+serverPlayerPositionPacket.getX()+" z: "+serverPlayerPositionPacket.getZ());
        clientInstance.startPermanentSubscriptions(serverPlayerPositionPacket.getX(), serverPlayerPositionPacket.getZ());
    }
}
