package friendshipMod.patches;

import friendshipMod.data.Relationship;
import friendshipMod.data.Relationships;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.entity.manager.EntityManager;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.ai.behaviourTree.leaves.HumanInteractWithSettlerAINode;
import necesse.entity.mobs.friendly.human.HumanMob;
import net.bytebuddy.asm.Advice;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This override determines the settlers to choose a conversation topic.
 * Instead of pulling from the nearby tiles, it pulls from that mob's relationships.
 */
@ModMethodPatch(target = HumanInteractWithSettlerAINode.class, name = "findRandomSettler", arguments = {Mob.class, int.class, int.class})
public class RandomSettlerPatch {
    @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    public static List<HumanMob> override(
            @Advice.This HumanInteractWithSettlerAINode<HumanMob> node,
            @Advice.Argument(0) HumanMob mob,
            @Advice.Argument(1) int maxTileRange,
            @Advice.Argument(2) int limit
    ) {
        Relationships relationships = Relationships.getRelationships(mob.getWorldEntity());
        List<Relationship> mobRelationships = relationships.getRelationshipsFor(mob);
        return mobRelationships
                .stream()
                .map(x -> {
                    if (x.getAssociation().first() != mob.getUniqueID()) {
                        return x.getAssociation().first();
                    } else {
                        return x.getAssociation().second();
                    }
                })
                .map(x -> mob
                        .getLevel()
                        .entityManager
                        .mobs
                        .streamAreaTileRange(mob.getX(), mob.getY(), maxTileRange)
                        .filter(y -> y instanceof HumanMob)
                        .filter(y -> y.getUniqueID() == x)
                        .findFirst())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(x -> (HumanMob)x)
                .limit(limit)
                .collect(Collectors.toList());
    }
}
