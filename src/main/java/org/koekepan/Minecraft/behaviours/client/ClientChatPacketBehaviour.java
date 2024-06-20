package org.koekepan.Minecraft.behaviours.client;

import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerMovementPacket;
import com.github.steveice10.packetlib.packet.Packet;
import org.koekepan.VAST.Connection.ClientConnectedInstance;
import org.koekepan.VAST.Packet.Behaviour;
import org.koekepan.VAST.Packet.PacketWrapper;
import org.koekepan.VAST.Packet.SPSPacket;

public class ClientChatPacketBehaviour implements Behaviour<Packet> {

    ClientConnectedInstance clientInstance;

    public ClientChatPacketBehaviour(ClientConnectedInstance clientInstance) {
        this.clientInstance = clientInstance;
    }

    @Override
    public void process(Packet packet) {
        System.out.println("ClientChatPacketBehaviour::process => Processing ClientChatPacket");

        ClientChatPacket clientChatPacket = (ClientChatPacket) packet;
//        clientInstance.set_position((int) playerMovementPacket.getX(), (int) playerMovementPacket.getZ());

        SPSPacket spsPacket = new SPSPacket(packet, clientInstance.getUsername(), "serverBound");
        PacketWrapper packetWrapper = PacketWrapper.getPacketWrapper(packet);
        if (packetWrapper != null) {
            packetWrapper.setSPSPacket(spsPacket);
            packetWrapper.isProcessed = true;
//            PacketWrapper.setProcessed(packet, true);
//            PacketCapture.log(packet.getClass().getSimpleName() + "_DONE_" + packetWrapper.unique_id, PacketCapture.LogCategory.SERVERBOUND_BEH);
        } else {
//            PacketCapture.log(packet.getClass().getSimpleName() + "_DONE_NULLPACKETWRAPPER", PacketCapture.LogCategory.SERVERBOUND_BEH);
            System.out.println("<ERROR!> ClientChatPacketBehaviour::process => PacketWrapper is null while processing packet: " + packet.toString());
            clientInstance.getPacketSender().removePacket(packet);
            return;
        }

        if (clientChatPacket.getMessage().contains("migrate")) {
            System.out.println("ClientChatPacketBehaviour::process => ClientChatPacket contains 'migrate'");
            clientInstance.addChannelRegistration("Koekepan|migrate");
        }




    }
}
