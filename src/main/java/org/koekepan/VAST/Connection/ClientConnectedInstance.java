package org.koekepan.VAST.Connection;

import com.github.steveice10.mc.auth.data.GameProfile;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.data.game.chunk.Chunk;
import com.github.steveice10.mc.protocol.packet.login.client.LoginStartPacket;
import com.github.steveice10.mc.protocol.packet.login.server.LoginSuccessPacket;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.ConnectedEvent;
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import com.github.steveice10.packetlib.packet.Packet;
import org.koekepan.App;
import org.koekepan.Minecraft.ChunkPosition;
import org.koekepan.VAST.CustomPackets.EstablishConnectionPacket;
import org.koekepan.VAST.Packet.PacketHandler;
import org.koekepan.VAST.Packet.SPSPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientConnectedInstance {
    private VastConnection vastConnection; // The connection to the VAST_COM server, used to publish packets to the vast network
    private PacketSender packetSender; // The packet sender, used to send packets to queue packets that are destined for the VAST_COM server and the client
    private PacketHandler packetHandler; // The packet handler, used to run packet behaviours that are received from the VAST_COM server and the client

    private Session session;

    private Boolean joined = false;
    private int entityID = -1; // The entity ID of the player, if the player has joined the game (otherwise -1)

    public static HashMap<PacketSender, ClientConnectedInstance> clientInstances_PacketSenders = new HashMap<PacketSender, ClientConnectedInstance>();
    public static HashSet<UUID> playerUUIDs = new HashSet<UUID>();
    private String clientUsername;

    private int x_position = 0;
    private int y_position = 0;

    public ClientConnectedInstance( Session session, String VastHost, int VastPort) {
        this.session = session;
        this.session.addListener(new ClientSessionListener());

        this.packetSender = new PacketSender();
        this.packetHandler = new PacketHandler(this);

        this.vastConnection = new VastConnection(VastHost, VastPort, this);

        clientInstances_PacketSenders.put(packetSender, this);
        packetSender.setClientSession(this.session);

//        packetSender.startClientSender();
        packetSender.startServerSender();
        this.vastConnection.connect();


        new Thread(() -> { // This thread is used to disconnect the client if it hasn't joined the game after 5 seconds
            try {
                Thread.sleep(5000); // Sleep for 5 seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (!joined) {
                disconnect();
            }
        }).start();
    }

    public String getUsername() {
//        return session.getFlag("username");
        return clientUsername;
    }

    public PacketHandler getPacketHandler() {
        return packetHandler;
    }

    public void addPlayerUUID(UUID id) {
        playerUUIDs.add(id);
    }

    public void removePlayerUUID(UUID id) {
        playerUUIDs.remove(id);
    }

    public boolean hasPlayerUUID(UUID id) {
        return playerUUIDs.contains(id);
    }

    private static class ClientSessionListener extends SessionAdapter { // This is the client listener (Listens to the packets sent/received from client)
        /*
        The ClientSessionListener should handle packets from the client, forwarding them to the actual Minecraft server.
        It should also handle the login process if necessary.
         */
        @Override
        public void packetReceived(PacketReceivedEvent event) { // Called when a packet is received from the client (Serverbound)
//            System.out.println("test");

//            System.out.println("Received packet (from client): " + event.getPacket().getClass().getSimpleName());

            if (event.getPacket() instanceof LoginStartPacket) { // Logins should be handled by the serverProxy via EstablishConnectionPacket
                LoginStartPacket startPacket = event.getPacket();
                // Extract the username from the login start packet
                String username = startPacket.getUsername();

                System.out.println("EmulatedServer: Received login packet from " + username);
                App.clientInstances.get(event.getSession()).setClientUsername(username);
                EstablishConnectionPacket establishConnectionPacket = new EstablishConnectionPacket(username, true);

                if (App.clientInstances.get(event.getSession()) != null) {
                    App.clientInstances.get(event.getSession()).packetSender.addServerboundPacket(establishConnectionPacket);
                } else {
                    System.out.println("EmulatedServer: Client instance is null");
                }
//                App.clientInstances.get(event.getSession()).packetSender.addServerboundPacket(establishConnectionPacket );
//                App.clientInstances.get(event.getSession()).packetHandler.addPacket(establishConnectionPacket);

            } else {
                // Forward packets to the Minecraft server (VASt_COM)
                if (App.clientInstances.get(event.getSession()) != null) {
                    App.clientInstances.get(event.getSession()).packetSender.addServerboundPacket(event.getPacket());
                } else {
                    System.out.println("Client instance is null");
                }
            }
        }

        @Override
        public void connected(ConnectedEvent event) {
            // Called when client is connected to the actual Minecraft server
            System.out.println("Connected to server");
        }

//        @Override
//        public void packetSent(PacketReceivedEvent event) { // SERVERBOUND!
//            // Called when a packet is sent to the server
//            System.out.println("Sent packet (to server)" + event.getPacket().getClass().getSimpleName());
//        }

    }

    private void setClientUsername(String username) {
        this.clientUsername = username;

        this.vastConnection.subscribe(100, 100, 1000, username); // TODO: This should be dynamic
    }

    public VastConnection getVastConnection() {
        return vastConnection;
    }

    public PacketSender getPacketSender() {
        return packetSender;
    }

    public Session getSession() {
        return session;
    }

    public Boolean getJoined() {
        return joined;
    }

    public void setJoined(Boolean joined) {
        this.joined = joined;
    }

    public boolean isJoined() {
        return joined;
    }

    public int getEntityID() {
        return entityID;
    }

    public void setEntityID(int entityID) {
        System.out.println("<" + this.clientUsername + "> Set entityID to " + entityID);

        this.entityID = entityID;
    }

    public void move_player(int x, int y) {
        this.x_position = x;
        this.y_position = y;

        this.vastConnection.publishMove(x, y);
    }

    boolean created_mobile_subscription = false;
    public void set_position(int x, int y) { // TODO: This is currently done in the serverBound packet, think about moveing it to the clientBound packet
        if (this.x_position == x && this.y_position == y) {
            return;
        }

        this.x_position = x;
        this.y_position = y;

        this.vastConnection.publishMove(x, y);

        if (!created_mobile_subscription) {
//            this.vastConnection.subscribeMobile(x, y, 100, "username");

//            // Create an ArrayList to hold ChunkPosition objects
//            ArrayList<ChunkPosition> square = new ArrayList<>();
//
//            // Define the side length and calculate the start and end points
//            int sideLength = 50;
//            int halfLength = sideLength / 2;
//
//            // Populate the ArrayList with ChunkPosition objects
//            for (int x_ = -halfLength; x_ < halfLength; x_++) {
//                for (int y_ = -halfLength; y_ < halfLength; y_++) {
//                    square.add(new ChunkPosition(x_, y_));
//                }
//            }
//
//            this.vastConnection.subscribeMobilePolygon( square, "username2");
//
            created_mobile_subscription = true;
        }

    }

    public void startPermanentSubscriptions(double x, double z) {
        this.vastConnection.unsubscribe(this.getUsername());
        this.vastConnection.subscribeMobile((int)x, (int)z, 10, this.getUsername());

        this.vastConnection.unsubscribe("clientBound");
        this.vastConnection.subscribeMobilePolygon(getSquare((int)x, (int)z, (21*16 + 16*2)), "clientBound"); // Each chunk is 16x16, so 21 chunks is 21*16, and 16*2 is the extra 2 chunks on each side as a buffer
    }

    private static ArrayList<ChunkPosition> getSquare(int x, int z, int sideLength) {
        ArrayList<ChunkPosition> square = new ArrayList<>();
        int halfLength = sideLength / 2;

        // Calculate the corner positions
        int x1 = x - halfLength;
        int x2 = x + halfLength - 1;
        int z1 = z - halfLength;
        int z2 = z + halfLength - 1;

        // Add the corner positions to the square ArrayList
        square.add(new ChunkPosition(x1, z1));
        square.add(new ChunkPosition(x1, z2));
        square.add(new ChunkPosition(x2, z1));
        square.add(new ChunkPosition(x2, z2));

        return square;
    }

    public void disconnect() {

        try {
            SPSPacket disconnectPacket = new SPSPacket(
                    new EstablishConnectionPacket(this.clientUsername, false),
                    this.clientUsername,
                    this.x_position,
                    this.y_position,
                    1,
                    "serverBound"
            );
            this.vastConnection.publish(disconnectPacket);
        } catch (NullPointerException e) {
            System.out.println("Error sending disconnect packet: " + e.getMessage());
        }

        try {
            if (this.vastConnection != null) {
                this.vastConnection.disconnect();
                this.vastConnection = null;
            }
        } catch (NullPointerException e) {
            System.out.println("Error disconnecting vastConnection: " + e.getMessage());
        }

        try {
            if (this.packetSender != null) {
                this.packetSender.stop();
                this.packetSender = null;
            }
        } catch (NullPointerException e) {
            System.out.println("Error stopping packetSender: " + e.getMessage());
        }

        try {
            if (this.packetHandler != null) {
                this.packetHandler.stop();
                this.packetHandler = null;
            }
        } catch (NullPointerException e) {
            System.out.println("Error stopping packetHandler: " + e.getMessage());
        }

        try {
            if (this.session != null) {
                this.session.disconnect("Disconnecting from server :)");
                this.session = null;
            }
        } catch (NullPointerException e) {
            System.out.println("Error disconnecting session: " + e.getMessage());
        }

        clientInstances_PacketSenders.remove(this.packetSender);
        App.clientInstances.remove(this);
    }
}
