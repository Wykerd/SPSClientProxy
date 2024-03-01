package org.koekepan.VAST.Connection.PacketSenderRunnables;

import com.github.steveice10.packetlib.Session;
import org.koekepan.Performance.PacketCapture;
import org.koekepan.VAST.Connection.ClientConnectedInstance;
import org.koekepan.VAST.Connection.PacketSender;
import org.koekepan.VAST.Packet.PacketWrapper;

import static org.koekepan.VAST.Connection.ClientConnectedInstance.clientInstances_PacketSenders;
public class ClientSender implements Runnable{

    private PacketSender packetSender;
    private Session clientSession;
    public ClientSender(PacketSender packetSender, Session clientSession) {
        this.packetSender = packetSender;
        this.clientSession = clientSession;
    }
    private int queueNumberClientbound = 0;
    @Override
    public void run() {
        try {
            long timeAdded = System.currentTimeMillis();

            while (!packetSender.clientboundPacketQueue.isEmpty()) {
                int queueNumberClientboundLast = packetSender.queueNumberClientboundLast;
                boolean clientboundPacketQueueContainsKey = packetSender.clientboundPacketQueue.containsKey(queueNumberClientbound);
                PacketWrapper wrapper = null;

//                System.out.println("ClientSender.run: packetSender.clientboundPacketQueue.size() = " + packetSender.clientboundPacketQueue.size() + " and queueNumberClientbound = " + queueNumberClientbound + " and clientboundPacketQueueContainsKey = " + clientboundPacketQueueContainsKey + " and clientboundPacketQueueContainsKey = " + clientboundPacketQueueContainsKey + " clientboundpacketqueuelast = " + queueNumberClientboundLast);

                if (clientboundPacketQueueContainsKey) {
                    wrapper = PacketWrapper.getPacketWrapperByQueueNumber(packetSender.clientboundPacketQueue, queueNumberClientbound);

//                    System.out.println("ClientSender.run: wrapper = <" + wrapper.getPacket().getClass().getSimpleName() + "> and isProcessed = " + wrapper.isProcessed);

                    if (wrapper != null && wrapper.isProcessed && this.clientSession != null) {

                        this.clientSession.send(wrapper.getPacket());
                        PacketCapture.log(wrapper.getPacket().getClass().getSimpleName() + "_" + PacketWrapper.get_unique_id(wrapper.getPacket()), PacketCapture.LogCategory.CLIENTBOUND_OUT);
//                        System.out.println("ClientSender.run: " + wrapper.getPacket().getClass().getSimpleName() + " sent to client: " + clientInstances_PacketSenders.get(this.packetSender).getUsername());

                        packetSender.removePacket(wrapper.getPacket());
                        timeAdded = System.currentTimeMillis(); // Reset time after sending a packet
                        queueNumberClientbound++;
                    }
                }

                // Handle timeout for both queues
                long currentTime = System.currentTimeMillis();
                if (currentTime - timeAdded > 20) { // TODO: Change back to 100 when problem found (could be 50) - This if seems to break the system?
                    if (clientboundPacketQueueContainsKey) {
//                        System.out.println("PacketSender.run: <TIMED OUT> (clientbound) 1");
                        if (!wrapper.getPacket().getClass().getSimpleName().equals("ServerChunkDataPacket")) {
                            System.out.println("ClientSender.run: <TIMED OUT> (clientbound) Wrapper is: " + wrapper.getPacket().getClass().getSimpleName() + " and isProcessed: " + wrapper.isProcessed);
                            packetSender.removePacket(wrapper.getPacket());
                            queueNumberClientbound++;
                        }
                        timeAdded = currentTime; // Reset time after handling timeouts
                    }
                }

                // Check if the queue number has reached or exceeded the last queue number and increment until a packet is found
                if (!packetSender.clientboundPacketQueue.containsKey(queueNumberClientbound) && queueNumberClientbound <= queueNumberClientboundLast) {
                    while (!packetSender.clientboundPacketQueue.containsKey(queueNumberClientbound)) {
                        queueNumberClientbound++;
//                        System.out.println("ClientSender.run: <INCREMENT> (clientbound) 1");
                        // Check if queueNumberClientbound has reached or exceeded the last queue number
                        if (queueNumberClientbound > queueNumberClientboundLast) {
//                            System.out.println("ClientSender.run: <BREAK> (clientbound) 1");
                            break; // Exit the loop if we have reached the end of the queue
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("ClientSender.run: Exception: " + e.getMessage());
            // Log the exception
        }
    }
}
