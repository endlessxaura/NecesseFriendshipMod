package friendshipMod.patches;

import friendshipMod.FriendshipMod;
import friendshipMod.data.*;
import friendshipMod.packets.RelationshipPacket;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.util.GameMath;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.ai.behaviourTree.Blackboard;
import necesse.entity.mobs.ai.behaviourTree.leaves.HumanInteractWithSettlerAINode;
import necesse.entity.mobs.friendly.human.HumanMob;
import net.bytebuddy.asm.Advice;

import java.util.Optional;

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
        if (!mob.isServer() || other == null) {
            return;
        }

        Personalities personalities = Personalities.getInstance(mob.getWorldEntity());
        Relationships relationships = Relationships.getInstance(mob.getWorldEntity());

        if (relationships.recentlyModified(mob, other)) {
            return;
        }

        if (stageTicker == 20) {
            Relationship relationship = relationships.getRelationship(mob, other);
            float relationshipPercent = (relationship.score + Math.abs(Relationships.min)) / (float) Relationships.getRange();
            float relationshipChance = GameMath.lerp(relationshipPercent, 0, relationshipWeight);

            Optional<Ticket> decidedTicket = TicketManager.getTicketManager(mob.getWorldEntity()).popDecidedTicket(mob.getUniqueID());
            float ticketChance = ticketWeight;
            if (decidedTicket.isPresent()) {
                if (decidedTicket.get().kind == Ticket.Kind.Human) {
                    int talkedAboutMobId = Integer.parseInt(decidedTicket.get().ticketId);
                    Relationship relationshipWithTicket = relationships.getRelationship(mob.getUniqueID(), talkedAboutMobId);
                    Relationship otherRelationshipWithTicket = relationships.getRelationship(other.getUniqueID(), talkedAboutMobId);
                    float distance;
                    if (relationshipWithTicket.score > otherRelationshipWithTicket.score) {
                        distance = relationshipWithTicket.score - otherRelationshipWithTicket.score;
                    } else {
                        distance = otherRelationshipWithTicket.score - relationshipWithTicket.score;
                    }
                    float ticketPercent = (Relationships.getRange() - distance) / Relationships.getRange();
                    ticketChance = GameMath.lerp(ticketPercent, 0, ticketWeight);
                }

                if (decidedTicket.get().kind == Ticket.Kind.Item || decidedTicket.get().kind == Ticket.Kind.Animal) {
                    Personality personality = personalities.getPersonalityFor(mob);
                    Personality otherPersonality = personalities.getPersonalityFor(other);
                    if (
                            (personality.likes(decidedTicket.get().ticketId) && otherPersonality.likes(decidedTicket.get().ticketId))
                                    || (personality.dislikes(decidedTicket.get().ticketId) && otherPersonality.dislikes(decidedTicket.get().ticketId))
                    ) {
                        ticketChance = ticketWeight;
                    } else if (
                            (personality.likes(decidedTicket.get().ticketId) && otherPersonality.dislikes(decidedTicket.get().ticketId))
                                    || (personality.dislikes(decidedTicket.get().ticketId) && otherPersonality.likes(decidedTicket.get().ticketId))
                    ) {
                        ticketChance = 0;
                    } else {
                        ticketChance = ticketWeight / 2f;
                    }
                }
            }

            interactionPositive = GameRandom.globalRandom.getChance(relationshipChance + ticketChance);
        }

        if (stageTicker == 80) {
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
