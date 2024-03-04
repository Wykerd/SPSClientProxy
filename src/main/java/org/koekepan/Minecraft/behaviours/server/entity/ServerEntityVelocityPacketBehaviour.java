package org.koekepan.Minecraft.behaviours.server.entity;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityVelocityPacket;
import com.github.steveice10.packetlib.packet.Packet;
import org.koekepan.VAST.Connection.ClientConnectedInstance;
import org.koekepan.VAST.Packet.Behaviour;
import org.koekepan.VAST.Packet.PacketWrapper;

public class ServerEntityVelocityPacketBehaviour implements Behaviour<Packet> {
    private ClientConnectedInstance clientInstance;
//    private IServerSession serverSession;

    private ServerEntityVelocityPacketBehaviour() {
    }

    public ServerEntityVelocityPacketBehaviour(ClientConnectedInstance clientInstance) {
        this.clientInstance = clientInstance;
//        this.serverSession = serverSession;
    }

    @Override
    public void process(Packet packet) {
        ServerEntityVelocityPacket serverEntityVelocityPacket = (ServerEntityVelocityPacket) packet;

//        ConsoleIO.println("Block changed at position: " + blockRecord.getPosition().getX() + ", " + blockRecord.getPosition().getY() + ", " + blockRecord.getPosition().getZ());        
        if (clientInstance.getEntityID() != serverEntityVelocityPacket.getEntityId()){
//            clientInstance.sendPacketToClient(packet);
            PacketWrapper.setProcessed(packet, true);
        } else {
            PacketWrapper packetWrapper = PacketWrapper.getPacketWrapper(packet);
            if (packetWrapper.getPlayerSpecific() != null){

                if (packetWrapper.getPlayerSpecific().equals(clientInstance.getUsername())){
//                clientInstance.sendPacketToClient(packet);
                    PacketWrapper.setProcessed(packet, true);
                } else {
//                SPSConnection.playerSpecificPacketMap.remove(packet);
//                PacketSender.removePacketFromQueue(packet);
//                Logger.log(this, Logger.Level.WARN, new String[]{"Entity", "entityVelocity", "behaviour"}, "Entity velocity not forwarded to client.");
                    clientInstance.getPacketSender().removePacket(packet);
                    return;
                }
                return;
            } else {
                clientInstance.getPacketSender().removePacket(packet);
//                System.out.println("ServerEntityVelocityPacket not forwarded to client, because it is not player specific.");
            }
        }
    }
}
