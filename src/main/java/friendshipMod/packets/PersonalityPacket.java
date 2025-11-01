package friendshipMod.packets;

import friendshipMod.FriendshipMod;
import friendshipMod.data.*;
import necesse.engine.network.NetworkPacket;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.client.Client;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import necesse.engine.world.WorldEntity;

import java.util.LinkedList;
import java.util.List;

public class PersonalityPacket extends Packet {
    public Integer mobId;
    public Personality personality;

    public PersonalityPacket(byte[] data) {
        super(data);
        PacketReader reader = new PacketReader(this);
        this.mobId = reader.getNextInt();
        this.personality = new Personality();
        personality.likes = getListFromPacket(reader);
        personality.dislikes = getListFromPacket(reader);
    }

    public PersonalityPacket(Integer mobId, Personality personality) {
        this.mobId = mobId;
        this.personality = personality;
        PacketWriter writer = new PacketWriter(this);
        writer.putNextInt(mobId);
        addListToPacket(writer, personality.likes);
        addListToPacket(writer, personality.dislikes);
    }

    private void addListToPacket(PacketWriter writer, List<Integer> list) {
        writer.putNextInt(list.size());
        for (Integer integer : list) {
            writer.putNextInt(integer);
        }
    }

    public List<Integer> getListFromPacket(PacketReader reader) {
        List<Integer> list = new LinkedList<>();
        int size = reader.getNextInt();
        for (int i = 0; i < size; i++) {
            list.add(reader.getNextInt());
        }
        return list;
    }

    public void applyPacket(WorldEntity worldEntity) {
        Personalities personalities = Personalities.getInstance(worldEntity);
        personalities.setPersonality(mobId, personality);
        System.out.println(FriendshipMod.modId + ": PersonalityPacket applied at " + (worldEntity.isServer() ? "server" : "client"));
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
