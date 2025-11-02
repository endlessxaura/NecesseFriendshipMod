package friendshipMod;

import friendshipMod.data.Personalities;
import friendshipMod.data.Relationships;
import friendshipMod.data.TicketManager;
import friendshipMod.packets.PersonalityPacket;
import friendshipMod.packets.RelationshipPacket;
import friendshipMod.packets.RelationshipsPacket;
import necesse.engine.modLoader.annotations.ModEntry;
import necesse.engine.registries.PacketRegistry;
import necesse.engine.registries.WorldDataRegistry;
import necesse.engine.world.WorldEntity;
import necesse.engine.world.worldData.WorldData;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;
import java.util.Collections;

import static net.bytebuddy.matcher.ElementMatchers.none;

@ModEntry
public class FriendshipMod
{
    public static String modId = "FriendshipMod";

    static {


    }

    public void init() {
        WorldDataRegistry.registerWorldData(Relationships.dataKey, Relationships.class);
        WorldDataRegistry.registerWorldData(TicketManager.dataKey, TicketManager.class);
        WorldDataRegistry.registerWorldData(Personalities.dataKey, Personalities.class);
        PacketRegistry.registerPacket(RelationshipPacket.class);
        PacketRegistry.registerPacket(RelationshipsPacket.class);
        PacketRegistry.registerPacket(PersonalityPacket.class);
    }

    public void initResources() {
    }

    public void postInit() {
    }

}
