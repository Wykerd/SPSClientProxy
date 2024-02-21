package org.koekepan.VAST.Connection;

import com.github.steveice10.packetlib.Session;
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
    private Session clientSession;


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

        clientInstances_PacketSenders.get(this).getPacketHandler().addPacket(packetWrapper);

//        System.out.println("PacketSender.addServerboundPacket: " + packet.getClass().getSimpleName());
        serverboundPacketQueue.put(++queueNumberServerboundLast, packetWrapper);
        packetWrapper.queueNumber = queueNumberServerboundLast;
        packetWrapper.unique_id = "SB" + UUID.randomUUID().toString().substring(0, 4) + queueNumberServerboundLast;
        packetWrapperMap.put(packet, packetWrapper);
    }

    public void setClientSession(Session session) {
        this.clientSession = session;
    }

//    public static boolean isPacketInClientboundQueue(Packet packet) {
//        return clientboundPacketQueue.containsKey(PacketWrapper.get_QueueNumber(packet));
//    }


    /// Serverbound: clientInstances_PacketSenders.get(this).getVastConnection().publish(spsPacket);
    @Override
    public void run() {
        int lastCheckedQueueNumber = -1; // Last checked queue number for comparison
        long timeAdded = 0;
        boolean processClientbound = true; // Flag to alternate between clientbound and serverbound

        try {
            while (!clientboundPacketQueue.isEmpty() || !serverboundPacketQueue.isEmpty()) {
                if (clientboundPacketQueue.isEmpty()) processClientbound = false;

                if (processClientbound && clientboundPacketQueue.containsKey(queueNumberClientbound)) {
                    final PacketWrapper wrapper = PacketWrapper.getPacketWrapperByQueueNumber(clientboundPacketQueue, queueNumberClientbound);
                    if (wrapper != null && wrapper.isProcessed && this.clientSession != null) {
                        System.out.println("PacketSender.run: Sending " + wrapper.getPacket().getClass().getSimpleName() + " to client");
                        clientSession.send(wrapper.getPacket());
                        System.out.println("PacketSender.run: " + wrapper.getPacket().getClass().getSimpleName() + " sent to client");
                        clientboundPacketQueue.remove(queueNumberClientbound);
                        queueNumberClientbound++;
                    }
                    processClientbound = false; // Next iteration will process serverbound
                } else if (!processClientbound && serverboundPacketQueue.containsKey(queueNumberServerbound)) {
                    final PacketWrapper wrapper = PacketWrapper.getPacketWrapperByQueueNumber(serverboundPacketQueue, queueNumberServerbound);

                    if ((wrapper != null) && (wrapper.isProcessed) && (clientInstances_PacketSenders.get(this).getVastConnection() != null)) {
                        clientInstances_PacketSenders.get(this).getVastConnection().publish(wrapper.getSPSPacket());
//                        serverboundPacketQueue.remove(queueNumberServerbound);
                        this.removePacket(wrapper.getPacket());

                        queueNumberServerbound++;
                    }
                    processClientbound = true; // Next iteration will process clientbound
                }

                // Handle timeout for both queues
                long currentTime = System.currentTimeMillis();
                if (currentTime - timeAdded > 100) {
                    if (clientboundPacketQueue.containsKey(queueNumberClientbound)) {
                        clientboundPacketQueue.remove(queueNumberClientbound);
                        queueNumberClientbound++;
                    }
                    if (serverboundPacketQueue.containsKey(queueNumberServerbound)) {
                        serverboundPacketQueue.remove(queueNumberServerbound);
                        queueNumberServerbound++;
                    }
                    timeAdded = currentTime; // Reset time after handling timeouts
                }

                // Ensure alternating starts with clientbound next if both queues are empty
                if (clientboundPacketQueue.isEmpty() && serverboundPacketQueue.isEmpty()) {
                    processClientbound = true;
                }

                if (!clientboundPacketQueue.containsKey(queueNumberClientbound) && queueNumberClientbound <= queueNumberClientboundLast) {
                    while (!clientboundPacketQueue.containsKey(queueNumberClientbound)) {
                        queueNumberClientbound++;
                        // Check if queueNumberClientbound has reached or exceeded the last queue number
                        if (queueNumberClientbound > queueNumberClientboundLast) {
                            break; // Exit the loop if we have reached the end of the queue
                        }
                    }
                }

                if (!serverboundPacketQueue.containsKey(queueNumberServerbound) && queueNumberServerbound <= queueNumberServerboundLast) {
                    while (!serverboundPacketQueue.containsKey(queueNumberServerbound)) {
                        queueNumberServerbound++;
                        // Check if queueNumberServerbound has reached or exceeded the last queue number
                        if (queueNumberServerbound > queueNumberServerboundLast) {
                            break; // Exit the loop if we have reached the end of the queue
                        }
                    }
                }

            }
        } catch (Exception e) {
            // Log the exception
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
