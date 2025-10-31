package friendshipMod.patches;

import friendshipMod.FriendshipMod;
import friendshipMod.data.Relationship;
import friendshipMod.data.Relationships;
import friendshipMod.data.Ticket;
import friendshipMod.data.TicketManager;
import friendshipMod.packets.RelationshipPacket;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.util.GameMath;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.ai.behaviourTree.Blackboard;
import necesse.entity.mobs.ai.behaviourTree.leaves.HumanInteractWithSettlerAINode;
import necesse.entity.mobs.friendly.human.HumanMob;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.utility.RandomString;

import java.util.Optional;
import java.util.Random;

@ModMethodPatch(target = HumanInteractWithSettlerAINode.class, name = "tickNode", arguments = {HumanMob.class, Blackboard.class})
public class FriendshipInteractionPatch {
    static final int ticketWeight = 70;
    static final int relationshipWeight = 30;

    @Advice.OnMethodExit
    static void onExit(@Advice.This HumanInteractWithSettlerAINode<HumanMob> node,
                       @Advice.Argument(0) HumanMob mob,
                       @Advice.Argument(1) Blackboard<HumanMob> blackboard,
                       @Advice.FieldValue("settlerCurrentlyInteractingWith") HumanMob other,
                       @Advice.FieldValue(value = "interactionPositive", readOnly = false) boolean interactionPositive,
                       @Advice.FieldValue("currentInteractionStageTicker") int stageTicker) {
        if (mob.isServer()) {

            if (stageTicker == 20) {
                Relationships relationships = Relationships.getRelationships(mob.getWorldEntity());
                Relationship relationship = relationships.getRelationship(mob, other);
                float relationshipPercent = (relationship.score + Math.abs(Relationships.min)) / (float)Relationships.getRange();
                float relationshipChance = GameMath.lerp(relationshipPercent, 0, relationshipWeight);

                Optional<Ticket> decidedTicket = TicketManager.getTicketManager(mob.getWorldEntity()).popDecidedTicket(mob.getUniqueID());
                float ticketChance = ticketWeight;
                if (decidedTicket.isPresent()) {
                    if (decidedTicket.get().kind == Ticket.Kind.Human) {
                        Relationship relationshipWithTicket = relationships.getRelationship(mob.getUniqueID(), decidedTicket.get().ticketId);
                        Relationship otherRelationshipWithTicket = relationships.getRelationship(other.getUniqueID(), decidedTicket.get().ticketId);
                        float distance;
                        if (relationshipWithTicket.score > otherRelationshipWithTicket.score) {
                            distance = relationshipWithTicket.score - otherRelationshipWithTicket.score;
                        } else {
                            distance = otherRelationshipWithTicket.score - relationshipWithTicket.score;
                        }
                        float ticketPercent = (Relationships.getRange() - distance) / Relationships.getRange();
                        ticketChance = GameMath.lerp(ticketPercent, 0, ticketWeight);
                    }
                }

                interactionPositive = GameRandom.globalRandom.getChance(relationshipChance + ticketChance);
            }

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
