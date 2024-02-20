package org.koekepan.Minecraft.behaviours.server.entity;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityEquipmentPacket;
import com.github.steveice10.packetlib.Client;
import com.github.steveice10.packetlib.packet.Packet;
import org.koekepan.VAST.Connection.ClientConnectedInstance;
import org.koekepan.VAST.Packet.Behaviour;
import org.koekepan.VAST.Packet.PacketWrapper;

public class ServerEntityEquipmentPacketBehaviour implements Behaviour<Packet> {
    private ClientConnectedInstance clientInstance;
//    private IServerSession serverSession;

    private ServerEntityEquipmentPacketBehaviour() {
    }

    public ServerEntityEquipmentPacketBehaviour(ClientConnectedInstance clientInstance) {
        this.clientInstance = clientInstance;
//        this.serverSession = serverSession;
    }

    @Override
    public void process(Packet packet) {
        ServerEntityEquipmentPacket serverEntityEquipmentPacket = (ServerEntityEquipmentPacket) packet;

        if (clientInstance.getEntityID() != serverEntityEquipmentPacket.getEntityId()){
            PacketWrapper.setProcessed(packet, true);
//            clientInstance.sendPacketToClient(packet);
        } else {
            PacketWrapper packetWrapper = PacketWrapper.getPacketWrapper(packet);
            if (packetWrapper.PlayerSpecific() != null){
                if (packetWrapper.PlayerSpecific().equals(clientInstance.getUsername())){
                    PacketWrapper.setProcessed(packet, true);
//                clientInstance.sendPacketToClient(packet);
                } else {
//                    SPSConnection.playerSpecificPacketMap.remove(packet);
//                    PacketSender.removePacketFromQueue(packet);
//                    Logger.log(this, Logger.Level.WARN, new String[]{"Entity", "entityEquipment", "behaviour"}, "Entity equipment not forwarder to client.");
                    clientInstance.getPacketSender().removePacket(packet);
                    return;
                }
                return;
            }
        }
    }
}
