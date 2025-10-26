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
                       @Advice.FieldValue("interactionPositive") boolean interactionPositive,
                       @Advice.FieldValue("currentInteractionStageTicker") int stageTicker) {
        if (stageTicker == 80) {
            if (interactionPositive) {
                System.out.println("Positive interaction!");
            } else {
                System.out.println("Negative interaction!");
            }
        }
    }
}
