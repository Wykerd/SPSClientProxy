package org.koekepan.VAST.Connection.PacketSenderRunnables;

import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.packet.Packet;
import org.koekepan.Performance.PacketCapture;
import org.koekepan.VAST.Connection.ClientConnectedInstance;
import org.koekepan.VAST.Connection.PacketSender;
import org.koekepan.VAST.CustomPackets.PINGPONG;
import org.koekepan.VAST.Packet.PacketWrapper;

import java.util.concurrent.ConcurrentHashMap;

import static org.koekepan.VAST.Connection.ClientConnectedInstance.clientInstances_PacketSenders;
public class ClientSender implements Runnable{

    private PacketSender packetSender;
    private Session clientSession;
    public ClientSender(PacketSender packetSender, Session clientSession) {
        this.packetSender = packetSender;
        this.clientSession = clientSession;
    }
    private int queueNumberClientbound = 0;

    public int getQueueNumberClientbound() {
        return queueNumberClientbound;
    }

    @Override
    public void run() {
        try {
            long timeAdded = System.currentTimeMillis();

            while (!packetSender.clientboundPacketQueue.isEmpty()) {
                boolean dosleep = true;
//                Thread.sleep(5);
//                System.out.println("Length of queue for client " + clientInstances_PacketSenders.get(this.packetSender).getUsername()  + ": " + packetSender.clientboundPacketQueue.size());
//            while (true) {

//                int queueNumberClientboundLast = packetSender.queueNumberClientboundLast -1;
                ConcurrentHashMap<Integer, PacketWrapper> localCopy = new ConcurrentHashMap<>(packetSender.clientboundPacketQueue);
                boolean clientboundPacketQueueContainsKey = packetSender.clientboundPacketQueue.containsKey(queueNumberClientbound);
                PacketWrapper wrapper = null;

//                System.out.println("ClientSender.run: packetSender.clientboundPacketQueue.size() = " + packetSender.clientboundPacketQueue.size() + " and queueNumberClientbound = " + queueNumberClientbound + " and clientboundPacketQueueContainsKey = " + clientboundPacketQueueContainsKey + " and clientboundPacketQueueContainsKey = " + clientboundPacketQueueContainsKey + " clientboundpacketqueuelast = " + queueNumberClientboundLast);

                if (clientboundPacketQueueContainsKey) {
//                    wrapper = PacketWrapper.getPacketWrapperByQueueNumber(packetSender.clientboundPacketQueue, queueNumberClientbound);
//                    if ((queueNumberClientbound < 1050 && queueNumberClientbound > 1030) || (queueNumberClientbound < 2030 && queueNumberClientbound > 2010)) {
//                        System.out.println("ClientSender.run: <TIMED OUT DUE TO ME> (clientbound) Wrapper is: " + wrapper.getPacket().getClass().getSimpleName() + " and isProcessed: " + wrapper.isProcessed);
////                    queueNumberClientbound ++;
//                        packetSender.removePacket(wrapper.getPacket());
//                        queueNumberClientbound++;
//                        timeAdded = System.currentTimeMillis();
//                    }

//                    System.out.println("ClientSender.run: wrapper = <" + wrapper.getPacket().getClass().getSimpleName() + "> and isProcessed = " + wrapper.isProcessed);
                    wrapper = PacketWrapper.getPacketWrapperByQueueNumber(packetSender.clientboundPacketQueue, queueNumberClientbound);
                        if (wrapper != null && wrapper.isProcessed && this.clientSession != null) {

                            Packet packet = wrapper.getPacket();

                            if (packet == null) {
                                System.out.println("ClientSender.run: Packet is null");
                                return;
                            }
                            if (this.clientSession.isConnected() && !(packet instanceof PINGPONG)) {
                                this.clientSession.send(wrapper.getPacket());
                            }
                            PacketCapture.log(clientInstances_PacketSenders.get(packetSender).getUsername(),wrapper.getPacket().getClass().getSimpleName() + "_" + PacketWrapper.get_unique_id(wrapper.getPacket()), PacketCapture.LogCategory.CLIENTBOUND_OUT);
////                        System.out.println("ClientSender.run: " + wrapper.getPacket().getClass().getSimpleName() + " sent to client: " + clientInstances_PacketSenders.get(this.packetSender).getUsername());
//
                            packetSender.removePacket(wrapper.getPacket());
                            timeAdded = System.currentTimeMillis(); // Reset time after sending a packet
                            queueNumberClientbound++;
                            dosleep = false;
//                            break;
                        }

                }
                // Handle timeout for both queues
                long currentTime = System.currentTimeMillis();

//                System.out.println("Current Time - Time Added: " + (currentTime - timeAdded));

                if ((currentTime - timeAdded > 250)) { // TODO: Change back to 100 when problem found (could be 50) - This if seems to break the system? (could be 20 for single client, multi client it should be more)
//                    System.out.println("TimeOUT");
//                    System.out.println("Current queue number: " + queueNumberClientbound);
                    clientboundPacketQueueContainsKey = packetSender.clientboundPacketQueue.containsKey(queueNumberClientbound);
                    if (clientboundPacketQueueContainsKey) {
                        wrapper = PacketWrapper.getPacketWrapperByQueueNumber(packetSender.clientboundPacketQueue, queueNumberClientbound);
//                        System.out.println("PacketSender.run: <TIMED OUT> (clientbound) 1");
                        if (!wrapper.getPacket().getClass().getSimpleName().equals("ServerChunkDataPacket")) {
                            System.out.println("ClientSender.run: <TIMED OUT> (clientbound) Wrapper is: " + wrapper.getPacket().getClass().getSimpleName() + " and isProcessed: " + wrapper.isProcessed + " packet: " + wrapper.getPacket().toString() );
//                            PacketCapture.log(wrapper.getPacket().getClass().getSimpleName() + "_" + PacketWrapper.get_unique_id(wrapper.getPacket()), PacketCapture.LogCategory.DELETED_PACKETS_TIME);
                            packetSender.removePacket(wrapper.getPacket());
                            queueNumberClientbound++;
                            dosleep = false;
                            timeAdded = currentTime; // Reset time after handling timeouts
                        }
                        else{
//                            PacketCapture.log(wrapper.getPacket().getClass().getSimpleName() + "_" + PacketWrapper.get_unique_id(wrapper.getPacket()), PacketCapture.LogCategory.CHUNK_DELETED_PACKETS_TIME);
                        }
                    }
                }

                // Check if the queue number has reached or exceeded the last queue number and increment until a packet is found
                if (queueNumberClientbound < packetSender.queueNumberClientboundLast && !packetSender.clientboundPacketQueue.containsKey(queueNumberClientbound)) {
//                    while (!packetSender.clientboundPacketQueue.containsKey(queueNumberClientbound)) {
//                    while (queueNumberClientbound < packetSender.queueNumberClientboundLast && !packetSender.clientboundPacketQueue.containsKey(queueNumberClientbound)) {
//                        System.out.println("Stuck in loop: User: " + clientInstances_PacketSenders.get(this.packetSender).getUsername() + " queueNumberClientbound: " + queueNumberClientbound + " queueNumberClientboundLast: " + queueNumberClientboundLast);
                        queueNumberClientbound++;
                        dosleep = false;
                        timeAdded = currentTime;
//                    }
//                    if (!packetSender.clientboundPacketQueue.containsKey(queueNumberClientbound)) {
//                        queueNumberClientbound++;
//                        break;
//                    }
//                        System.out.println("tewst");
//                    Thread.sleep(1000);
//                    break;
//                        timeAdded = currentTime; // Reset time after handling timeouts

//                        System.out.println("ClientSender.run: <INCREMENT> (clientbound) 1 for username: " + clientInstances_PacketSenders.get(this.packetSender).getUsername());
//                         Check if queueNumberClientbound has reached or exceeded the last queue number
//                        if (queueNumberClientbound > queueNumberClientboundLast) {
//                            System.out.println("ClientSender.run: <BREAK> (clientbound) 1");
//                            break; // Exit the loop if we have reached the end of the queue
//                        }
//                    }
                }

                if (dosleep) {
                    Thread.sleep(1);
                }
            }
        } catch (Exception e) {
            System.out.println("ClientSender.run: Exception: " + e.getMessage());
            // Log the exception
        }
    }
}
