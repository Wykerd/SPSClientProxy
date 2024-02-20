package org.koekepan.VAST.Packet;

import com.github.steveice10.packetlib.packet.Packet;

import java.util.HashMap;

public class PacketWrapper {
    public static final HashMap<Packet, PacketWrapper> packetWrapperMap = new HashMap<Packet, PacketWrapper>();
    public int queueNumber;
    public String unique_id;
    private SPSPacket spsPacket;
    private Packet packet;
    public boolean isProcessed = false;
    public boolean clientBound = false;
    private String PlayerSpecific = null;

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
        this.PlayerSpecific = PlayerSpecific;
    }

    public String PlayerSpecific() {
        return PlayerSpecific;
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
        return packetWrapperMap.get(packet);
    }

    public static PacketWrapper getPacketWrapperByQueueNumber(HashMap<Integer, PacketWrapper> map, int queueNumber) {
        return map.get(queueNumber);
    }

    public static void setProcessed(Packet packet, boolean isProcessed) {
        PacketWrapper packetWrapper = packetWrapperMap.get(packet);
        if (packetWrapper != null) {
            packetWrapper.isProcessed = isProcessed;
        }
    }

    public static void removePacketWrapper(Packet packet) {
        packetWrapperMap.remove(packet);
    }


}
