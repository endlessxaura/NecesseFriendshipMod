package friendshipMod.patches;

import friendshipMod.data.TicketManager;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.friendly.human.HumanMob;
import net.bytebuddy.asm.Advice;

/**
 * This captures the thought ticket that a settler sends during a conversation.
 */
@ModMethodPatch(target = HumanMob.ShowOtherSettlerThoughtMobAbility.class, name = "runAndSend", arguments = {Mob.class, int.class})
public class ShowOtherSettlerThoughtPatch {
    @Advice.OnMethodExit
    public static void onExit(
            @Advice.This HumanMob.ShowOtherSettlerThoughtMobAbility ability,
            @Advice.Argument(0) Mob target
    ) {
        TicketManager
                .getTicketManager(ability.getMob().getWorldEntity())
                .setDecidedTicket(ability.getMob().getUniqueID(), target);
    }

}
