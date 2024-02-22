package org.koekepan.Minecraft.behaviours;

import org.koekepan.VAST.Connection.ClientConnectedInstance;
import org.koekepan.VAST.Packet.Behaviour;
import org.koekepan.VAST.Packet.PacketWrapper;

import com.github.steveice10.packetlib.packet.Packet;
import org.koekepan.VAST.Packet.SPSPacket;

public class ForwardPacketBehaviour implements Behaviour<Packet> {

	private ClientConnectedInstance clientInstance;
	private boolean toServer;
	
	@SuppressWarnings("unused")
	private ForwardPacketBehaviour() {}
	
	
	public ForwardPacketBehaviour(ClientConnectedInstance clientInstance, boolean toServer) {
		this.clientInstance = clientInstance;
		this.toServer = toServer;
	}

	
	@Override
	public void process(Packet packet) {
		if (toServer) {
//			clientInstance.getPacketSender().addServerboundPacket(packet);
			SPSPacket spsPacket = new SPSPacket(packet, clientInstance.getUsername(), "serverBound");
			PacketWrapper packetWrapper = PacketWrapper.getPacketWrapper(packet);
			if (packetWrapper != null) {
				packetWrapper.setSPSPacket(spsPacket);
			}
			else {
				//sleep for 5ms
				try {
					Thread.sleep(10); // Sometimes the packet wrapper is not set yet, so we sleep for a bit and try again
					packetWrapper = PacketWrapper.getPacketWrapper(packet);
					packetWrapper.setSPSPacket(spsPacket);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
//		else {
//			clientInstance.getPacketSender().addClientboundPacket(packet);
//		}
		PacketWrapper.setProcessed(packet, true); // Server/Clientbound packets are assigned in ClientConnectedInstance and on Vast Publication
	}
}
