package org.koekepan.Minecraft.behaviours.client;

import com.github.steveice10.packetlib.packet.Packet;
import org.koekepan.App;
import org.koekepan.VAST.Connection.ClientConnectedInstance;
import org.koekepan.VAST.CustomPackets.EstablishConnectionPacket;
import org.koekepan.VAST.Packet.Behaviour;
import org.koekepan.VAST.Packet.PacketWrapper;
import org.koekepan.VAST.Packet.SPSPacket;

public class EstablishConnectionPacketBehaviour implements Behaviour<Packet> {

    private final ClientConnectedInstance clientInstance;

    public EstablishConnectionPacketBehaviour(ClientConnectedInstance clientInstance) {
        this.clientInstance = clientInstance;
    }

    @Override
    public void process(Packet packet) {
        EstablishConnectionPacket establishConnectionPacket = (EstablishConnectionPacket)packet;

        SPSPacket spsPacket = new SPSPacket(packet, clientInstance.getUsername(), "serverBound");
        PacketWrapper packetWrapper = PacketWrapper.getPacketWrapper(packet);
        if (packetWrapper != null) {
            packetWrapper.setSPSPacket(spsPacket);
            if(establishConnectionPacket.establishConnection() && !clientInstance.isJoined()) {
                spsPacket.x = App.gateWayServer_xPosition;
                spsPacket.y = App.gateWayServer_yPosition;
                spsPacket.radius = 10; // A bit arbitrary, but also for migration might be beneficial to have a larger radius
            }
        }
		try {
            PacketWrapper.setProcessed(packet, true); // Server/Clientbound packets are assigned in ClientConnectedInstance and on Vast Publication
        } catch (Exception e) {
            System.out.println("EstablishConnectionPacketBehaviour for packet: <" + packet.getClass().getSimpleName() + "> failed setting processed: <" + e.getMessage() + ">");
        }
    }

}
