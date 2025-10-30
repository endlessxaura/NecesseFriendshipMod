package friendshipMod.patches;


import friendshipMod.data.Relationships;
import friendshipMod.packets.RelationshipsPacket;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.entity.manager.EntityManager;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.friendly.human.HumanMob;
import net.bytebuddy.asm.Advice;

@ModMethodPatch(target = EntityManager.class, name = "addMob", arguments = {Mob.class, float.class, float.class})
public class RelationshipsOnMobSpawnMethodPatch {
    @Advice.OnMethodExit
    static void onExit(
        @Advice.This EntityManager entityManager,
        @Advice.Argument(0) Mob mob
    ) {
        if (entityManager.level.isServer() && mob.shouldSendSpawnPacket() && mob instanceof HumanMob) {
            Relationships relationships = Relationships.getRelationships(entityManager.level.getWorldEntity());
            RelationshipsPacket packet = new RelationshipsPacket(relationships.getRelationshipsFor(mob));
            entityManager.level.getServer().network.sendToClientsWithEntity(packet, mob);
            System.out.println("FriendshipMod: Server sent relationships for " + mob.getDisplayName());
        }
    }

}
