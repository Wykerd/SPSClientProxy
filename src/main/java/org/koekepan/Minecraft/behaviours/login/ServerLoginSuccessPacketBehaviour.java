package org.koekepan.Minecraft.behaviours.login;

import com.github.steveice10.mc.protocol.packet.login.server.LoginSuccessPacket;
import com.github.steveice10.packetlib.packet.Packet;


import org.koekepan.herobrineproxy.ConsoleIO;
import org.koekepan.herobrineproxy.Logger;
import org.koekepan.herobrineproxy.behaviour.Behaviour;
import org.koekepan.herobrineproxy.session.IProxySessionNew;

public class ServerLoginSuccessPacketBehaviour implements Behaviour<Packet> {

	public static Boolean loginSuccess = false;
	private IProxySessionNew proxySession;
	private static Packet loginPacket;

	@SuppressWarnings("unused")
	private ServerLoginSuccessPacketBehaviour() {}
	
	
	public ServerLoginSuccessPacketBehaviour(IProxySessionNew proxySession) {
		this.proxySession = proxySession;
	}

    public static Packet get_packet() {
		return loginPacket;
    }

	@Override
	public void process(Packet packet) {
		proxySession.sendPacketToClient(packet);
		loginSuccess = true;
		loginPacket = packet;
		LoginSuccessPacket loginSuccessPacket = (LoginSuccessPacket)packet;
		Logger.log(this, Logger.Level.INFO, new String[]{"network", "behaviour", "initialisation"} ,"Player \""+loginSuccessPacket.getProfile().getName()+"\" has successfully logged into the server");
	}
}