package friendshipMod.patches;

import friendshipMod.data.Personalities;
import friendshipMod.data.Relationships;
import friendshipMod.packets.PersonalityRequestPacket;
import friendshipMod.packets.RelationshipRequestPacket;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.friendly.human.HumanMob;
import net.bytebuddy.asm.Advice;

@ModMethodPatch(target = Mob.class, name = "requestServerUpdate", arguments = {})
public class MobRequestServerUpdatePatch {

    @Advice.OnMethodExit
    public static void onExit(
            @Advice.This Mob mob
    ) {
        if (mob.isClient() && mob instanceof HumanMob) {
            Personalities personalities = Personalities.getInstance(mob.getWorldEntity());
            if (!personalities.hasPersonalityFor(mob)) {
                PersonalityRequestPacket requestPacket = new PersonalityRequestPacket(mob.getUniqueID());
                mob.getClient().network.sendPacket(requestPacket);
            }

            Relationships relationships = Relationships.getInstance(mob.getWorldEntity());
            if (relationships.getRelationshipsFor(mob.getUniqueID()).isEmpty()) {
                RelationshipRequestPacket requestPacket = new RelationshipRequestPacket(mob.getUniqueID());
                mob.getClient().network.sendPacket(requestPacket);
            }
        }
    }
}
