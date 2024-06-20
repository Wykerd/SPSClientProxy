package org.koekepan.Minecraft.behaviours;

import org.koekepan.Minecraft.behaviours.login.ServerLoginSuccessPacketBehaviour;
import org.koekepan.Minecraft.behaviours.server.ServerChunkDataPacketBehaviour;
import org.koekepan.Minecraft.behaviours.server.ServerJoinGamePacketBehaviour;
import org.koekepan.Minecraft.behaviours.server.ServerPluginMessagePacketBehaviour;
import org.koekepan.Minecraft.behaviours.server.ServerUnloadChunkPacketBehaviour;
import org.koekepan.Minecraft.behaviours.server.entity.*;
import org.koekepan.VAST.Connection.ClientConnectedInstance;
import org.koekepan.VAST.CustomPackets.EstablishConnectionPacket;
import org.koekepan.VAST.Packet.BehaviourHandler;

import com.github.steveice10.mc.protocol.packet.ingame.server.*;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.*;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.*;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.*;
import com.github.steveice10.mc.protocol.packet.ingame.server.scoreboard.*;
import com.github.steveice10.mc.protocol.packet.ingame.server.window.*;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.*;
import com.github.steveice10.mc.protocol.packet.login.server.*;
import com.github.steveice10.mc.protocol.packet.status.server.*;
import com.github.steveice10.packetlib.packet.Packet;

public class ClientBoundPacketBehaviours extends BehaviourHandler<Packet> {

	private ClientConnectedInstance clientInstance;
//	private IServerSession serverSession;
	private ForwardPacketBehaviour clientForwarder;

	public ClientBoundPacketBehaviours(ClientConnectedInstance clientInstance) {
		this.clientInstance = clientInstance;
//		this.serverSession = serverSession;
	}
	

	/*public void registerDefaultBehaviours() {
		clearBehaviours();
		this.registerForwardingBehaviour();
	}*/
	
	
	public void registerForwardingBehaviour() {
		clearBehaviours();
		clientForwarder = new ForwardPacketBehaviour(clientInstance, false);
		registerBehaviour(LoginDisconnectPacket.class, clientForwarder);
		registerBehaviour(EncryptionRequestPacket.class, clientForwarder);
		registerBehaviour(LoginSuccessPacket.class, new ServerLoginSuccessPacketBehaviour(clientInstance));
		registerBehaviour(LoginSetCompressionPacket.class, clientForwarder);

		registerBehaviour(ServerSpawnObjectPacket.class, clientForwarder);
		registerBehaviour(ServerSpawnExpOrbPacket.class, clientForwarder);
		registerBehaviour(ServerSpawnGlobalEntityPacket.class, clientForwarder);
		registerBehaviour(ServerSpawnMobPacket.class, clientForwarder);
		registerBehaviour(ServerSpawnPaintingPacket.class, clientForwarder);

		registerBehaviour(ServerSpawnPlayerPacket.class, new ServerSpawnPlayerPacketBehaviour(clientInstance));

		registerBehaviour(ServerEntityAnimationPacket.class, clientForwarder);
		registerBehaviour(ServerStatisticsPacket.class, clientForwarder);
		registerBehaviour(ServerBlockBreakAnimPacket.class, clientForwarder);
		registerBehaviour(ServerUpdateTileEntityPacket.class, clientForwarder);
		registerBehaviour(ServerBlockValuePacket.class, clientForwarder);
		registerBehaviour(ServerBlockChangePacket.class, clientForwarder);
		registerBehaviour(ServerBossBarPacket.class, clientForwarder);
		registerBehaviour(ServerDifficultyPacket.class, clientForwarder);
		registerBehaviour(ServerTabCompletePacket.class, clientForwarder);
		registerBehaviour(ServerChatPacket.class, clientForwarder);
		registerBehaviour(ServerMultiBlockChangePacket.class, clientForwarder);
		registerBehaviour(ServerConfirmTransactionPacket.class, clientForwarder);
		registerBehaviour(ServerCloseWindowPacket.class, clientForwarder);
		registerBehaviour(ServerOpenWindowPacket.class, clientForwarder);
		registerBehaviour(ServerWindowItemsPacket.class, clientForwarder);
		registerBehaviour(ServerWindowPropertyPacket.class, clientForwarder);
		registerBehaviour(ServerSetSlotPacket.class, clientForwarder);
		registerBehaviour(ServerSetCooldownPacket.class, clientForwarder);
		registerBehaviour(ServerPlaySoundPacket.class, clientForwarder);
		registerBehaviour(ServerDisconnectPacket.class, clientForwarder);
		registerBehaviour(ServerEntityStatusPacket.class, clientForwarder);
		registerBehaviour(ServerExplosionPacket.class, clientForwarder);

		registerBehaviour(ServerUnloadChunkPacket.class, new ServerUnloadChunkPacketBehaviour(clientInstance));
//		registerBehaviour(ServerUnloadChunkPacket.class, clientForwarder);

		registerBehaviour(ServerNotifyClientPacket.class, clientForwarder);
		registerBehaviour(ServerKeepAlivePacket.class, clientForwarder);


		registerBehaviour(ServerChunkDataPacket.class, new ServerChunkDataPacketBehaviour(clientInstance));
//		registerBehaviour(ServerChunkDataPacket.class, clientForwarder);

		registerBehaviour(ServerPlayEffectPacket.class, clientForwarder);
		registerBehaviour(ServerSpawnParticlePacket.class, clientForwarder);
		registerBehaviour(ServerJoinGamePacket.class, new ServerJoinGamePacketBehaviour(clientInstance));
		registerBehaviour(ServerMapDataPacket.class, clientForwarder);


		/// Movement Packets ///
		/// These packets are used to update the position of an entity on the client. They should not be forwarded to the client
		/// if the entityId contained in the packet is the entityId of the client.
		registerBehaviour(ServerEntityPositionPacket.class, 		new ServerEntityMovementPacketBehaviour(clientInstance));
		registerBehaviour(ServerEntityPositionRotationPacket.class, new ServerEntityMovementPacketBehaviour(clientInstance));
		registerBehaviour(ServerEntityRotationPacket.class, 		new ServerEntityMovementPacketBehaviour(clientInstance));
		registerBehaviour(ServerEntityMovementPacket.class, 		new ServerEntityMovementPacketBehaviour(clientInstance));

		registerBehaviour(ServerEntityHeadLookPacket.class, 		new ServerEntityHeadLookPacketBehaviour(clientInstance));
		////////////////////////

		registerBehaviour(ServerVehicleMovePacket.class, clientForwarder);
		registerBehaviour(ServerOpenTileEntityEditorPacket.class, clientForwarder);
		registerBehaviour(ServerPlayerAbilitiesPacket.class, clientForwarder);
		registerBehaviour(ServerCombatPacket.class, clientForwarder);
		registerBehaviour(ServerPlayerListEntryPacket.class, 		new ServerPlayerListEntryPacketBehaviour(clientInstance));
		registerBehaviour(ServerPlayerUseBedPacket.class, clientForwarder);
		registerBehaviour(ServerEntityDestroyPacket.class, clientForwarder);
		registerBehaviour(ServerEntityRemoveEffectPacket.class, clientForwarder);
		registerBehaviour(ServerResourcePackSendPacket.class, clientForwarder);
		registerBehaviour(ServerRespawnPacket.class, clientForwarder);

		registerBehaviour(ServerWorldBorderPacket.class, clientForwarder);
		registerBehaviour(ServerSwitchCameraPacket.class, clientForwarder);
		registerBehaviour(ServerPlayerChangeHeldItemPacket.class, clientForwarder);
		registerBehaviour(ServerDisplayScoreboardPacket.class, clientForwarder);
		registerBehaviour(ServerDisplayScoreboardPacket.class, clientForwarder);
		registerBehaviour(ServerEntityMetadataPacket.class, clientForwarder);
		registerBehaviour(ServerEntityAttachPacket.class, clientForwarder);

		registerBehaviour(ServerEntityVelocityPacket.class, 		new ServerEntityVelocityPacketBehaviour(clientInstance));
		registerBehaviour(ServerEntityEquipmentPacket.class, 		new ServerEntityEquipmentPacketBehaviour(clientInstance));

		registerBehaviour(ServerPlayerPositionRotationPacket.class, new ServerPlayerPositionPacketBehaviour(clientInstance));
		registerBehaviour(ServerPlayerSetExperiencePacket.class, clientForwarder);
		registerBehaviour(ServerPlayerHealthPacket.class, clientForwarder);
		registerBehaviour(ServerScoreboardObjectivePacket.class, clientForwarder);
		registerBehaviour(ServerEntitySetPassengersPacket.class, clientForwarder);
		registerBehaviour(ServerTeamPacket.class, clientForwarder);
		registerBehaviour(ServerUpdateScorePacket.class, clientForwarder);
		registerBehaviour(ServerSpawnPositionPacket.class, clientForwarder);
		registerBehaviour(ServerUpdateTimePacket.class, clientForwarder);
		registerBehaviour(ServerTitlePacket.class, clientForwarder);
		registerBehaviour(ServerPlayBuiltinSoundPacket.class, clientForwarder);
		registerBehaviour(ServerPlayerListDataPacket.class, clientForwarder);
		registerBehaviour(ServerEntityCollectItemPacket.class, clientForwarder);

		registerBehaviour(ServerEntityTeleportPacket.class, 		new ServerEntityTeleportPacketBehaviour(clientInstance));

		registerBehaviour(ServerEntityPropertiesPacket.class, clientForwarder);
		registerBehaviour(ServerEntityEffectPacket.class, clientForwarder);

		registerBehaviour(StatusResponsePacket.class, clientForwarder);
		registerBehaviour(StatusPongPacket.class, clientForwarder);

		registerBehaviour(ServerPluginMessagePacket.class, new ServerPluginMessagePacketBehaviour(clientInstance));
//		registerBehaviour(ServerPluginMessagePacket.class, new ServerPluginMessagePacketBehaviour(clientInstance));
	}
	
	
//	public void registerMigrationBehaviour() {
//		this.clearBehaviours();
//		clientForwarder = new ForwardPacketBehaviour(proxySession, false);
//		registerBehaviour(LoginDisconnectPacket.class, clientForwarder);
//		registerBehaviour(EncryptionRequestPacket.class, clientForwarder);
//		registerBehaviour(LoginSuccessPacket.class, new MigrateLoginSuccessPacketBehaviour(proxySession) );
//		registerBehaviour(LoginSetCompressionPacket.class, new LoginSetCompressionPacketBehaviour());
//
// 		registerBehaviour(ServerSpawnObjectPacket.class, clientForwarder);
//		registerBehaviour(ServerSpawnExpOrbPacket.class, clientForwarder);
//		registerBehaviour(ServerSpawnGlobalEntityPacket.class, clientForwarder);
//		registerBehaviour(ServerSpawnMobPacket.class, clientForwarder);
//		registerBehaviour(ServerSpawnPaintingPacket.class, clientForwarder);
//		registerBehaviour(ServerSpawnPlayerPacket.class, clientForwarder);
//		registerBehaviour(ServerEntityAnimationPacket.class, clientForwarder);
//		registerBehaviour(ServerStatisticsPacket.class, clientForwarder);
//		registerBehaviour(ServerBlockBreakAnimPacket.class, clientForwarder);
//		registerBehaviour(ServerUpdateTileEntityPacket.class, clientForwarder);
//		registerBehaviour(ServerBlockValuePacket.class, clientForwarder);
//		registerBehaviour(ServerBlockChangePacket.class, clientForwarder);
//		registerBehaviour(ServerBossBarPacket.class, clientForwarder);
//		registerBehaviour(ServerDifficultyPacket.class, clientForwarder);
//		registerBehaviour(ServerTabCompletePacket.class, clientForwarder);
//		registerBehaviour(ServerChatPacket.class, clientForwarder);
//		registerBehaviour(ServerMultiBlockChangePacket.class, clientForwarder);
//		registerBehaviour(ServerConfirmTransactionPacket.class, clientForwarder);
//		registerBehaviour(ServerCloseWindowPacket.class, clientForwarder);
//		registerBehaviour(ServerOpenWindowPacket.class, clientForwarder);
//		registerBehaviour(ServerWindowItemsPacket.class, clientForwarder);
//		registerBehaviour(ServerWindowPropertyPacket.class, clientForwarder);
//		registerBehaviour(ServerSetSlotPacket.class, clientForwarder);
//		registerBehaviour(ServerSetCooldownPacket.class, clientForwarder);
//		registerBehaviour(ServerPlaySoundPacket.class, clientForwarder);
//		registerBehaviour(ServerDisconnectPacket.class, clientForwarder);
//		registerBehaviour(ServerEntityStatusPacket.class, clientForwarder);
//		registerBehaviour(ServerExplosionPacket.class, clientForwarder);
//		registerBehaviour(ServerUnloadChunkPacket.class, clientForwarder);
//		registerBehaviour(ServerNotifyClientPacket.class, clientForwarder);
//		registerBehaviour(ServerKeepAlivePacket.class, clientForwarder);
//		registerBehaviour(ServerChunkDataPacket.class, clientForwarder);
//		registerBehaviour(ServerPlayEffectPacket.class, clientForwarder);
//		registerBehaviour(ServerSpawnParticlePacket.class, clientForwarder);
//		registerBehaviour(ServerJoinGamePacket.class, new MigrateJoinGamePacketBehaviour(proxySession, serverSession));
//		registerBehaviour(ServerMapDataPacket.class, clientForwarder);
//		registerBehaviour(ServerEntityPositionPacket.class, clientForwarder);
//		registerBehaviour(ServerEntityPositionRotationPacket.class, clientForwarder);
//		registerBehaviour(ServerEntityRotationPacket.class, clientForwarder);
//		registerBehaviour(ServerEntityMovementPacket.class, clientForwarder);
//		registerBehaviour(ServerVehicleMovePacket.class, clientForwarder);
//		registerBehaviour(ServerOpenTileEntityEditorPacket.class, clientForwarder);
//		registerBehaviour(ServerPlayerAbilitiesPacket.class, clientForwarder);
//		registerBehaviour(ServerCombatPacket.class, clientForwarder);
//		registerBehaviour(ServerPlayerListEntryPacket.class, clientForwarder);
//		registerBehaviour(ServerPlayerPositionRotationPacket.class, new ServerPlayerPositionPacketBehaviour(proxySession));
//		registerBehaviour(ServerPlayerUseBedPacket.class, clientForwarder);
//		registerBehaviour(ServerEntityDestroyPacket.class, clientForwarder);
//		registerBehaviour(ServerEntityRemoveEffectPacket.class, clientForwarder);
//		registerBehaviour(ServerResourcePackSendPacket.class, clientForwarder);
//		registerBehaviour(ServerRespawnPacket.class, clientForwarder);
//		registerBehaviour(ServerEntityHeadLookPacket.class, clientForwarder);
//		registerBehaviour(ServerWorldBorderPacket.class, clientForwarder);
//		registerBehaviour(ServerSwitchCameraPacket.class, clientForwarder);
//		registerBehaviour(ServerPlayerChangeHeldItemPacket.class, clientForwarder);
//		registerBehaviour(ServerDisplayScoreboardPacket.class, clientForwarder);
//		registerBehaviour(ServerEntityMetadataPacket.class, clientForwarder);
//		registerBehaviour(ServerEntityAttachPacket.class, clientForwarder);
//		registerBehaviour(ServerEntityVelocityPacket.class, clientForwarder);
//		registerBehaviour(ServerEntityEquipmentPacket.class, clientForwarder);
//		registerBehaviour(ServerPlayerSetExperiencePacket.class, clientForwarder);
//		registerBehaviour(ServerPlayerHealthPacket.class, clientForwarder);
//		registerBehaviour(ServerScoreboardObjectivePacket.class, clientForwarder);
//		registerBehaviour(ServerEntitySetPassengersPacket.class, clientForwarder);
//		registerBehaviour(ServerTeamPacket.class, clientForwarder);
//		registerBehaviour(ServerUpdateScorePacket.class, clientForwarder);
//		registerBehaviour(ServerSpawnPositionPacket.class, clientForwarder);
//		registerBehaviour(ServerUpdateTimePacket.class, clientForwarder);
//		registerBehaviour(ServerTitlePacket.class, clientForwarder);
//		registerBehaviour(ServerPlayBuiltinSoundPacket.class, clientForwarder);
//		registerBehaviour(ServerPlayerListDataPacket.class, clientForwarder);
//		registerBehaviour(ServerEntityCollectItemPacket.class, clientForwarder);
//		registerBehaviour(ServerEntityTeleportPacket.class, clientForwarder);
//		registerBehaviour(ServerEntityPropertiesPacket.class, clientForwarder);
//		registerBehaviour(ServerEntityEffectPacket.class, clientForwarder);
//
//		registerBehaviour(StatusResponsePacket.class, clientForwarder);
//		registerBehaviour(StatusPongPacket.class, clientForwarder);
//
//		registerBehaviour(ServerPluginMessagePacket.class, new ServerPluginMessagePacketBehaviour(proxySession));
//	}
}
