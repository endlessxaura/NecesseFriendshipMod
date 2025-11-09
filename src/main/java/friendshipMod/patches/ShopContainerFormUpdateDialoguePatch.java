package friendshipMod.patches;

import friendshipMod.FriendshipMod;
import friendshipMod.data.Personalities;
import friendshipMod.data.Personality;
import friendshipMod.data.Relationship;
import friendshipMod.data.Relationships;
import friendshipMod.packets.RelationshipPacket;
import friendshipMod.utilities.Messages;
import necesse.engine.localization.message.GameMessage;
import necesse.engine.localization.message.GameMessageBuilder;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.world.WorldEntity;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.forms.ContainerComponent;
import necesse.gfx.forms.presets.containerComponent.mob.DialogueForm;
import necesse.gfx.forms.presets.containerComponent.mob.ShopContainerForm;
import necesse.inventory.InventoryItem;
import necesse.inventory.PlayerInventoryManager;
import necesse.inventory.container.mob.ShopContainer;
import necesse.inventory.container.mob.ShopContainerPartyUpdateEvent;
import net.bytebuddy.asm.Advice;

import java.util.function.Consumer;

@ModMethodPatch(target = ShopContainerForm.class, name = "updateDialogue", arguments = {})
public class ShopContainerFormUpdateDialoguePatch {

    public static class CloseRunner implements Runnable {
        public ShopContainerForm<ShopContainer> container;

        public CloseRunner(ShopContainerForm<ShopContainer> container) {
            this.container = container;
        }

        @Override
        public void run() {
            container.getClient().closeContainer(true);
        }
    }

    public static class ConversationRunner implements Runnable {
        public ShopContainerForm<ShopContainer> container;
        public DialogueForm conversationForm;
        public Personality personality;

        public ConversationRunner(
                ShopContainerForm<ShopContainer> container,
                DialogueForm conversationForm,
                Personality personality
        ) {
            this.container = container;
            this.conversationForm = conversationForm;
            this.personality = personality;
        }

        @Override
        public void run() {
            conversationForm.reset(container.header, Messages.getRandomMessageFor(container.getContainer().getMob()));
            addConversationOptions(container, conversationForm, personality);
        }
    }

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

    public static class ImpactfulDialogueRunner extends DialogRunner {
        int value;
        String type;

        public ImpactfulDialogueRunner(ShopContainerForm<ShopContainer> container, DialogueForm form, int value, String type) {
            super(container, form);
            this.value = value;
            this.type = type;
        }

        @Override
        public void run() {
            super.run();
            PlayerMob player = container.getClient().getPlayer();
            Mob mob = container.getContainer().getMob();
            if (player != null && mob != null) {
                Relationships relationships = Relationships.getInstance(container.getClient().worldEntity);
                if (!relationships.recentlyModified(player, mob, type)) {
                    Relationship relationship = relationships.getRelationship(player, mob);
                    relationship.score += value;
                    relationships.setRelationship(relationship, type);
                    RelationshipPacket packet = new RelationshipPacket(relationship, type);
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

    public static class GiftDialogueRunner extends DialogRunner {
        Personality personality;
        InventoryItem inventoryItem;

        public GiftDialogueRunner(
                ShopContainerForm<ShopContainer> container,
                DialogueForm form,
                Personality personality,
                InventoryItem inventoryItem
        ) {
            super(container, form);
            this.personality = personality;
            this.inventoryItem = inventoryItem;

        }

        @Override
        public void run() {
            PlayerMob player = container.getClient().getPlayer();
            Mob mob = container.getContainer().getMob();
            int remainingItems = player.getInv().removeItems(inventoryItem.item, 1, true, true, true, true, FriendshipMod.modId + "GiftGiving");
            Relationships relationships = Relationships.getInstance(container.getClient().worldEntity);
            Relationship relationship = relationships.getRelationship(player, mob);

            GameMessageBuilder message = new GameMessageBuilder();
            if (relationships.recentlyModified(relationship.getAssociation(), Relationships.AdjustmentTypes.Gift)) {
                message.append("I appreciate it, but you've already given me something recently!");
            } else {
                int value = 0;
                if (inventoryItem != null && remainingItems == 1) {
                    if (personality.likes(inventoryItem.item)) {
                        message.append("Thanks, I like this a lot!");
                        value = 5;
                    } else if (personality.dislikes(inventoryItem.item)) {
                        message.append("Oh. Um. Sure...");
                        value = 1;
                    } else {
                        message.append("Thanks.");
                        value = -5;
                    }
                } else {
                    message.append("But you don't have one.");
                    value = -1;
                }

                relationship.score += value;
                relationships.setRelationship(relationship, Relationships.AdjustmentTypes.Gift);
                RelationshipPacket packet = new RelationshipPacket(relationship, Relationships.AdjustmentTypes.Gift);
                System.out.println(FriendshipMod.modId + ": Client update sent for (" + player.getUniqueID() + ", " + mob.getUniqueID() + ")");
                player.getClient().network.sendPacket(packet);
            }

            form.reset(container.header, message);
            form.addDialogueOption(
                    new GameMessageBuilder().append("Goodbye."),
                    new CloseRunner(container)
            );
            super.run();
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

    public static void addConversationOptions(
            ShopContainerForm<ShopContainer> container,
            DialogueForm conversationForm,
            Personality personality
    ) {
        conversationForm.addDialogueOption(
                new GameMessageBuilder().append("What else?"),
                new ConversationRunner(container, conversationForm, personality)
        );
        conversationForm.addDialogueOption(
                new GameMessageBuilder().append("Goodbye."),
                new CloseRunner(container)
        );
    }

    public static DialogueForm generateConversationForm(
            ShopContainerForm<ShopContainer> container,
            Personality personality
    ) {
        DialogueForm conversationForm = new DialogueForm(
                FriendshipMod.modId + "ConversationForm",
                container.width,
                container.height,
                container.header,
                Messages.getRandomMessageFor(container.getContainer().getMob())
        );
        addConversationOptions(
                container,
                conversationForm,
                personality
        );
        return conversationForm;
    }

    public static DialogueForm generateGiftForm(
            ShopContainerForm<ShopContainer> container,
            Personality personality
    ) {
        return new DialogueForm(
                FriendshipMod.modId + "GiftForm",
                container.width,
                container.height,
                container.header,
                new GameMessageBuilder().append("")
        );
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
        DialogueForm conversationForm = generateConversationForm(shopContainerForm, personality);
        conversationForm = shopContainerForm.addComponent(conversationForm);
        ContainerComponent.setPosFocus(conversationForm);
        shopContainerForm.dialogueForm.addDialogueOption(
                new GameMessageBuilder().append("Let's talk."),
                new ImpactfulDialogueRunner(shopContainerForm, conversationForm, 1, Relationships.AdjustmentTypes.Talk)
        );
        shopContainerForm.dialogueForm.setHeight(Math.max(shopContainerForm.dialogueForm.getContentHeight() + 5, shopContainerForm.height));

        DialogueForm giftForm = generateGiftForm(shopContainerForm, personality);
        giftForm = shopContainerForm.addComponent(giftForm);
        ContainerComponent.setPosFocus(giftForm);
        InventoryItem inventoryItem = shopContainerForm.getClient().getPlayer().getSelectedItem();
        if (inventoryItem != null) {
            shopContainerForm.dialogueForm.addDialogueOption(
                    new GameMessageBuilder().append("This " + inventoryItem.getItemDisplayName() + " is a gift for you!"),
                    new GiftDialogueRunner(shopContainerForm, giftForm, personality, inventoryItem)
            );
        }
    }
}
