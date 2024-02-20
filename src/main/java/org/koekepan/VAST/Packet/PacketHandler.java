package org.koekepan.VAST.Packet;

import com.github.steveice10.packetlib.packet.Packet;
import org.koekepan.Minecraft.behaviours.ClientBoundPacketBehaviours;
import org.koekepan.VAST.Connection.ClientConnectedInstance;

import java.util.ArrayDeque;
import java.util.Deque;

public class PacketHandler implements Runnable {

    Deque<PacketWrapper> packetQueue = new ArrayDeque<PacketWrapper>();
    BehaviourHandler<Packet> behaviourHandler = new BehaviourHandler<Packet>();

    public PacketHandler(ClientConnectedInstance clientInstance) {
        this.behaviourHandler = new BehaviourHandler<Packet>();
        ClientBoundPacketBehaviours clientBoundPacketBehaviours = new ClientBoundPacketBehaviours(clientInstance);
        clientBoundPacketBehaviours.registerForwardingBehaviour();
        this.setBehaviours(clientBoundPacketBehaviours);
    }

    public void addPacket(PacketWrapper packetWrapper) {
        packetQueue.add(packetWrapper);
    }

    public void setBehaviours(BehaviourHandler<Packet> behaviourHandler) {
        this.behaviourHandler = behaviourHandler;
    }

    @Override
    public void run() {

        try {
            if (!packetQueue.isEmpty()) {
                PacketWrapper packetWrapper = packetQueue.poll();
                if (packetWrapper != null) {
                    if (!packetWrapper.isProcessed) {
                        Packet packet = packetWrapper.getPacket();
                        if (packet != null) {
                            this.behaviourHandler.process(packet);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
