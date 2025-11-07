package friendshipMod.patches;

import friendshipMod.data.Personalities;
import friendshipMod.data.Relationships;
import friendshipMod.packets.PersonalityRequestPacket;
import friendshipMod.packets.RelationshipRequestPacket;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.friendly.human.HumanMob;
import net.bytebuddy.asm.Advice;

import java.util.LinkedList;
import java.util.List;

@ModMethodPatch(target = Mob.class, name = "clientTick", arguments = {})
public class MobClientTickPatch {
    public static final List<Integer> requestedPersonalities = new LinkedList<>();
    public static final List<Integer> requestedRelationships = new LinkedList<>();
    public static long personalityRequestTime = 0; // These just prevent overloading the server
    public static long relationshipRequestTime = 0; // These just prevent overloading the server

    @Advice.OnMethodExit
    public static void onExit(
            @Advice.This Mob mob
    ) {
        if (mob.isClient() && mob instanceof HumanMob) {
            Personalities personalities = Personalities.getInstance(mob.getWorldEntity());
            Relationships relationships = Relationships.getInstance(mob.getWorldEntity());
            if (mob.getWorldTime() - personalityRequestTime > 500 && !personalities.hasPersonalityFor(mob) && !requestedPersonalities.contains(mob.getUniqueID())) {
                PersonalityRequestPacket requestPacket = new PersonalityRequestPacket(mob.getUniqueID());
                mob.getClient().network.sendPacket(requestPacket);
                personalityRequestTime = mob.getWorldTime();
                requestedPersonalities.add(mob.getUniqueID());
            }

            if (mob.getWorldTime() - relationshipRequestTime > 500 && relationships.getRelationshipsFor(mob.getUniqueID()).isEmpty() && !requestedRelationships.contains(mob.getUniqueID())) {
                RelationshipRequestPacket requestPacket = new RelationshipRequestPacket(mob.getUniqueID());
                mob.getClient().network.sendPacket(requestPacket);
                relationshipRequestTime = mob.getWorldTime();
                requestedRelationships.add(mob.getUniqueID());
            }
        }
    }
}
