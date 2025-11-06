package friendshipMod.packets;

import friendshipMod.FriendshipMod;
import friendshipMod.data.Personalities;
import friendshipMod.data.Personality;
import necesse.engine.network.NetworkPacket;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import necesse.entity.mobs.Mob;

import javax.naming.OperationNotSupportedException;

public class PersonalityRequestPacket extends Packet {
    public int mobId;

    public PersonalityRequestPacket(byte[] data) {
        super(data);
        PacketReader reader = new PacketReader(this);
        this.mobId = reader.getNextInt();
    }

    public PersonalityRequestPacket(int mobId) {
        this.mobId = mobId;
        PacketWriter writer = new PacketWriter(this);
        writer.putNextInt(mobId);
    }

    @Override
    public void processServer(NetworkPacket packet, Server server, ServerClient client) {
        System.out.println(FriendshipMod.modId + ": server received personality request");
        Personalities personalities = Personalities.getInstance(client.getLevel().getWorldEntity());
        Mob mob = server.getLocalServerClient().getLevel().entityManager.mobs.get(mobId, true);
        if (mob == null) {
            return;
        }
        if (!personalities.hasPersonalityFor(mobId)) {
            personalities.generatePersonalityFor(mob);
        }
        Personality personality = personalities.getPersonalityFor(mob);
        PersonalityPacket personalityPacket = new PersonalityPacket(mobId, personality);
        server.network.sendToClientsWithEntity(personalityPacket, mob);
    }
}
