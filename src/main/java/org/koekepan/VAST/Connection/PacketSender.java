package org.koekepan.VAST.Connection;

import com.github.steveice10.packetlib.packet.Packet;
import org.koekepan.VAST.Packet.PacketWrapper;
import org.koekepan.VAST.Packet.SPSPacket;

import java.util.HashMap;
import java.util.UUID;

import static org.koekepan.VAST.Connection.ClientConnectedInstance.clientInstances_PacketSenders;
import static org.koekepan.VAST.Packet.PacketWrapper.packetWrapperMap;

public class PacketSender implements Runnable { // This is the packet sender, it sends packets to the VAST_COM server and the Client (Clientbound and Serverbound)
    private final HashMap<Integer, PacketWrapper> clientboundPacketQueue = new HashMap<>();
    private final HashMap<Integer, PacketWrapper> serverboundPacketQueue = new HashMap<>();
    private int queueNumberClientbound = 0;    // The queue number of the next packet to be sent to the client
    private int queueNumberServerbound = 0;    // The queue number of the next packet to be sent to the server
    private int queueNumberClientboundLast = 0;    // The queue number of the last packet in the queue
    private int queueNumberServerboundLast = 0;    // The queue number of the last packet in the queue

    public PacketSender() {
    }

//    public static synchronized void addClientboundPacket(Packet packet) {
//        PacketWrapper packetWrapper = new PacketWrapper(packet);
//        packetWrapper.clientBound = true;
//        synchronized (PacketSender.class) { // Synchronize on the class object if static fields are being modified
//            queueNumberClientboundLast++;
//            clientboundPacketQueue.put(queueNumberClientboundLast, packetWrapper);
//        }
//        packetWrapper.queueNumber = queueNumberClientboundLast;
//        packetWrapperMap.put(packet, packetWrapper);
//
//        // ConsoleIO.println("PacketSender.addClientboundPacket: " + packet.getClass().getSimpleName());
//    }

    public void addServerboundPacket(Packet packet) {
        PacketWrapper packetWrapper = new PacketWrapper(packet);
        packetWrapper.clientBound = false;
        serverboundPacketQueue.put(++queueNumberServerboundLast, packetWrapper);
        packetWrapper.queueNumber = queueNumberServerboundLast;
        packetWrapper.unique_id = "SB" + UUID.randomUUID().toString().substring(0, 4) + queueNumberServerboundLast;
        packetWrapperMap.put(packet, packetWrapper);
    }

//    public static boolean isPacketInClientboundQueue(Packet packet) {
//        return clientboundPacketQueue.containsKey(PacketWrapper.get_QueueNumber(packet));
//    }

    @Override
    public void run() {

        try {

//            if (!clientboundPacketQueue.isEmpty()) {
//                PacketWrapper packetWrapper = clientboundPacketQueue.get(++queueNumberClientbound);
//                if (packetWrapper != null) {
//                    if (packetWrapper.isProcessed) {
//                        Packet packet = packetWrapper.getPacket();
//                        if (packet != null) {
//                            VastConnection.getInstance().send(packet);
//                        }
//                    }
//                }
//            }

            if (!serverboundPacketQueue.isEmpty()) {
                PacketWrapper packetWrapper = serverboundPacketQueue.get(++queueNumberServerbound);
                if (packetWrapper != null) {
                    if (packetWrapper.isProcessed) {
                        SPSPacket spsPacket = packetWrapper.getSPSPacket();
                        if (spsPacket != null) {
//                            VastConnection.getInstance().send(packet);
                            clientInstances_PacketSenders.get(this).getVastConnection().publish(spsPacket);
                            removePacket(packetWrapper.getPacket());
                        }
                    }
                }
            }



        } catch (Exception e) {
            System.out.println("PacketSender.run: " + e.getMessage());
        }


    }

    public void removePacket(Packet packet) {
        PacketWrapper packetWrapper = packetWrapperMap.get(packet);
        if (packetWrapper != null) {
            if (packetWrapper.clientBound) {
                clientboundPacketQueue.remove(packetWrapper.queueNumber);
            } else {
                serverboundPacketQueue.remove(packetWrapper.queueNumber);
            }
        }
        PacketWrapper.removePacketWrapper(packet);
    }
}
