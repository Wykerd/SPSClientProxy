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
        this.packetHandler = new PacketHandler();

        this.vastConnection.connect();
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
//                SPSPacket spsPacket = new SPSPacket(establishConnectionPacket, username, "serverBound");

////              Use the username to connect to the actual Minecraft server
//                MinecraftProtocol protocol = new MinecraftProtocol(username);
//
////                serverSession = new Client("server.host.com", 25565, protocol, new TcpSessionFactory());
//                Client client = new Client("server.host.com", 25565, protocol, new TcpSessionFactory());
//                serverSession = client.getSession();
//
////              Add a listener to handle packets from the Minecraft server
//                serverSession.addListener(new ServerSessionListener());
//                serverSession.connect(); // This initiates the connection

//                vastConnection.publish(spsPacket);
//                App.clientInstances.get(event.getSession()).vastConnection.publish(spsPacket);
                App.clientInstances.get(event.getSession()).packetSender.addServerboundPacket(establishConnectionPacket);

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
