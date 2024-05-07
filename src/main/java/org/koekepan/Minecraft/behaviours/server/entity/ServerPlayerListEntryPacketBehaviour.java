package org.koekepan.Minecraft.behaviours.server.entity;

import com.github.steveice10.mc.protocol.data.game.PlayerListEntry;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerPlayerListEntryPacket;
import com.github.steveice10.packetlib.packet.Packet;
import org.koekepan.VAST.Connection.ClientConnectedInstance;
import org.koekepan.VAST.Packet.Behaviour;
import org.koekepan.VAST.Packet.PacketWrapper;

import java.util.Arrays;

public class ServerPlayerListEntryPacketBehaviour implements Behaviour<Packet> {

    private ClientConnectedInstance clientInstance;

    private ServerPlayerListEntryPacketBehaviour() {
    }

    public ServerPlayerListEntryPacketBehaviour(ClientConnectedInstance clientInstance) {
        this.clientInstance = clientInstance;
    }

    @Override
    public void process(Packet packet) {
        try {
            PacketWrapper.setProcessed(packet, true); // Server/Clientbound packets are assigned in ClientConnectedInstance and on Vast Publication
        } catch (Exception e) {
            System.out.println("ForwardPacketBehaviour for packet: <" + packet.getClass().getSimpleName() + "> failed setting processed: <" + e.getMessage() + ">");
        }

        ServerPlayerListEntryPacket serverPlayerListEntryPacket = (ServerPlayerListEntryPacket) packet;

//        clientInstance.addPlayerListEntry(serverPlayerListEntryPacket);
//        System.out.println("<" + clientInstance.getUsername() + "> ServerPlayerListEntryPacket entries: " + Arrays.toString(serverPlayerListEntryPacket.getEntries()));

//        clientInstance.addPlayerUUID(serverPlayerListEntryPacket.getEntries()[0].);

//        for (PlayerListEntry entry : serverPlayerListEntryPacket.getEntries()) {
//            clientInstance.addPlayerUUID(entry.getProfile().getId());
//        }


    }
}
