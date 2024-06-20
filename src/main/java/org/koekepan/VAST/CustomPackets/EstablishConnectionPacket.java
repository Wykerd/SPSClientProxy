package org.koekepan.VAST.CustomPackets;

import java.io.IOException;

import com.github.steveice10.mc.protocol.util.ReflectionToString;
import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;
import com.github.steveice10.packetlib.packet.Packet;

public class EstablishConnectionPacket implements Packet {

    boolean establishConnection;
    String username;
    String ip;
    int port;

    public EstablishConnectionPacket() {}


    public EstablishConnectionPacket(String username, boolean establishConnection, String ip, int port) {
        this.username = username;
        this.establishConnection = establishConnection;
        this.ip = ip;
        this.port = port;
    }

    public EstablishConnectionPacket(String username, boolean establishConnection) {
        this.username = username;
        this.establishConnection = establishConnection;
    }


    public String getUsername() {
        return this.username;
    }


    public boolean establishConnection() {
        return this.establishConnection;
    }

    public String getIp() {
    	return this.ip;
    }

    public int getPort() {
    	return this.port;
    }


    @Override
    public void read(NetInput in) throws IOException {
        this.username = in.readString();
        byte data = in.readByte();
        this.establishConnection = (data == 1);
        this.ip = in.readString(); // read the ip from the input stream
        this.port = in.readVarInt(); // read the port from the input stream
    }

    @Override
    public void write(NetOutput out) throws IOException {
        out.writeString(this.username);
        byte[] data = new byte[1];
        data[0] = (byte)(establishConnection ? 1 : 0);
        out.writeBytes(data);
        out.writeString(this.ip); // write the ip to the output stream
        out.writeVarInt(this.port); // write the port to the output stream
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