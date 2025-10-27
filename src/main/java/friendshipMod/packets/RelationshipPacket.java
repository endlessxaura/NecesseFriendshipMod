package friendshipMod.packets;

import friendshipMod.data.Association;
import friendshipMod.data.Relationships;
import necesse.engine.network.NetworkPacket;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.client.Client;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import necesse.engine.world.WorldEntity;
import necesse.entity.mobs.Mob;

public class RelationshipPacket extends Packet {
    public final Association association;
    public final Integer value;

    public RelationshipPacket(byte[] data) {
        super(data);
        PacketReader reader = new PacketReader(this);
        association = new Association(
            reader.getNextInt(),
            reader.getNextInt()
        );
        value = reader.getNextInt();
    }

    public RelationshipPacket(Association association, int value) {
        this.association = association;
        this.value = value;
    }

    public RelationshipPacket(Mob firstMob, Mob secondMob, int value) {
        this.association = new Association(firstMob.getUniqueID(), secondMob.getUniqueID());
        this.value = value;
    }

    public RelationshipPacket(Integer firstMobId, Integer secondMobId, int value) {
        this.association = new Association(firstMobId, secondMobId);
        this.value = value;
    }

    public void applyPacket(WorldEntity worldEntity) {
        Relationships relationships = Relationships.getRelationships(worldEntity);
        relationships.setRelationship(association, value);
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
