package org.koekepan.herobrineproxy.packet.behaviours.server;

import com.github.steveice10.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import com.github.steveice10.packetlib.packet.Packet;

import org.koekepan.herobrineproxy.ConsoleIO;
import org.koekepan.herobrineproxy.Logger;
import org.koekepan.herobrineproxy.behaviour.Behaviour;
import org.koekepan.herobrineproxy.session.ClientProxySession;
import org.koekepan.herobrineproxy.session.IProxySessionNew;
import org.koekepan.herobrineproxy.session.IServerSession;

public class ServerJoinGamePacketBehaviour implements Behaviour<Packet> {
	public static boolean joined_game = false;
		
	private IProxySessionNew proxySession;
	private IServerSession serverSession;
	private static Packet joinpacket = null;
	
	@SuppressWarnings("unused")
	private ServerJoinGamePacketBehaviour() {}
	
	
	public ServerJoinGamePacketBehaviour(IProxySessionNew proxySession, IServerSession serverSession) {
		this.proxySession = proxySession;
		this.serverSession = serverSession;
	}

	public static Packet get_packet() {
		return joinpacket;
	}

	@Override
	public void process(Packet packet) {
		ServerJoinGamePacket serverJoinPacket = (ServerJoinGamePacket) packet;
		ClientProxySession.setEntityID(serverJoinPacket.getEntityId());
		//proxySession.setJoined(true);
		Logger.log(this, Logger.Level.DEBUG, new String[]{"network", "behaviour", "connection", "initialisation", "joinGamePacket"} ,"Player \""+proxySession.getUsername()+"\" with entityID <"+serverJoinPacket.getEntityId()+"> has successfully joined world");
//		proxySession.sendPacketToClient(packet);
		serverSession.setJoined(true);
//		joined_game = true;
		joinpacket = packet;
		//proxySession.registerForPluginChannels();
		//proxySession.getJoinedCountDownLatch().countDown();
		//MinecraftProtocol protocol = (MinecraftProtocol)(proxySession.getServer().getPacketProtocol());
	}
}