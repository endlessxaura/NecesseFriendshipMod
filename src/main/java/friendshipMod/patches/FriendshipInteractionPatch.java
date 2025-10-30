package friendshipMod.patches;

import friendshipMod.FriendshipMod;
import friendshipMod.data.Relationships;
import friendshipMod.packets.RelationshipPacket;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.entity.mobs.ai.behaviourTree.Blackboard;
import necesse.entity.mobs.ai.behaviourTree.leaves.HumanInteractWithSettlerAINode;
import necesse.entity.mobs.friendly.human.HumanMob;
import net.bytebuddy.asm.Advice;

@ModMethodPatch(target = HumanInteractWithSettlerAINode.class, name = "tickNode", arguments = {HumanMob.class, Blackboard.class})
public class FriendshipInteractionPatch {
    @Advice.OnMethodExit
    static void onExit(@Advice.This HumanInteractWithSettlerAINode<HumanMob> node,
                       @Advice.Argument(0) HumanMob mob,
                       @Advice.Argument(1) Blackboard<HumanMob> blackboard,
                       @Advice.FieldValue("settlerCurrentlyInteractingWith") HumanMob other,
                       @Advice.FieldValue("interactionPositive") boolean interactionPositive,
                       @Advice.FieldValue("currentInteractionStageTicker") int stageTicker) {
        if (mob.isServer()) {
            if (stageTicker == 80) {
                Relationships relationships = Relationships.getRelationships(mob.getWorldEntity());
                int score = relationships.getRelationship(mob, other);
                if (interactionPositive) {
                    relationships.setRelationship(mob, other, score + 1);
                    mob.getLevel().getServer().network.sendToClientsWithEntity(
                            new RelationshipPacket(mob, other, score + 1),
                            mob
                    );
                    System.out.println(FriendshipMod.modId + ": Server update sent for (" + mob.getUniqueID() + ", " + other.getUniqueID() + ") with increment of 1");
                } else {
                    relationships.setRelationship(mob, other, score - 1);
                    mob.getLevel().getServer().network.sendToClientsWithEntity(
                            new RelationshipPacket(mob, other, score - 1),
                            mob
                    );
                    System.out.println(FriendshipMod.modId + ": Server update sent for (" + mob.getUniqueID() + ", " + other.getUniqueID() + ") with decrement of 1");
                }
            }
        }
    }
}
