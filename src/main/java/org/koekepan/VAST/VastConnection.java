package org.koekepan.VAST;

import com.google.gson.Gson;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.koekepan.Minecraft.ChunkPosition;

import static java.lang.Thread.sleep;

public class VastConnection {

    private final int VAST_COM_PORT;
    private final String VAST_COM_IP;
    private final UUID uuid = UUID.randomUUID();

    private Socket socket;

    public VastConnection(String VAST_COM_IP, int VAST_COM_PORT) {
        this.VAST_COM_IP = VAST_COM_IP;
        this.VAST_COM_PORT = VAST_COM_PORT;
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
                        System.out.println("Successfully connected to the correct server.");
                        result[0] = true;
//						return true;
                    } else {
                        System.out.println("Failed to connect to the correct server.");
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
//            initialiseListeners();
            return true;
        }
        return false;
    }

    private void initialiseVASTclient() { // TODO: This subscribe should be more client-specific: (This is just for the login procedure)
        socket.emit("spawn_VASTclient", "Minecraft Client 1", "10.42.0.1", "20000", "100", "100"); 
        
        try {
            sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        this.subscribe(100, 100, 10000);

        System.out.println("VAST Client initialised");
    }

    public void subscribe(int x, int z, int aoi) {
        subscribe(x, z, aoi, null);
    }

    public void subscribe(int x, int z, int aoi, String channel) {
        if (channel == null) {
            socket.emit("subscribe", x, z, aoi, "clientBound");
//            if (!listeners.isEmpty()) {
//                socket.emit("subscribe", x, z, aoi, listeners.values().iterator().next().getUsername());
//            }
        } else {
            socket.emit("subscribe", x, z, aoi, channel);
        }
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

    public void unsubscribed(String channel) {
        socket.emit("clearsubscriptions", channel);
    }


}
