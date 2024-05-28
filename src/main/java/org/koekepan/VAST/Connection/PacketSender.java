package org.koekepan.VAST.Connection;

import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.packet.Packet;
import org.koekepan.Performance.PacketCapture;
import org.koekepan.VAST.Connection.PacketSenderRunnables.ClientSender;
import org.koekepan.VAST.Connection.PacketSenderRunnables.ServerSender;
import org.koekepan.VAST.Packet.ExecutorServiceSingleton;
import org.koekepan.VAST.Packet.PacketWrapper;
import org.koekepan.VAST.Packet.SPSPacket;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.*;

import static org.koekepan.VAST.Connection.ClientConnectedInstance.clientInstances_PacketSenders;
import static org.koekepan.VAST.Packet.PacketWrapper.packetWrapperMap;

public class PacketSender implements Runnable { // This is the packet sender, it sends packets to the VAST_COM server and the Client (Clientbound and Serverbound)
    public final ConcurrentHashMap<Integer, PacketWrapper> clientboundPacketQueue = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<Integer, PacketWrapper> serverboundPacketQueue = new ConcurrentHashMap<>();

//    public final ConcurrentHashMap
//    private int queueNumberClientbound = 0;    // The queue number of the next packet to be sent to the client
//    public int queueNumberServerbound = 0;    // The queue number of the next packet to be sent to the server
    public int queueNumberClientboundLast = 0;    // The queue number of the last packet in the queue
    public int queueNumberServerboundLast = 0;    // The queue number of the last packet in the queue
    private Session clientSession;

    private ServerSender serverSender;
    private ClientSender clientSender;

    public PacketSender() {
    }

    public ClientSender getClientSender() {
        return clientSender;
    }

    public ServerSender getServerSender() {
        return serverSender;
    }

    public void start() {
        startServerSender();
        startClientSender();
    }

    private ScheduledExecutorService packetExecutor;
    private ScheduledExecutorService packetExecutor2;

    ScheduledExecutorService executorService = ExecutorServiceSingleton.getInstance();
    private Future<?> serverSenderFuture;
    private Future<?> clientSenderFuture;

    public void startServerSender() {
        this.serverSender = new ServerSender(this, clientInstances_PacketSenders.get(this).getVastConnection());
        packetExecutor = Executors.newSingleThreadScheduledExecutor();
        packetExecutor.scheduleAtFixedRate(serverSender, 0, 1, TimeUnit.MILLISECONDS);
//        serverSenderFuture= executorService.scheduleAtFixedRate(serverSender, 0, 1, TimeUnit.MILLISECONDS);
    }

    public void startClientSender() {
        this.clientSender = new ClientSender(this, this.clientSession);
        packetExecutor2 = Executors.newSingleThreadScheduledExecutor();
        packetExecutor2.scheduleAtFixedRate(clientSender, 0, 1, TimeUnit.MILLISECONDS);
//        clientSenderFuture = executorService.scheduleAtFixedRate(clientSender, 0, 1, TimeUnit.MILLISECONDS);
    }

    public void stopServerSender() {
        if (packetExecutor != null) {
            packetExecutor.shutdown();
            packetExecutor = null;
        } else {
            System.out.println("PacketSender.stopServerSender: packetExecutor is null");
        }
//        if (serverSenderFuture != null) {
//            serverSenderFuture.cancel(true);
//            serverSenderFuture = null;
//        }
    }

    public void stopClientSender() {
        if (packetExecutor2 != null) {
            packetExecutor2.shutdown();
            packetExecutor2 = null;
        } else {
            System.out.println("PacketSender.stopClientSender: packetExecutor2 is null");
        }
//        if (clientSenderFuture != null) {
//            clientSenderFuture.cancel(true);
//            clientSenderFuture = null;
//        }
    }

    public void addClientboundPacket(Packet packet) {
        PacketWrapper packetWrapper = PacketWrapper.getPacketWrapper(packet);

        if (packetWrapper != null) {
            packetWrapper.clientBound = true;
//        synchronized (PacketSender.class) { // Synchronize on the class object if static fields are being modified
//            queueNumberClientboundLast++;
            packetWrapper.queueNumber = ++queueNumberClientboundLast;
            clientboundPacketQueue.put(queueNumberClientboundLast, packetWrapper);
//        }
            clientInstances_PacketSenders.get(this).getPacketHandler().addPacket(packetWrapper);
        } else {
            System.out.println("ERROR: PacketSender.addClientboundPacket: packetWrapper is null");
        }


        // ConsoleIO.println("PacketSender.addClientboundPacket: " + packet.getClass().getSimpleName());
    }

    public void addServerboundPacket(Packet packet) {
        PacketWrapper packetWrapper = new PacketWrapper(packet);
        packetWrapper.clientBound = false;
        packetWrapperMap.put(packet, packetWrapper);

//        System.out.println("PacketSender.addServerboundPacket: " + packet.getClass().getSimpleName());
        packetWrapper.queueNumber = ++queueNumberServerboundLast;
        packetWrapper.unique_id = "SB" + UUID.randomUUID().toString().substring(0, 4) + queueNumberServerboundLast;
        serverboundPacketQueue.put(queueNumberServerboundLast, packetWrapper);

        clientInstances_PacketSenders.get(this).getPacketHandler().addPacket(packetWrapper);
        PacketCapture.log(packet.getClass().getSimpleName() + "_" + packetWrapper.unique_id, PacketCapture.LogCategory.SERVERBOUND_IN);
    }

    public void setClientSession(Session session) {
        this.clientSession = session;
    }

//    public static boolean isPacketInClientboundQueue(Packet packet) {
//        return clientboundPacketQueue.containsKey(PacketWrapper.get_QueueNumber(packet));
//    }

    @Override
    public void run() {
////        int lastCheckedQueueNumber = -1; // Last checked queue number for comparison
//        long timeAdded = System.currentTimeMillis();
//        boolean processClientbound = true; // Flag to alternate between clientbound and serverbound
//
//        try {
//            while (!clientboundPacketQueue.isEmpty() || !serverboundPacketQueue.isEmpty()) {
//                if (clientboundPacketQueue.isEmpty()) {processClientbound = false;} else if (serverboundPacketQueue.isEmpty()) {processClientbound = true;}
////                System.out.println("PacketSender.run: clientBoundQueue: " + clientboundPacketQueue.size() + " serverBoundQueue: " + serverboundPacketQueue.size());
////                System.out.println("PacketSender.run: queueNumberClientbound: " + queueNumberClientbound + " queueNumberServerbound: " + queueNumberServerbound);
////                System.out.println("PacketSender.run: processClientbound: " + processClientbound);
//
//                if (processClientbound && clientboundPacketQueue.containsKey(queueNumberClientbound)) {
//                    final PacketWrapper wrapper = PacketWrapper.getPacketWrapperByQueueNumber(clientboundPacketQueue, queueNumberClientbound);
////                    System.out.println("Packet being tested: " + wrapper.getPacket().getClass().getSimpleName() + " with queue number: " + queueNumberClientbound + " and isProcessed: " + wrapper.isProcessed);
//                    if (wrapper != null && wrapper.isProcessed && this.clientSession != null) {
////                        if (wrapper.getPacket().getClass().getSimpleName().equals("LoginSuccessPacket")) tempBool = false; // TODO: Remove this line!
//                        clientSession.send(wrapper.getPacket());
//                        System.out.println("PacketSender.run: " + wrapper.getPacket().getClass().getSimpleName() + " sent to client");
//
////                        clientboundPacketQueue.remove(queueNumberClientbound);
//                        this.removePacket(wrapper.getPacket());
//                        timeAdded = System.currentTimeMillis(); // Reset time after sending a packet
//                        queueNumberClientbound++;
//
//                        processClientbound = false; // Next iteration will process serverbound
//                    }
//                } else if (!processClientbound && serverboundPacketQueue.containsKey(queueNumberServerbound)) {
//                    final PacketWrapper wrapper = PacketWrapper.getPacketWrapperByQueueNumber(serverboundPacketQueue, queueNumberServerbound);
//
//                    if ((wrapper != null) && (wrapper.isProcessed) && (clientInstances_PacketSenders.get(this).getVastConnection() != null)) {
//                        clientInstances_PacketSenders.get(this).getVastConnection().publish(wrapper.getSPSPacket());
////                        serverboundPacketQueue.remove(queueNumberServerbound);
//                        this.removePacket(wrapper.getPacket());
//                        timeAdded = System.currentTimeMillis(); // Reset time after sending a packet
//                        queueNumberServerbound++;
//
//                        processClientbound = true; // Next iteration will process serverbound
//                    }
//                }
//
//                // Handle timeout for both queues
//                long currentTime = System.currentTimeMillis();
//                if (currentTime - timeAdded > 100) { // TODO: Change back to 100 when problem found (could be 50)
////                    System.out.println("PacketSender.run: <TIMED OUT> difference: " + (currentTime - timeAdded) + " processClientbound: " + processClientbound + " clientSession: " + (clientSession != null));
////                    System.out.println("PacketSender.run: <TIMED OUT> Amount of packets in clientbound queue: " + clientboundPacketQueue.size() + " and serverbound queue: " + serverboundPacketQueue.size());
//                    if (clientboundPacketQueue.containsKey(queueNumberClientbound) && processClientbound) {
//                        final PacketWrapper wrapper = PacketWrapper.getPacketWrapperByQueueNumber(clientboundPacketQueue, queueNumberClientbound);
//
//                        if (!clientboundPacketQueue.get(queueNumberClientbound).getPacket().getClass().getSimpleName().equals("ServerChunkDataPacket")) {
//                            System.out.println("PacketSender.run: <TIMED OUT> (clientbound) Wrapper is: " + wrapper.getPacket().getClass().getSimpleName() + " and isProcessed: " + wrapper.isProcessed + " and ClientSession is null: " + (clientSession == null));
//                            this.removePacket(clientboundPacketQueue.get(queueNumberClientbound).getPacket());
//                            queueNumberClientbound++;
//                        }
//                    }
//                    if (serverboundPacketQueue.containsKey(queueNumberServerbound) && !processClientbound) {
//                        final PacketWrapper wrapper = PacketWrapper.getPacketWrapperByQueueNumber(serverboundPacketQueue, queueNumberServerbound);
//                        System.out.println("PacketSender.run: <TIMED OUT> (serverbound) Wrapper is: " + wrapper.getPacket().getClass().getSimpleName() + " and isProcessed: " + wrapper.isProcessed + " and VastConnection is null: " + (clientInstances_PacketSenders.get(this).getVastConnection() == null));
//
//                        this.removePacket(serverboundPacketQueue.get(queueNumberServerbound).getPacket());
//                        queueNumberServerbound++;
//                    }
//                    timeAdded = currentTime; // Reset time after handling timeouts
//                }
//
//                // Check if the queue number has reached or exceeded the last queue number and increment until a packet is found
//                if (!clientboundPacketQueue.containsKey(queueNumberClientbound) && queueNumberClientbound <= queueNumberClientboundLast) {
//                    while (!clientboundPacketQueue.containsKey(queueNumberClientbound)) {
//                        queueNumberClientbound++;
//                        // Check if queueNumberClientbound has reached or exceeded the last queue number
//                        if (queueNumberClientbound > queueNumberClientboundLast) {
//                            break; // Exit the loop if we have reached the end of the queue
//                        }
//                    }
//                }
//
//                if (!serverboundPacketQueue.containsKey(queueNumberServerbound) && queueNumberServerbound <= queueNumberServerboundLast) {
//                    while (!serverboundPacketQueue.containsKey(queueNumberServerbound)) {
//                        queueNumberServerbound++;
//                        // Check if queueNumberServerbound has reached or exceeded the last queue number
//                        if (queueNumberServerbound > queueNumberServerboundLast) {
//                            break; // Exit the loop if we have reached the end of the queue
//                        }
//                    }
//                }
//
//            }
//        } catch (Exception e) {
//            // Log the exception
//        }
    }

    public void removePacket(Packet packet) {
        PacketWrapper packetWrapper = packetWrapperMap.get(packet);
        if (packetWrapper != null) {
            if (packetWrapper.clientBound) {
//                synchronized (clientboundPacketQueue) {
                    clientboundPacketQueue.remove(packetWrapper.queueNumber);
//                }
            } else {
//                synchronized (serverboundPacketQueue) {
                    serverboundPacketQueue.remove(packetWrapper.queueNumber);
//                }
            }
        }

        PacketCapture.log(packet.getClass().getSimpleName() + "_" + PacketWrapper.get_unique_id(packet), PacketCapture.LogCategory.DELETED_PACKETS);
        PacketWrapper.removePacketWrapper(packet);
    }

    public void stop() {
        stopServerSender();
        stopClientSender();
        clientboundPacketQueue.clear();
        serverboundPacketQueue.clear();
//        packetWrapperMap.clear();
    }
}
