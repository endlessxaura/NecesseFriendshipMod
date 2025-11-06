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

    public RelationshipPacket(byte[] data) {
        super(data);
        PacketReader reader = new PacketReader(this);
        Association association = new Association(
            reader.getNextInt(),
            reader.getNextInt()
        );
        int value = reader.getNextInt();
        relationship = new Relationship(association, value);
    }

    public RelationshipPacket(Relationship relationship) {
        this.relationship = relationship;
        PacketWriter writer = new PacketWriter(this);
        writer.putNextInt(relationship.getAssociation().first());
        writer.putNextInt(relationship.getAssociation().second());
        writer.putNextInt(relationship.score);
    }

    public void applyPacket(WorldEntity worldEntity) {
        Relationships relationships = Relationships.getInstance(worldEntity);
        relationships.setRelationship(relationship);
        System.out.println(FriendshipMod.modId + ": RelationshipPacket applied at " + (worldEntity.isServer() ? "server" : "client"));
    }

    @Override
    public void processClient(NetworkPacket packet, Client client) {
        applyPacket(client.worldEntity);
    }
}
