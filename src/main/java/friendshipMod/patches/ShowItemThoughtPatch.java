package friendshipMod.patches;

import friendshipMod.data.Ticket;
import friendshipMod.data.TicketManager;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.entity.mobs.friendly.human.HumanMob;
import necesse.inventory.item.Item;
import net.bytebuddy.asm.Advice;

/**
 * This captures the thought ticket that a settler sends during a conversation.
 */
@ModMethodPatch(target = HumanMob.ShowItemThoughtMobAbility.class, name = "runAndSend", arguments = {Item.class, int.class})
public class ShowItemThoughtPatch {
    @Advice.OnMethodExit
    public static void onExit(
            @Advice.This HumanMob.ShowOtherSettlerThoughtMobAbility ability,
            @Advice.Argument(0) Item target
    ) {
        TicketManager
                .getTicketManager(ability.getMob().getWorldEntity())
                .setDecidedTicket(ability.getMob().getUniqueID(), target.getStringID(), Ticket.Kind.Item);
    }
}
