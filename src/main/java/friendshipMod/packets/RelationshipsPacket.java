package friendshipMod.packets;

import friendshipMod.FriendshipMod;
import friendshipMod.data.Association;
import friendshipMod.data.Relationship;
import friendshipMod.data.Relationships;
import necesse.engine.network.NetworkPacket;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.client.Client;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import necesse.engine.world.WorldEntity;

import java.util.Hashtable;
import java.util.List;

public class RelationshipsPacket extends Packet {
    Hashtable<Association, Integer> associationScores;

    public RelationshipsPacket(byte[] data) {
        super(data);
        associationScores = new Hashtable<Association, Integer>(20);
        PacketReader reader = new PacketReader(this);
        int size = reader.getNextInt();
        for (int i = 0; i < size; i++) {
            Association association = new Association(
                reader.getNextInt(),
                reader.getNextInt()
            );
            int value = reader.getNextInt();
            associationScores.put(association, value);
        }
    }

    public RelationshipsPacket(List<Relationship> relationships) {
        this.associationScores = new Hashtable<>(20);
        PacketWriter writer = new PacketWriter(this);
        writer.putNextInt(relationships.size());
        for (Relationship relationship : relationships) {
            writer.putNextInt(relationship.getAssociation().first());
            writer.putNextInt(relationship.getAssociation().second());
            writer.putNextInt(relationship.score);
        }
    }

    public void applyPacket(WorldEntity worldEntity) {
        Relationships relationships = Relationships.getRelationships(worldEntity);
        for (Association association : associationScores.keySet()) {
            relationships.setRelationship(association, associationScores.get(association));
        }
        for (Association association : associationScores.keySet()) {
            System.out.println(FriendshipMod.modId + ": Set " + relationships.associationOutput(association) + " at " + (worldEntity.isServer() ? "server" : "client"));
        }
    }

    @Override
    public void processServer(NetworkPacket packet, Server server, ServerClient client) {
        applyPacket(server.world.worldEntity);
    }

    @Override
    public void processClient(NetworkPacket packet, Client client) {
        applyPacket(client.worldEntity);
    }
}
