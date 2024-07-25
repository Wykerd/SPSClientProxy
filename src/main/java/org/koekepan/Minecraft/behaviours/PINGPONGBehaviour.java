package org.koekepan.Minecraft.behaviours;

import org.koekepan.VAST.Connection.ClientConnectedInstance;
import org.koekepan.VAST.Connection.PacketSender;
import org.koekepan.VAST.Packet.Behaviour;
import org.koekepan.VAST.Packet.PacketWrapper;

import com.github.steveice10.packetlib.packet.Packet;
import org.koekepan.VAST.Packet.SPSPacket;

public class PINGPONGBehaviour implements Behaviour<Packet> {

    private ClientConnectedInstance emulatedClientConnection;
    private boolean toServer;

    @SuppressWarnings("unused")
    private PINGPONGBehaviour() {}


    public PINGPONGBehaviour(ClientConnectedInstance emulatedClientConnection) {
        this.emulatedClientConnection = emulatedClientConnection;
    }


    @Override
    public void process(Packet packet) {
        PacketWrapper packetWrapper = PacketWrapper.getPacketWrapper(packet);
        toServer = !packetWrapper.clientBound;

        new ForwardPacketBehaviour(emulatedClientConnection, toServer).process(packet);
    }
}