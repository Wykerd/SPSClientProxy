package org.koekepan;

import com.github.steveice10.packetlib.event.server.ServerListener;
import com.github.steveice10.mc.protocol.MinecraftConstants;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.ServerLoginHandler;
import com.github.steveice10.packetlib.Server;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.server.*;
import com.github.steveice10.packetlib.tcp.TcpSessionFactory;
import org.koekepan.VAST.Connection.ClientConnectedInstance;
import org.koekepan.VAST.Connection.VastConnection;

import java.util.HashMap;

public class App 
{
    static AppConfig config = new AppConfig();
//    config.loadProperties();

    // This is the server ip/port that the proxy will listen on (aka the server that is emulated)
    static String host = config.getHost();
    static int port = config.getPort();

    // This is the VAST_COM ip/port that the proxy will connect to (aka the sps client)
    static String vastHost = config.getVastHost();
    static int vastPort = config.getVastPort();

    private static VastConnection vastConnection;

//    private static final ArrayList<ClientConnectedInstance> clientInstances = new ArrayList<ClientConnectedInstance>();
    public static HashMap<Session, ClientConnectedInstance> clientInstances = new HashMap<Session, ClientConnectedInstance>();

    public App() {
        // 0. Initialize the packet sender
//        PacketSender packetSender = new PacketSender();

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

//                clientInstances.add(new ClientConnectedInstance(session, vastHost, vastPort));
                clientInstances.put(session, new ClientConnectedInstance(session, vastHost, vastPort));

//                session.addListener(new ClientSessionListener());

            }

            @Override
            public void sessionRemoved(SessionRemovedEvent sessionRemovedEvent) {
                System.out.println("Session removed: " + sessionRemovedEvent.getSession().getHost() + ":" + sessionRemovedEvent.getSession().getPort());
                clientInstances.get(sessionRemovedEvent.getSession()).disconnect();
            }
        });

        // TODO: Should wait for the server to be bound before connecting to VAST_COM
        // 2. Create VAST_COM connection
//        vastConnection = new VastConnection(vastHost, vastPort);
//        vastConnection.connect();

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
