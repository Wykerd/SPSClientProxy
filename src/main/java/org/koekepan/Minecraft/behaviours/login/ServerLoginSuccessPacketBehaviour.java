package org.koekepan.Minecraft.behaviours.login;

import com.github.steveice10.mc.protocol.packet.login.server.LoginSuccessPacket;
import com.github.steveice10.packetlib.packet.Packet;


import org.koekepan.VAST.Connection.ClientConnectedInstance;
import org.koekepan.VAST.Packet.Behaviour;
import org.koekepan.VAST.Packet.PacketWrapper;

public class ServerLoginSuccessPacketBehaviour implements Behaviour<Packet> {

//	public static Boolean loginSuccess = false;
	private ClientConnectedInstance clientInstance;
	private static Packet loginPacket;

	@SuppressWarnings("unused")
	private ServerLoginSuccessPacketBehaviour() {}
	
	
	public ServerLoginSuccessPacketBehaviour(ClientConnectedInstance clientInstance) {
		this.clientInstance = clientInstance;
	}

    public static Packet get_packet() {
		return loginPacket;
    }

	@Override
	public void process(Packet packet) {
//		clientInstance.sendPacketToClient(packet);
		PacketWrapper.setProcessed(packet, true);
//		loginSuccess = true;
		loginPacket = packet;
		LoginSuccessPacket loginSuccessPacket = (LoginSuccessPacket)packet;
//		Logger.log(this, Logger.Level.INFO, new String[]{"network", "behaviour", "initialisation"} ,"Player \""+loginSuccessPacket.getProfile().getName()+"\" has successfully logged into the server");
		System.out.println("Player \""+loginSuccessPacket.getProfile().getName()+"\" has successfully logged into the server");
	}
}