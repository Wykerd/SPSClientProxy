package org.koekepan.VAST.Packet;

import com.github.steveice10.packetlib.packet.Packet;
import org.koekepan.Minecraft.behaviours.ClientBoundPacketBehaviours;
import org.koekepan.Minecraft.behaviours.ServerBoundPacketBehaviours;
import org.koekepan.VAST.Connection.ClientConnectedInstance;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PacketHandler implements Runnable {

//    Deque<PacketWrapper> packetQueue = new ArrayDeque<PacketWrapper>();
    BehaviourHandler<Packet> behaviourHandler;
//    List<Thread> threads = new ArrayList<>(); // List to store all threads

    private final ExecutorService executorService;


    public PacketHandler(ClientConnectedInstance clientInstance) {
        this.behaviourHandler = new BehaviourHandler<Packet>();
        ClientBoundPacketBehaviours clientBoundPacketBehaviours = new ClientBoundPacketBehaviours(clientInstance);
        clientBoundPacketBehaviours.registerForwardingBehaviour();

        ServerBoundPacketBehaviours serverBoundPacketBehaviours = new ServerBoundPacketBehaviours(clientInstance);
        serverBoundPacketBehaviours.registerForwardingBehaviour();

        //Merge the behaviours
        BehaviourHandler<Packet> behaviourHandler = BehaviourHandler.mergeBehaviourHandlers(clientBoundPacketBehaviours, serverBoundPacketBehaviours);

        this.setBehaviours(behaviourHandler);

        // Initialize the thread pool with a fixed size. Adjust size based on your application's requirements and hardware capabilities.
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public void addPacket(PacketWrapper packetWrapper) {
        if (packetWrapper == null) {
            System.out.println("PacketHandler::addPacket => PacketWrapper is null");
            return;
        }
        if (packetWrapper.isProcessed) {
            System.out.println("PacketHandler::addPacket => Packet is already processed");
            return;
        }
        Packet packet = packetWrapper.getPacket();
        if (packet == null) {
            System.out.println("PacketHandler::addPacket => Packet is null");
            return;
        }
        executorService.submit(() -> {
            try {
                this.behaviourHandler.process(packet);
                // Optionally, add callback or future to handle post-processing
            } catch (Exception e) {
                e.printStackTrace(); // Consider a more robust error handling approach
            }
        });
    }
    public void setBehaviours(BehaviourHandler<Packet> behaviourHandler) {
        this.behaviourHandler = behaviourHandler;
    }

    @Override
    public void run() {

        try {

            // New thread per process


//            if (!packetQueue.isEmpty()) {
//                PacketWrapper packetWrapper = packetQueue.poll();
//                if (packetWrapper != null) {
//                    if (!packetWrapper.isProcessed) {
//                        Packet packet = packetWrapper.getPacket();
//                        if (packet != null) {
//                            new Thread(() -> {
//                            this.behaviourHandler.process(packet);
//                            }).start();
//                        }
//                    }
//                }
//            }


//            if (!packetQueue.isEmpty()) {
//                PacketWrapper packetWrapper = packetQueue.poll();
//                if (packetWrapper != null) {
//                    if (!packetWrapper.isProcessed) {
//                        Packet packet = packetWrapper.getPacket();
//                        if (packet != null) {
//                            this.behaviourHandler.process(packet);
//                        }
//                    }
//                }
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        // Stop all threads
        executorService.shutdown();
    }
}
