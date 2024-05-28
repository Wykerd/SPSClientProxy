package org.koekepan.VAST.Packet;

import com.github.steveice10.packetlib.packet.Packet;
import org.koekepan.Minecraft.behaviours.ClientBoundPacketBehaviours;
import org.koekepan.Minecraft.behaviours.ServerBoundPacketBehaviours;
import org.koekepan.Performance.PacketCapture;
import org.koekepan.VAST.Connection.ClientConnectedInstance;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class PacketHandler implements Runnable {

//    Deque<PacketWrapper> packetQueue = new ArrayDeque<PacketWrapper>();
    BehaviourHandler<Packet> behaviourHandler;
//    List<Thread> threads = new ArrayList<>(); // List to store all threads

    private final ExecutorService executorService;
//    private final Semaphore semaphore;
//    private final BlockingQueue<PacketWrapper> packetQueue;



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
//        this.executorService = Executors.newFixedThreadPool((int)Runtime.getRuntime().availableProcessors());
        this.executorService = ExecutorServiceSingleton.getInstance();
//        this.semaphore = new Semaphore(Runtime.getRuntime().availableProcessors()); // Adjust this value based on your needs
//        this.packetQueue = new LinkedBlockingQueue<>();
    }

    public void addPacket(PacketWrapper packetWrapper) {
        if (packetWrapper == null) {
            System.out.println("PacketHandler::addPacket => PacketWrapper is null");
            return;
        }
        if (packetWrapper.isProcessed) {
//            System.out.println("PacketHandler::addPacket => Packet is already processed");
            return;
        }
        Packet packet = packetWrapper.getPacket();
        if (packet == null) {
            System.out.println("PacketHandler::addPacket => Packet is null");
            return;
        }

//        packetWrapper.isProcessed = true;

//        PacketCapture.log(packet.getClass().getSimpleName() + "_" + packetWrapper.unique_id, PacketCapture.LogCategory.PACKET_BEH_QUEUE);
//        ExecutorServiceSingleton.executeWithTimeout(() -> {
//            try {
//                this.behaviourHandler.process(packet);
//                // Optionally, add callback or future to handle post-processing
//            } catch (Exception e) {
//                e.printStackTrace(); // Consider a more robust error handling approach
//            }
//        }, 300, TimeUnit.MILLISECONDS);

        executorService.submit(() -> {
            try {
                this.behaviourHandler.process(packet);
                // Optionally, add callback or future to handle post-processing
            } catch (Exception e) {
                e.printStackTrace(); // Consider a more robust error handling approach
            }
        });

//        try {
//            semaphore.acquire();
//            executorService.submit(() -> {
//                try {
//                    this.behaviourHandler.process(packet);
//                } catch (Exception e) {
//                    e.printStackTrace(); // Consider a more robust error handling approach
//                } finally {
//                    semaphore.release();
//                }
//            });
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt(); // Preserve the interrupt status
//            // Handle the exception
//        }
//        packetQueue.offer(packetWrapper);
    }
    public void setBehaviours(BehaviourHandler<Packet> behaviourHandler) {
        this.behaviourHandler = behaviourHandler;
    }

    @Override
    public void run() {
//        while (true) {
////            System.out.println(packetQueue.toArray().length);
//            List<PacketWrapper> batch = new ArrayList<>();
//            packetQueue.drainTo(batch);
//
//            if (!batch.isEmpty()) {
//                try {
//                    semaphore.acquire();
//                    executorService.submit(() -> {
//                        try {
//                            for (PacketWrapper packetWrapper : batch) {
//                                this.behaviourHandler.process(packetWrapper.getPacket());
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        } finally {
//                            semaphore.release();
//                        }
//                    });
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                }
//            }
//        }

//        try {

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
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    public void stop() {
        // Stop all threads
//        executorService.shutdown(); // TODO: This is temp, just while I use executorServiceSingleton
    }
}
