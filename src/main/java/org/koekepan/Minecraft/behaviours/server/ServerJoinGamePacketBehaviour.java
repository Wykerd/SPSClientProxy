package org.koekepan.Minecraft.behaviours.server;

import com.github.steveice10.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnPlayerPacket;
import com.github.steveice10.packetlib.packet.Packet;

import org.koekepan.App;
import org.koekepan.Performance.PacketCapture;
import org.koekepan.VAST.Connection.ClientConnectedInstance;
import org.koekepan.VAST.CustomPackets.EstablishConnectionPacket;
import org.koekepan.VAST.Packet.Behaviour;
import org.koekepan.VAST.Packet.PacketWrapper;
import org.koekepan.VAST.Packet.SPSPacket;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerJoinGamePacketBehaviour implements Behaviour<Packet> {
	public static boolean joined_game = false;
	private ClientConnectedInstance clientInstance;
//	private IServerSession serverSession;
//	private static Packet joinpacket = null;
	
	@SuppressWarnings("unused")
	private ServerJoinGamePacketBehaviour() {}
	
	
	public ServerJoinGamePacketBehaviour(ClientConnectedInstance clientInstance) {
		this.clientInstance = clientInstance;
//		this.serverSession = serverSession;
	}

//	public static Packet get_packet() {
//		return joinpacket;
//	}

	@Override
	public void process(Packet packet) {

		if (clientInstance.isMigrating()){
//			username, boolean establishConnection, String ip, int port

			if (clientInstance.getMinecraftServerHost() == null || clientInstance.getMinecraftServerPort() == 0) {
//				EstablishConnectionPacket establishConnectionPacket = new EstablishConnectionPacket(clientInstance.getUsername(), false, "10.42.0.205", 25565);
				EstablishConnectionPacket establishConnectionPacket = new EstablishConnectionPacket(clientInstance.getUsername(), false, App.config.getKoekepanBootHost(), App.config.getKoekepanBootPort());
				SPSPacket spsPacket = new SPSPacket(establishConnectionPacket, "serverBound", clientInstance.getXPosition(), clientInstance.getYPosition(), 100, "serverBound");

				System.out.println("ServerJoinGamePacketBehaviour::process => Sending Disconnect Packet username: " + clientInstance.getUsername() + " to KoekepanBootHost " + App.config.getKoekepanBootHost() + " port " + App.config.getKoekepanBootPort());

				clientInstance.getVastConnection().publish(spsPacket);
//				clientInstance.getPacketSender().addServerboundPacket(establishConnectionPacket);
//				(establishConnectionPacket);
			} else {
				EstablishConnectionPacket establishConnectionPacket = new EstablishConnectionPacket(clientInstance.getUsername(), false, clientInstance.getMinecraftServerHost(), clientInstance.getMinecraftServerPort());
				SPSPacket spsPacket = new SPSPacket(establishConnectionPacket, "serverBound", clientInstance.getXPosition(), clientInstance.getYPosition(), 100, "serverBound");

				System.out.println("ServerJoinGamePacketBehaviour::process => Sending Disconnect Packet username: " + clientInstance.getUsername() + " to ip " + clientInstance.getMinecraftServerHost() + " port " + clientInstance.getMinecraftServerPort());

				clientInstance.getVastConnection().publish(spsPacket);
//				clientInstance.getPacketSender().addServerboundPacket(establishConnectionPacket);
//				(establishConnectionPacket);
			}

			clientInstance.setMinecraftServer(clientInstance.getMigratingMinecraftServerHost(), clientInstance.getMigratingMinecraftServerPort());
			clientInstance.setMigratingMinecraftServer(null, 0);
//			clientInstance.setMigarting(false); //migrated

			System.out.println("ServerJoinGamePacketBehaviour::process => Migrated to server");

			ServerJoinGamePacket serverJoinPacket = (ServerJoinGamePacket) packet;
			clientInstance.setEntityID(serverJoinPacket.getEntityId());
			clientInstance.setJoined(true);
			clientInstance.getPacketSender().removePacket(packet);

			/// Publish a spawn client packet (for other clients that might be close to this client)
//			    public ServerSpawnPlayerPacket(int entityId, UUID uuid, double x, double y, double z, float yaw, float pitch, EntityMetadata metadata[]) {
//			ServerSpawnPlayerPacket spawnPlayerPacket = new ServerSpawnPlayerPacket(clientInstance.getEntityID(), clientInstance.getUUID(), clientInstance.getXPosition(), clientInstance.getYPosition(), clientInstance.getZPosition(), clientInstance.getYaw(), clientInstance.getPitch(), clientInstance.getMetadata());



			return;
		}

		System.out.println("ServerJoinGamePacketBehaviour::process => Processing ServerJoinGamePacket");
		ServerJoinGamePacket serverJoinPacket = (ServerJoinGamePacket) packet;

		clientInstance.setEntityID(serverJoinPacket.getEntityId());
		clientInstance.setJoined(true);

//		PacketWrapper.setProcessed(packet, true);
		clientInstance.getSession().send(packet);
		PacketCapture.log(packet.getClass().getSimpleName() + "_" + PacketWrapper.get_unique_id(packet), PacketCapture.LogCategory.CLIENTBOUND_OUT);
		clientInstance.getPacketSender().removePacket(packet);

		clientInstance.getPacketSender().startClientSender();
	}
}