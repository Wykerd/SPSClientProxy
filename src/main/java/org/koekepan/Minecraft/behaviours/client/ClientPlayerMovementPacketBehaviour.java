package org.koekepan.Minecraft.behaviours.client;

import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerMovementPacket;
import com.github.steveice10.packetlib.packet.Packet;
import org.koekepan.Minecraft.ChunkPosition;
import org.koekepan.VAST.Connection.ClientConnectedInstance;
import org.koekepan.VAST.Packet.Behaviour;
import org.koekepan.VAST.Packet.PacketWrapper;
import org.koekepan.VAST.Packet.SPSPacket;

import java.util.Arrays;
import java.util.List;

public class ClientPlayerMovementPacketBehaviour implements Behaviour<Packet> {

    private final ClientConnectedInstance clientInstance;

    public ClientPlayerMovementPacketBehaviour(ClientConnectedInstance clientInstance) {
        this.clientInstance = clientInstance;
    }

    @Override
    public void process(Packet packet) {

        ClientPlayerMovementPacket playerMovementPacket = (ClientPlayerMovementPacket) packet;
        clientInstance.set_position((int) playerMovementPacket.getX(), (int) playerMovementPacket.getZ());

        SPSPacket spsPacket = new SPSPacket(packet, clientInstance.getUsername(), "serverBound");
        PacketWrapper packetWrapper = PacketWrapper.getPacketWrapper(packet);
        if (packetWrapper != null) {
            packetWrapper.setSPSPacket(spsPacket);
        }

        PacketWrapper.setProcessed(packet, true);

//        clientInstance.getVastConnection().unsubscribe("clientBound");
//        clientInstance.getVastConnection().unsubscribe(clientInstance.getUsername());
//        clientInstance.getVastConnection().subscribe((int) playerMovementPacket.getX(), (int) playerMovementPacket.getZ(), 10, clientInstance.getUsername());

        // Now call subscribePolygon with this list
//        clientInstance.getVastConnection().subscribePolygon(getChunkPositions(playerMovementPacket));

    }

    private List<ChunkPosition> getChunkPositions(ClientPlayerMovementPacket playerMovementPacket) {
        int x = (int) playerMovementPacket.getX();
        int z = (int) playerMovementPacket.getZ();

// Calculate the corner points of the square
        ChunkPosition topLeft = new ChunkPosition(x - 220, z - 220);
        ChunkPosition topRight = new ChunkPosition(x + 220, z - 220);
        ChunkPosition bottomLeft = new ChunkPosition(x - 220, z + 220);
        ChunkPosition bottomRight = new ChunkPosition(x + 220, z + 220);

// Create a list of these positions
        List<ChunkPosition> positions = Arrays.asList(topLeft, topRight, bottomLeft, bottomRight);
        return positions;
    }
}
