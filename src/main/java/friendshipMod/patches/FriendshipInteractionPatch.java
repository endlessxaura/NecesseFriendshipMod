package friendshipMod.patches;

import friendshipMod.FriendshipMod;
import friendshipMod.data.Relationship;
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
                Relationship relationship = relationships.getRelationship(mob, other);
                if (interactionPositive) {
                    relationship.score += 1;
                } else {
                    relationship.score -= 1;
                }
                relationships.setRelationship(relationship);
                mob.getLevel().getServer().network.sendToClientsWithEntity(
                    new RelationshipPacket(relationship),
                    mob
                );
                System.out.println(FriendshipMod.modId + ": Server update sent for (" + mob.getUniqueID() + ", " + other.getUniqueID() + ")");
            }
        }
    }
}
