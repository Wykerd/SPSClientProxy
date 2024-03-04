package org.koekepan.VAST.Packet;

import com.github.steveice10.packetlib.packet.Packet;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Thread.sleep;

public class PacketWrapper {
    public static final HashMap<Packet, PacketWrapper> packetWrapperMap = new HashMap<Packet, PacketWrapper>();
    public int queueNumber;
    public String unique_id;
    private SPSPacket spsPacket;
    private Packet packet;
    public boolean isProcessed = false;
    public boolean clientBound = false;
    private String playerSpecific = null;

    public PacketWrapper(SPSPacket spsPacket) {

        this.spsPacket = spsPacket;
        this.isProcessed = true;
    }

    public PacketWrapper (SPSPacket spsPacket, boolean isProcessed) {
        this.spsPacket = spsPacket;
        this.isProcessed = isProcessed;
    }

    public PacketWrapper(Packet packet) {
        this.packet = packet;
    }
    public PacketWrapper(Packet packet, boolean isProcessed) {
        this.packet = packet;
        this.isProcessed = isProcessed;
    }

    public static void setPlayerSpecific(Packet packet, String username) {
        PacketWrapper packetWrapper = packetWrapperMap.get(packet);
        if (packetWrapper != null) {
            packetWrapper.setPlayerSpecific(username);
        }
    }

    public SPSPacket getSPSPacket() {
        return spsPacket;
    }

    public Packet getPacket() {
        return packet;
    }

    public void setPacket(Packet newPacket) {
        this.packet = newPacket;
    }

    public void setSPSPacket(SPSPacket newSpsPacket) {
        this.spsPacket = newSpsPacket;
    }

    public void setPlayerSpecific(String PlayerSpecific) {
        this.playerSpecific = PlayerSpecific;
    }

    public String getPlayerSpecific() {
        return playerSpecific;
    }

    public static int get_QueueNumber(Packet packet) {
        PacketWrapper packetWrapper = packetWrapperMap.get(packet);
        if (packetWrapper != null) {
            return packetWrapper.queueNumber;
        }
        return -1;
    }

    public static void set_unique_id(Packet packet, String unique_id) {
        PacketWrapper packetWrapper = packetWrapperMap.get(packet);
        if (packetWrapper != null) {
            packetWrapper.unique_id = unique_id;
        }
    }

    public static String get_unique_id(Packet packet) {
        PacketWrapper packetWrapper = packetWrapperMap.get(packet);
        if (packetWrapper != null) {
            return packetWrapper.unique_id;
        }
        return null;
    }

    public static PacketWrapper getPacketWrapper(Packet packet) {
        PacketWrapper packetWrapper = null;
        try {
            packetWrapper = packetWrapperMap.get(packet);
        } catch (Exception e) {
            System.out.println("PacketWrapper::getPacketWrapper => PacketWrapper is null! Packet: " + packet.getClass().getSimpleName());
        }
        return packetWrapper;
    }

    public static PacketWrapper getPacketWrapperByQueueNumber(ConcurrentHashMap<Integer, PacketWrapper> map, int queueNumber) {
        return map.get(queueNumber);
    }

    public static void setProcessed(Packet packet, boolean isProcessed) {
        PacketWrapper packetWrapper = packetWrapperMap.get(packet);
        if (packetWrapper != null) {
            packetWrapper.isProcessed = isProcessed;
        } else {
            new Thread(() -> {
                try {
                    sleep(50);
//                    setProcessed(packet, isProcessed);
                    PacketWrapper packetWrapper1 = packetWrapperMap.get(packet);
                    if (packetWrapper1 != null) {
                        packetWrapper1.isProcessed = isProcessed;
                    } else {
                        System.out.println("PacketWrapper::setProcessed => (ERROR) PacketWrapper is null! Packet: " + packet.getClass().getSimpleName());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    public static void removePacketWrapper(Packet packet) {
        packetWrapperMap.remove(packet);
    }


}
