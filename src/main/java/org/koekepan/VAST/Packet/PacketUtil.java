package org.koekepan.VAST.Packet;

import com.github.steveice10.packetlib.io.buffer.ByteBufferNetInput;
import com.github.steveice10.packetlib.io.buffer.ByteBufferNetOutput;
import com.github.steveice10.packetlib.packet.Packet;
import com.google.gson.Gson;
import org.json.JSONException;

import java.nio.ByteBuffer;
public final class PacketUtil {
    private static final org.koekepan.Minecraft.PacketProtocol protocol = new org.koekepan.Minecraft.PacketProtocol();

    private PacketUtil() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    public static Packet retrievePacket(String publication) {
        Gson gson = new Gson();
        Packet packet = null;
        try {
            byte[] payload = gson.fromJson(publication, byte[].class);
            packet = bytesToPacket(payload);
        } catch (Throwable e) {
            System.out.println(e.toString());
        }
        return packet;
    }

    public static Packet bytesToPacket(byte[] payload) {
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        ByteBufferNetInput input = new ByteBufferNetInput(buffer);
        Packet packet = null;
        try {
            int packetId = protocol.getPacketHeader().readPacketId(input);
            packet = protocol.createIncomingPacket(packetId);
            packet.read(input);
        } catch (Exception e) {
            System.out.println("Exception: " + e.toString());
        }
        return packet;
    }

    public static SPSPacket receivePublication(Object... data) { // received from the vast matcher/client
        SPSPacket spsPacket = null;

        try {
            org.json.JSONObject jsonObject = (org.json.JSONObject) data[0];
            org.json.JSONObject payloadObject = jsonObject.getJSONObject("payload");

//			ConsoleIO.println("This is what I've got: " + payloadObject.toString());

//			{"chain":[1],"clientID":"zDJlt","payload":{"0":0,"1":"user_01","2":0,"3":0,"4":200,"5":"[117,7,117,115,101,114,95,48,49,1]","6":"login"},"recipients":[1],"channel":"login","aoi":{"center":{"x":500,"y":500},"radius":500},"matcherID":1}
            String connectionID = payloadObject.getString("connectionID");
            String userName = payloadObject.getString("username");
            int x = payloadObject.getInt("x");
            int y = payloadObject.getInt("y");
            int radius = payloadObject.getInt("radius");
            String publication = payloadObject.getString("actualPacket");
            String channel = payloadObject.getString("channel");

            Packet packet = retrievePacket(publication);

            spsPacket = new SPSPacket(packet, userName, x, y, radius, channel);
//				public SPSPacket(Packet packet, String username, int x, int y, int radius, String channel) {

        } catch (JSONException e) {
            e.printStackTrace();
            // Handle the exception (e.g., log it, throw it, etc.)
        }
        return spsPacket;
    }

    public static byte[] packetToBytes(Packet packet) {
        ByteBuffer buffer = ByteBuffer.allocate(75000);
        ByteBufferNetOutput output = new ByteBufferNetOutput(buffer);

        int packetId = protocol.getOutgoingId(packet.getClass());
        try {
            protocol.getPacketHeader().writePacketId(output, packetId);
            packet.write(output);
        } catch (Exception e) {
            System.out.println("Exception: "+e.toString());
        }
        byte[] payload = new byte[buffer.position()];
        buffer.flip();
        buffer.get(payload);
        return payload;
    }

}