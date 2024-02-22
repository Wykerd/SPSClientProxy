package org.koekepan.VAST.Connection.PacketSenderRunnables;

import com.github.steveice10.packetlib.Session;
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
            int queueNumberClientboundLast = packetSender.queueNumberClientboundLast;

            while (!packetSender.clientboundPacketQueue.isEmpty()) {
                boolean clientboundPacketQueueContainsKey = packetSender.clientboundPacketQueue.containsKey(queueNumberClientbound);
                PacketWrapper wrapper = null;

                if (clientboundPacketQueueContainsKey) {
                    wrapper = PacketWrapper.getPacketWrapperByQueueNumber(packetSender.clientboundPacketQueue, queueNumberClientbound);
                    if (wrapper != null && wrapper.isProcessed && this.clientSession != null) {

                        this.clientSession.send(wrapper.getPacket());
                        System.out.println("PacketSender.run: " + wrapper.getPacket().getClass().getSimpleName() + " sent to client");

                        packetSender.removePacket(wrapper.getPacket());
                        timeAdded = System.currentTimeMillis(); // Reset time after sending a packet
                        queueNumberClientbound++;
                    }
                }

                // Handle timeout for both queues
                long currentTime = System.currentTimeMillis();
                if (currentTime - timeAdded > 50) { // TODO: Change back to 100 when problem found (could be 50) - This if seems to break the system?
                    if (clientboundPacketQueueContainsKey) {
                        System.out.println("PacketSender.run: <TIMED OUT> (clientbound) 1");
                        if (wrapper == null) {
                            System.out.println("PacketSender.run: <TIMED OUT> (clientbound) 2");
                            break;
                        }
                        if (!wrapper.getPacket().getClass().getSimpleName().equals("ServerChunkDataPacket")) {
                            System.out.println("PacketSender.run: <TIMED OUT> (clientbound) Wrapper is: " + wrapper.getPacket().getClass().getSimpleName() + " and isProcessed: " + wrapper.isProcessed);
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
                        // Check if queueNumberClientbound has reached or exceeded the last queue number
                        if (queueNumberClientbound > queueNumberClientboundLast) {
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
