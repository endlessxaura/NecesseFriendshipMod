package friendshipMod.patches;

import friendshipMod.ai.HumanGiveGiftAINode;
import necesse.engine.modLoader.annotations.ModConstructorPatch;
import necesse.entity.mobs.ai.behaviourTree.trees.HumanAI;
import necesse.entity.mobs.friendly.human.HumanMob;
import net.bytebuddy.asm.Advice;

@ModConstructorPatch(target = HumanAI.class, arguments = {int.class, boolean.class, boolean.class, int.class})
public class HumanAIConstructorPatch {
    @Advice.OnMethodExit
    public static void onExit(@Advice.This HumanAI<HumanMob> ai) {
        ai.addChild(new HumanGiveGiftAINode<>());
    }
}
