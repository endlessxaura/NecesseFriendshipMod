package friendshipMod;

import friendshipMod.data.Relationships;
import friendshipMod.packets.RelationshipPacket;
import friendshipMod.packets.RelationshipsPacket;
import necesse.engine.modLoader.annotations.ModEntry;
import necesse.engine.registries.PacketRegistry;
import necesse.engine.registries.WorldDataRegistry;
import necesse.engine.world.WorldEntity;

@ModEntry
public class FriendshipMod
{
    public static String modId = "FriendshipMod";

    public void init() {
        WorldDataRegistry.registerWorldData(Relationships.dataKey, Relationships.class);
        PacketRegistry.registerPacket(RelationshipPacket.class);
        PacketRegistry.registerPacket(RelationshipsPacket.class);
    }

    public void initResources() {
    }

    public void postInit() {
    }

}
