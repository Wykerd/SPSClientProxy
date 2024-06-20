package org.koekepan.VAST.Connection;

import com.github.steveice10.packetlib.packet.Packet;
import com.google.gson.Gson;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import jdk.jfr.consumer.RecordedObject;
import org.koekepan.App;
import org.koekepan.Minecraft.ChunkPosition;
import org.koekepan.Performance.PacketCapture;
import org.koekepan.VAST.Connection.PacketSenderRunnables.ClientSender;
import org.koekepan.VAST.Connection.PacketSenderRunnables.ServerSender;
import org.koekepan.VAST.CustomPackets.EstablishConnectionPacket;
import org.koekepan.VAST.Packet.PacketWrapper;
import org.koekepan.VAST.Packet.SPSPacket;

import static java.lang.Thread.sleep;
import static org.koekepan.App.config;
import static org.koekepan.VAST.Packet.PacketUtil.*;
import static org.koekepan.VAST.Packet.PacketWrapper.packetWrapperMap;

public class VastConnection {

    private final int VAST_COM_PORT;
    private final String VAST_COM_IP;
    private final UUID uuid = UUID.randomUUID();

    private Socket socket;
    private ClientConnectedInstance clientInstance;

    private int connectionID;

    public VastConnection(String VAST_COM_IP, int VAST_COM_PORT, ClientConnectedInstance clientInstance) {
        this.VAST_COM_IP = VAST_COM_IP;
        this.VAST_COM_PORT = VAST_COM_PORT;
        this.clientInstance = clientInstance;
    }

    public void disconnect() {
        socket.emit("disconnect_client", connectionID);
//        socket.emit("disconnect", connectionID);
        socket.disconnect();
        socket = null;

        System.out.println("VAST: Disconnected from VAST server");
    }

    private boolean initialiseConnection() {
        final CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        final boolean[] result = {false};
        try {
            socket = IO.socket("http://" + VAST_COM_IP + ":" + VAST_COM_PORT);
            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    socket.emit("handshake", uuid.toString() ,"Hello, server. This is Java Client");
                }
            }).on("handshake", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    if (("Hello, client with UUID: " + uuid.toString() + ". This is Node.js Server.").equals(args[0])) {
                        System.out.println("VAST: Successfully connected to the correct server.");
                        result[0] = true;
//						return true;
                    } else {
                        System.out.println("VAST: Failed to connect to the correct server.");
                    }
                    completableFuture.complete(true);
                }
            });
            socket.connect();

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            result[0] = completableFuture.get();  // this will block until the CompletableFuture is complete
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        return result[0];

    }
    public boolean connect() {
        if (initialiseConnection()) {
            initialiseVASTclient();
            initialiseListeners();
            return true;
        }
        return false;
    }

    private void initialiseVASTclient() { // TODO: This subscribe should be more client-specific: (This is just for the login procedure)


        String gatewayMatcherHost = config.getGateWayMatcherHost();
        int gatewayMatcherPort = config.getGateWayMatcherPort();
        int gatewayMatcher_x = config.getGateWayMatcherXPosition();
        int gatewayMatcher_y = config.getGateWayMatcherYPosition();

//        socket.emit("spawn_VASTclient", "Minecraft Client 1", "10.42.0.1", "20000", "100", "100");
        socket.emit("spawn_VASTclient", "MC 1", gatewayMatcherHost, Integer.toString(gatewayMatcherPort), gatewayMatcher_x, gatewayMatcher_y);
        
        try {
            sleep(1000);
////            this.subscribe(100, 100, 100000);
//            this.clientInstance.startPermanentSubscriptions(100, 100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("VAST Client initialised");
    }

    public void initialiseListeners() {
//		ConsoleIO.println("Initialize SPS listeners");
        socket.on("ID", new Emitter.Listener() {
            @Override
            public void call(Object... data) {
                System.out.println("Received connection ID: <" + data[0] + ">");
                connectionID = (int) data[0];
            }
        });

        socket.on("getType", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                socket.emit("type", "client");
            }
        });

        socket.on("publication", new Emitter.Listener() {
            @Override
            public void call(Object... data) { // receive publication from vast matcher as a client
//                tempcounter_deleteme += 1;
//				ConsoleIO.println("Received a publication from vastnet and attempting to send to Minecraft player!");
//                VastConnection.printQueueNumbers();
                final SPSPacket packet = receivePublication(data);

                // Format of packet.username: "username&unique_id"
                final String username = packet.username.split("&")[0];
                final String unique_id = packet.username.split("&")[1];

                int x = packet.x;
                int y = packet.y;
                int radius = packet.radius;

                PacketWrapper packetWrapper = new PacketWrapper(packet.packet);
                packetWrapper.unique_id = unique_id;

//                if (packet.packet.getClass().getSimpleName().equals("ServerChunkDataPacket")) {
//                    System.out.println("CHUNK! received from VAST");
//                }

                if (packetWrapperMap.containsKey(packet.packet)) {
                System.out.println("Packet already exists in packetWrapperMap, i.e. duplicate packet");
                }
//                System.out.println("receive");

                packetWrapperMap.put(packet.packet, packetWrapper);

//                System.out.println(clientInstance.getUsername() + " received publication from vast matcher: <" + packet.packet.getClass().getSimpleName() + "> username: <" + username + "> channel: <" + packet.channel + ">");

                if (packet.channel.equals(clientInstance.getUsername())) { // Player Specific Packets



                    PacketWrapper.setPlayerSpecific(packet.packet, clientInstance.getUsername());
                    clientInstance.getPacketSender().addClientboundPacket(packet.packet);
                    PacketCapture.log(packet.packet.getClass().getSimpleName() + "_" + PacketWrapper.get_unique_id(packet.packet), PacketCapture.LogCategory.CLIENTBOUND_IN);
//                    System.out.println("set player specific for packet <" + packet.packet.getClass().getSimpleName() + "> for username: <" + username + "> and channel: <" + packet.channel + ">");
                } else if (packet.channel.equals("clientBound") && !Objects.equals(packet.packet.getClass().getSimpleName(), "ServerKeepAlivePacket")) {
                     if (clientInstance != null) {
                         clientInstance.getPacketSender().addClientboundPacket(packet.packet);
                         PacketCapture.log(packet.packet.getClass().getSimpleName() + "_" + PacketWrapper.get_unique_id(packet.packet), PacketCapture.LogCategory.CLIENTBOUND_IN);
                     } else {
                         System.out.println("ERROR: ClientInstance is null");
                     }
                } else {
                    if (!Objects.equals(username, "Herobrine")) {
//                    Logger.log(SPSConnection.this, Logger.Level.WARN, new String[]{"connection","network", "publication"},"Received a packet <" + packet.packet.getClass().getSimpleName() + "> for an unknown session <"+username+">" + " via channel <" + packet.channel + ">");
                    }
                    System.out.println("No destination for packet <" + packet.packet.getClass().getSimpleName() + "> for session <" + username + ">");
                    packetWrapperMap.remove(packet.packet);
                }
//                }
            }
        });
    }

    public void subscribe(int x, int z, int aoi) {
        subscribe(x, z, aoi, null);
    }

    public void subscribe(int x, int z, int aoi, String channel) {
        socket.emit("subscribe", x, z, aoi, channel == null ? "clientBound" : channel);
    }

    public void subscribePolygon(List<ChunkPosition> positions){
        List<float[]> posList = new ArrayList<float[]>();
        for (ChunkPosition position : positions) {
            posList.add(new float[]{position.getX(), position.getZ()});
        }

        String jsonPositions = new Gson().toJson(posList);

//		ConsoleIO.println("Length of jsonpositions: " + jsonPositions.toString());

        socket.emit("clearsubscriptions", "clientBound");
        socket.emit("subscribe_polygon", jsonPositions, "clientBound");

//        if (!listeners.isEmpty()) {
//            String username = listeners.values().iterator().next().getUsername();
//            socket.emit("clearsubscriptions", username);
//            socket.emit("subscribe_polygon", jsonPositions, username);
//        }

    }

    public void unsubscribe(String channel) {
        socket.emit("clearsubscriptions", channel);
    }

    public void subscribeMobile(int x, int y, int aoi, String channel) {
        socket.emit("subscribe_mobile", x, y, aoi, channel);
    }

    public void subscribeMobilePolygon(List<ChunkPosition> positions, String channel) {
        List<float[]> posList = new ArrayList<float[]>();
        for (ChunkPosition position : positions) {
            posList.add(new float[]{position.getX(), position.getZ()});
        }

        String jsonPositions = new Gson().toJson(posList);

        socket.emit("subscribe_mobile_polygon", jsonPositions, channel);
    }

    public void publish(SPSPacket packet) { // sends to vast matcher as client

//        System.out.println("Connection <"+uuid+"> sent packet <"+packet.packet.getClass().getSimpleName()+"> on channel <"+packet.channel+"> at x: <"+packet.x+"> y: <"+packet.y+"> radius: <"+packet.radius+">");

        //convert to JSON
        Gson gson = new Gson();
        byte[] payload = packetToBytes(packet.packet);
        String json = gson.toJson(payload);
        //ConsoleIO.println("Connection <"+connectionID+"> sent packet <"+packet.packet.getClass().getSimpleName()+"> on channel <"+packet.channel+">");

        if (packet.packet.getClass().getSimpleName().equals("EstablishConnectionPacket")) {
            System.out.println("EstablishConnectionPacket sent to VAST with: " + ((EstablishConnectionPacket) packet.packet).establishConnection());
        }

//        temp_pubcounter += 1;
//        Logger.log(this, Logger.Level.DEBUG, new String[]{"counter", "clientPub"},"Amount of packets sent: " + temp_pubcounter + ": " + packet.packet.getClass().getSimpleName());
        PacketCapture.log(packet.packet.getClass().getSimpleName() + "_" + PacketWrapper.get_unique_id(packet.packet), PacketCapture.LogCategory.SERVERBOUND_OUT);
//        socket.emit("publish", connectionID, packet.username, 100, 100, 10000, json, packet.channel); // TODO: AOI - This should not be hard coded, this is also wack

        if (packet.x == 0 && packet.y == 0) {
            socket.emit("publish", connectionID, packet.username, this.clientInstance.getXPosition(), this.clientInstance.getYPosition(), 10, json, packet.channel); // Radius is 10, to ensure migration TODO: test
        } else {
            socket.emit("publish", connectionID, packet.username, packet.x, packet.y, packet.radius, json, packet.channel);
        }
    }


    public void publishMove(int x, int y) {
        int x_position = x;
        int z_position = y;
        socket.emit("move", x_position, z_position);
    }

    public static void printQueueNumbers() {
        for (ClientConnectedInstance instance : ClientConnectedInstance.clientInstances_PacketSenders.values()) {

            if (instance != null){
                PacketSender packetSender = instance.getPacketSender();
                if (packetSender == null) {
                    System.out.println("packetSender is null for instance: " + instance.getUsername());
//                    Now print all instances
                    for (ClientConnectedInstance instance1 : ClientConnectedInstance.clientInstances_PacketSenders.values()) {
                        System.out.println("Instance: " + instance1.getUsername());
                    }
                    return;
                }
//            System.out.println("User: " + instance.getUsername());
                ClientSender clientSender = packetSender.getClientSender();
                if (clientSender != null) {
                    if (packetSender.queueNumberClientboundLast - clientSender.getQueueNumberClientbound() > 10) {
                        System.out.println("User: " + instance.getUsername() + " queueNumberClientbound: " + clientSender.getQueueNumberClientbound() + " queueNumberClientboundLast: " + packetSender.queueNumberClientboundLast);
                    }
//                System.out.println("User: " + instance.getUsername() + " queueNumberClientbound: " + clientSender.getQueueNumberClientbound() + " queueNumberClientboundLast: " + packetSender.queueNumberClientboundLast);
                }

                ServerSender serverSender = packetSender.getServerSender();
                if (serverSender != null) {
                    if (packetSender.queueNumberServerboundLast - serverSender.getQueueNumberServerbound() > 10) {
                        System.out.println("User: " + instance.getUsername() + " queueNumberServerbound: " + serverSender.getQueueNumberServerbound() + " queueNumberServerboundLast: " + packetSender.queueNumberServerboundLast);
                    }
//                System.out.println("User: " + instance.getUsername() + " queueNumberServerbound: " + serverSender.getQueueNumberServerbound() + " queueNumberServerboundLast: " + packetSender.queueNumberServerboundLast);
                }

//                Print the size of the clientboundPacketQueue and the serverboundPacketQueue
//                System.out.println("User: " + instance.getUsername() + " clientboundPacketQueue: " + packetSender.clientboundPacketQueue.size() + " serverboundPacketQueue: " + packetSender.serverboundPacketQueue.size());

//            System.out.println("User: " + instance.getUsername() + " queueNumberClientbound: " + packetSender.getClientSender().getQueueNumberClientbound() + " queueNumberClientboundLast: " + packetSender.queueNumberClientboundLast);
//            System.out.println("queueNumberClientboundLast: " + packetSender.getQueueNumberClientboundLast());
            }
        }
    }
}
