package friendshipMod.patches;

import friendshipMod.FriendshipMod;
import friendshipMod.data.Personalities;
import friendshipMod.data.Personality;
import friendshipMod.data.Relationship;
import friendshipMod.data.Relationships;
import friendshipMod.packets.RelationshipPacket;
import necesse.engine.localization.message.GameMessageBuilder;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.forms.ContainerComponent;
import necesse.gfx.forms.presets.containerComponent.mob.DialogueForm;
import necesse.gfx.forms.presets.containerComponent.mob.ShopContainerForm;
import necesse.inventory.container.mob.ShopContainer;
import necesse.inventory.container.mob.ShopContainerPartyUpdateEvent;
import net.bytebuddy.asm.Advice;

import java.util.function.Consumer;

@ModMethodPatch(target = ShopContainerForm.class, name = "updateDialogue", arguments = {})
public class ShopContainerFormUpdateDialoguePatch {
    public static class DialogRunner implements Runnable {
        public ShopContainerForm<ShopContainer> container;
        public DialogueForm form;

        public DialogRunner(ShopContainerForm<ShopContainer> container, DialogueForm form) {
            this.container = container;
            this.form = form;
        }

        @Override
        public void run() {
            container.makeCurrent(form);
        }
    }

    public static class DialogRunnerPositive extends DialogRunner {
        public DialogRunnerPositive(ShopContainerForm<ShopContainer> container, DialogueForm form) {
            super(container, form);
        }

        @Override
        public void run() {
            super.run();
            PlayerMob player = container.getClient().getPlayer();
            Mob mob = container.getContainer().getMob();
            if (player != null && mob != null) {
                Relationships relationships = Relationships.getInstance(container.getClient().worldEntity);
                if (!relationships.recentlyModified(player, mob)) {
                    Relationship relationship = relationships.getRelationship(player, mob);
                    relationship.score += 1;
                    relationships.setRelationship(relationship);
                    RelationshipPacket packet = new RelationshipPacket(relationship);
                    if (player.isServer()) {
                        player.getServerClient().sendPacket(packet);
                    } else {
                        player.getClientClient().getClient().network.sendPacket(packet);
                    }
                    System.out.println(FriendshipMod.modId + ": Client update sent for (" + player.getUniqueID() + ", " + mob.getUniqueID() + ")");
                }
            }
        }
    }

    public static class Resizer implements Consumer<ShopContainerPartyUpdateEvent> {
        DialogueForm form;

        public Resizer(DialogueForm form) {
            this.form = form;
        }

        @Override
        public void accept(ShopContainerPartyUpdateEvent shopContainerPartyUpdateEvent) {
            ContainerComponent.setPosFocus(form);
        }
    }

    @Advice.OnMethodExit
    public static void onExit(
        @Advice.This ShopContainerForm<ShopContainer> shopContainerForm
    ) {
        Personalities personalities = Personalities.getInstance(shopContainerForm.getClient().worldEntity);
        Personality personality = personalities.getPersonalityFor(shopContainerForm.getContainer().getMob());
        if (personality == null) {
            return;
        }
        DialogueForm conversationForm = new DialogueForm(
                FriendshipMod.modId + "ConversationForm",
                shopContainerForm.width,
                shopContainerForm.height,
                shopContainerForm.header,
                personality.getRandomMessage(shopContainerForm.getClient().getLevel())
        );
        conversationForm = shopContainerForm.addComponent(conversationForm);
        ContainerComponent.setPosFocus(conversationForm);
        // TODO: respond to resizing
        shopContainerForm.dialogueForm.addDialogueOption(
                new GameMessageBuilder().append("Let's talk."),
                new DialogRunnerPositive(shopContainerForm, conversationForm)
        );
        shopContainerForm.dialogueForm.setHeight(Math.max(shopContainerForm.dialogueForm.getContentHeight() + 5, shopContainerForm.height));
    }
}
