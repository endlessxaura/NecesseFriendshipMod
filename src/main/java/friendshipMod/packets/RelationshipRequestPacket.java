package friendshipMod.packets;

import friendshipMod.FriendshipMod;
import friendshipMod.data.Relationship;
import friendshipMod.data.Relationships;
import necesse.engine.network.NetworkPacket;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import necesse.entity.mobs.Mob;

import java.util.List;

public class RelationshipRequestPacket extends Packet {
    public int mobId;

    public RelationshipRequestPacket(byte[] data) {
        super(data);
        PacketReader reader = new PacketReader(this);
        mobId = reader.getNextInt();
    }

    public RelationshipRequestPacket(int mobId) {
        this.mobId = mobId;
        PacketWriter writer = new PacketWriter(this);
        writer.putNextInt(mobId);
    }

    @Override
    public void processServer(NetworkPacket packet, Server server, ServerClient client) {
        System.out.println(FriendshipMod.modId + ": server received relationships request");
        Relationships relationships = Relationships.getInstance(client.getLevel().getWorldEntity());
        Mob mob = client.getLevel().entityManager.mobs.get(mobId, true);
        List<Relationship> mobRelationships = relationships.getRelationshipsFor(mobId);
        RelationshipsPacket relationshipsPacket = new RelationshipsPacket(mobRelationships);
        server.network.sendToClientsWithEntity(relationshipsPacket, mob);
    }
}
