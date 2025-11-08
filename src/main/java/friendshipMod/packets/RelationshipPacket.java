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

public class RelationshipPacket extends Packet {
    public Relationship relationship;
    public String type;

    public RelationshipPacket(byte[] data) {
        super(data);
        PacketReader reader = new PacketReader(this);
        Association association = new Association(
            reader.getNextInt(),
            reader.getNextInt()
        );
        int value = reader.getNextInt();
        this.type = reader.getNextString();
        relationship = new Relationship(association, value);
    }

    public RelationshipPacket(Relationship relationship, String type) {
        this.relationship = relationship;
        this.type = type;
        PacketWriter writer = new PacketWriter(this);
        writer.putNextInt(relationship.getAssociation().first());
        writer.putNextInt(relationship.getAssociation().second());
        writer.putNextInt(relationship.score);
        writer.putNextString(type);
    }

    public void applyPacket(WorldEntity worldEntity) {
        Relationships relationships = Relationships.getInstance(worldEntity);
        relationships.setRelationship(relationship, type);
        System.out.println(FriendshipMod.modId + ": RelationshipPacket applied at " + (worldEntity.isServer() ? "server" : "client"));
    }

    @Override
    public void processClient(NetworkPacket packet, Client client) {
        applyPacket(client.worldEntity);
    }

    @Override
    public void processServer(NetworkPacket packet, Server server, ServerClient client) {
        applyPacket(server.world.worldEntity);
    }
}
