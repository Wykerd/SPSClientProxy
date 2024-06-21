package org.koekepan.VAST.CustomPackets;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

import com.github.steveice10.mc.protocol.util.ReflectionToString;
import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;
import com.github.steveice10.packetlib.packet.Packet;

public class PINGPONG implements Packet {
    public static final UUID serverUUID = UUID.randomUUID();
    private UUID PingOriginServerID;

    public PINGPONG() {
    }

    public enum Direction {
        SERVERBOUND,
        CLIENTBOUND
    }

    public enum Origin {
        ClientProxy,
        CLIENT_VAST_COM,
        MATCHER,
        SERVER_VAST_COM,
        ServerProxy
    }

    public enum Type {
        PING,
        PONG
    }

    // List of all servers packet has processed through
    private final Map<Origin, UUID> propogationList = new HashMap<>(); // All destinations while pinging

//    private final Map<Origin, AbstractMap.SimpleEntry<UUID, Instant>> propogationList2 = new HashMap<>();


    private final UUID uniqueId = UUID.randomUUID();
    private Instant initTime;
    private Direction direction;
    private Origin origin;
    private Type type;

    boolean serverBound;
//    public PINGPONG(UUID pingOriginServerID) {
//        PingOriginServerID = pingOriginServerID;
//    }


    public PINGPONG(Direction direction, Origin origin, Type type) {
        this.direction = direction;
        this.origin = origin;
        this.type = type;
        this.initTime = Instant.now();
        this.PingOriginServerID = serverUUID;
    }


    public Direction getDirection() {
        return this.direction;
    }

    public Origin getOrigin() {
        return this.origin;
    }

    public Type getType() {
        return this.type;
    }

    public UUID getUniqueId() {
        return this.uniqueId;
    }

    public Instant getInitTime() {
        return this.initTime;
    }

    public Map<Origin, UUID> getPropogationList() {
        return this.propogationList;
    }

    public void addPropogation(Origin origin) {
        this.propogationList.put(origin, serverUUID);
    }

    public void setServerBound(boolean serverBound) {
        this.serverBound = serverBound;
    }

    public boolean isServerBound() {
        return this.serverBound;
    }

    public void setOrigin(Origin origin) {
        this.origin = origin;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public boolean hasPropagatedThroughMe() {
        return this.propogationList.containsValue(serverUUID);
    }

    public UUID getPingOriginServerID() {
        return PingOriginServerID;
    }

    public void setPingOriginServerID(UUID pingOriginServerID) {
        PingOriginServerID = pingOriginServerID;
    }

    @Override
    public void read(NetInput in) throws IOException {
        this.direction = Direction.valueOf(in.readString());
        this.origin = Origin.valueOf(in.readString());
        this.type = Type.valueOf(in.readString());
        this.serverBound = in.readBoolean();
        this.initTime = Instant.ofEpochMilli(in.readLong());
        this.PingOriginServerID = in.readUUID();

//        // Read the size of the propogationList
//        int propogationListSize = in.readVarInt();
//
//        // Read each entry in the propogationList
//        for (int i = 0; i < propogationListSize; i++) {
//            Origin key = Origin.valueOf(in.readString());
//            UUID value = in.readUUID();
//            this.propogationList.put(key, value);
//        }
    }

    @Override
    public void write(NetOutput out) throws IOException {
        out.writeString(this.direction.name());
        out.writeString(this.origin.name());
        out.writeString(this.type.name());
        out.writeBoolean(this.serverBound);
        out.writeLong(this.initTime.toEpochMilli());
        out.writeUUID(this.PingOriginServerID);

//        // Write the size of the propogationList
//        out.writeVarInt(this.propogationList.size());
//
//        // Write each entry in the propogationList
//        for (Map.Entry<Origin, UUID> entry : this.propogationList.entrySet()) {
//            out.writeString(entry.getKey().name());
//            out.writeUUID(entry.getValue());
//        }
    }


    @Override
    public boolean isPriority() {
        return false;
    }


    @Override
    public String toString() {
        return ReflectionToString.toString(this);
    }
}