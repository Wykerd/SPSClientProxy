package org.koekepan.VAST.Connection;

import com.github.steveice10.mc.protocol.packet.login.client.LoginStartPacket;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.ConnectedEvent;
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import org.koekepan.App;
import org.koekepan.VAST.CustomPackets.EstablishConnectionPacket;
import org.koekepan.VAST.Packet.PacketHandler;

import java.util.HashMap;
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

    public ClientConnectedInstance( Session session, String VastHost, int VastPort) {
        this.session = session;
        this.session.addListener(new ClientSessionListener());
        this.vastConnection = new VastConnection(VastHost, VastPort);
        this.packetSender = new PacketSender();
        this.packetHandler = new PacketHandler(this);

        clientInstances_PacketSenders.put(packetSender, this);
        packetSender.setClientSession(this.session);

        ScheduledExecutorService packetExecutor;
        packetExecutor = Executors.newSingleThreadScheduledExecutor();
        packetExecutor.scheduleAtFixedRate(packetSender, 0, 50, TimeUnit.MILLISECONDS);

        ScheduledExecutorService handlerExecutor;
        handlerExecutor = Executors.newSingleThreadScheduledExecutor();
        handlerExecutor.scheduleAtFixedRate(packetHandler, 0, 50, TimeUnit.MILLISECONDS);

        this.vastConnection.connect();
    }

    public String getUsername() {
        return session.getFlag("username");
    }

    public PacketHandler getPacketHandler() {
        return packetHandler;
    }

    private static class ClientSessionListener extends SessionAdapter { // This is the client listener (Listens to the packets sent/received from client)
        /*
        The ClientSessionListener should handle packets from the client, forwarding them to the actual Minecraft server.
        It should also handle the login process if necessary.
         */
        private Session serverSession; // The session to the actual Minecraft server (VAST_COM)
        @Override
        public void packetReceived(PacketReceivedEvent event) { // Called when a packet is received from the client
            System.out.println("Received packet (from client): " + event.getPacket().getClass().getSimpleName());

            if (event.getPacket() instanceof LoginStartPacket) { // Logins should be handled by the serverProxy via EstablishConnectionPacket
                LoginStartPacket startPacket = event.getPacket();
                // Extract the username from the login start packet
                String username = startPacket.getUsername();

                System.out.println("Received login packet from " + username);

                EstablishConnectionPacket establishConnectionPacket = new EstablishConnectionPacket(username, true);

                if (App.clientInstances.get(event.getSession()) != null) {
                    App.clientInstances.get(event.getSession()).packetSender.addServerboundPacket(establishConnectionPacket);
                } else {
                    System.out.println("Client instance is null");
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

//            // Forward packets to the Minecraft server (VASt_COM)
//            if (serverSession != null) {
//                serverSession.send(event.getPacket());
//            }
        }

        @Override
        public void connected(ConnectedEvent event) {
            // Called when client is connected to the actual Minecraft server
            System.out.println("Connected to server");
        }

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

    public int getEntityID() {
        return entityID;
    }

    public void setEntityID(int entityID) {
        this.entityID = entityID;
    }

}
