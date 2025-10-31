package friendshipMod;

import friendshipMod.data.Relationships;
import friendshipMod.data.TicketManager;
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
        WorldDataRegistry.registerWorldData(TicketManager.dataKey, TicketManager.class);
        PacketRegistry.registerPacket(RelationshipPacket.class);
        PacketRegistry.registerPacket(RelationshipsPacket.class);
    }

    public void initResources() {
    }

    public void postInit() {
    }

}
