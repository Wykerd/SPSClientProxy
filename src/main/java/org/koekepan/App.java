package org.koekepan;

import com.github.steveice10.mc.protocol.ClientListener;
import com.github.steveice10.mc.protocol.packet.login.client.LoginStartPacket;
import com.github.steveice10.packetlib.Client;
import com.github.steveice10.packetlib.event.server.ServerListener;
import com.github.steveice10.mc.protocol.MinecraftConstants;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.ServerLoginHandler;
import com.github.steveice10.packetlib.Server;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.server.*;
import com.github.steveice10.packetlib.event.session.ConnectedEvent;
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent;
import com.github.steveice10.packetlib.event.session.PacketSentEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import com.github.steveice10.packetlib.packet.Packet;
import com.github.steveice10.packetlib.tcp.TcpSessionFactory;
import org.koekepan.VAST.SPSPacket;
import org.koekepan.VAST.VastConnection;

public class App 
{

    // This is the server ip/port that the proxy will listen on (aka the server that is emulated)
    static String host = "localhost";
    static int port = 25565;

    // This is the VAST_COM ip/port that the proxy will connect to (aka the sps client)
    static String vastHost = "localhost";
    static int vastPort = 3456;

    private static VastConnection vastConnection;

    public App() {
        // 1. Create a server
        Server server = new Server(host, port, MinecraftProtocol.class, new TcpSessionFactory());
        server.setGlobalFlag("server-info", "SPSClientProxy");
        server.setGlobalFlag(MinecraftConstants.VERIFY_USERS_KEY, false);
        server.setGlobalFlag("server-login-handler", new ServerLoginHandler() {
            @Override
            public void loggedIn(com.github.steveice10.packetlib.Session session) {
                System.out.println("Player " + session.getFlag("username") + " has connected.");
            }
        });
        server.bind(true);

        server.addListener(new ServerListener() { // This is the server listener for the proxy server (aka the server that is emulated)
            @Override
            public void serverBound(ServerBoundEvent serverBoundEvent) {
                System.out.println("ServerBoundEvent (simulated server): " );
            }

            @Override
            public void serverClosing(ServerClosingEvent serverClosingEvent) {
                System.out.println("Emulated server is closing");
            }

            @Override
            public void serverClosed(ServerClosedEvent serverClosedEvent) {
                System.out.println("Emulated server is closed");
            }

            @Override
            public void sessionAdded(SessionAddedEvent event) {
//                event.getSession().addListener(new ClientSessionListener());
                System.out.println("Session added: " + event.getSession().getHost() + ":" + event.getSession().getPort());

                Session session = event.getSession();
                session.addListener(new ClientSessionListener());

            }

            @Override
            public void sessionRemoved(SessionRemovedEvent sessionRemovedEvent) {
                System.out.println("Session removed: " + sessionRemovedEvent.getSession().getHost() + ":" + sessionRemovedEvent.getSession().getPort());
            }
        });

        // TODO: Should wait for the server to be bound before connecting to VAST_COM
        // 2. Create VAST_COM connection
        vastConnection = new VastConnection(vastHost, vastPort);
        vastConnection.connect();

        // 3. Create a client/Start the login process


    }

    public static void main(String[] args )
    {
        // read command line parameters
        try {
            switch (args.length) {
                case 0:
                    // do nothing;
                    break;
                case 2:
                    vastHost = args[0];
                    vastPort = Integer.parseInt(args[1]);
                    break;
                case 4:
                    vastHost = args[0];
                    vastPort = Integer.parseInt(args[1]);
                    host = args[2];
                    port = Integer.parseInt(args[3]);
                    break;
                default:
                    printUsageMessage();
                    break;
            }
        } catch (NumberFormatException e) {
            printUsageMessage();
        }

        System.out.println( "Hello World! We are starting the SPSClientProxy!");
        System.out.println("VAST_COM Host: " + vastHost + " Port: " + vastPort);
        System.out.println("Emulated Server Host: " + host + " Port: " + port);

        new App();
    }

    public static void printUsageMessage() {
        System.out.println("Herobrine Proxy v1.11.2a");
        System.out.println("-------------------------");
        System.out.println("To use from command line execute HerobrineProxy class with parameters: ");

        System.out.println("> [Server Host (String)] [Server Port (int)] and default [Proxy Host:Proxy Port] = [127.0.0.1:25570] or ");
        System.out.println("> [Server Host (String)] [Server Port (int)] [Proxy Host (String)] [Proxy Port (int)]");
    }

    public static class ClientSessionListener extends SessionAdapter { // This is the client listener (Listens to the packets sent/received from client)
        /*
        The ClientSessionListener should handle packets from the client, forwarding them to the actual Minecraft server.
        It should also handle the login process if necessary.
         */
        private Session serverSession; // The session to the actual Minecraft server (VAST_COM)
        @Override
        public void packetReceived(PacketReceivedEvent event) { // Called when a packet is received from the client
            System.out.println("Received packet (from client): " + event.getPacket().getClass().getSimpleName());

            if (event.getPacket() instanceof LoginStartPacket) {
                LoginStartPacket startPacket = event.getPacket();
                // Extract the username from the login start packet
                String username = startPacket.getUsername();

                System.out.println("Received login packet from " + username);

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

                SPSPacket spsPacket = new SPSPacket(event.getPacket(), username, "serverBound");
                vastConnection.publish(spsPacket);

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

//    public static class ServerSessionListener extends SessionAdapter { // This is the server listener (Listens to the packets sent/received from server/VAST_COM)
//        @Override
//        public void packetReceived(PacketReceivedEvent event) { // CLIENTBOUND!
//            System.out.println("Received packet (from server): " + event.getPacket().getClass().getSimpleName());
//            // Forward packets to the client
//            // You need to have a reference to the client's session to do this
//        }
//
//        @Override
//        public void packetSent(PacketSentEvent event) { // SERVERBOUND!
//            // Called when a packet is sent to the server
//            System.out.println("Sent packet (to server)" + event.getPacket().getClass().getSimpleName());
//        }
//    }


}
