package friendshipMod.data;

import necesse.engine.localization.message.GameMessage;
import necesse.engine.localization.message.GameMessageBuilder;
import necesse.engine.registries.ItemRegistry;
import necesse.engine.registries.MobRegistry;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.friendly.HusbandryMob;
import necesse.entity.mobs.friendly.human.HappinessModifier;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.Item;
import necesse.inventory.recipe.Recipe;
import necesse.inventory.recipe.Recipes;
import necesse.level.maps.Level;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Personality {
    // TODO: Personality traits
    public List<String> likes;
    public List<String> dislikes;
    public static final int foodLikes = 5;
    public static final int furnitureLikes = 2;
    public static final int animalLikes = 1;
    public static final String likeMessageFormat = "I think my %s is beautiful!";
    public static final String dislikeMessageFormat = "I think my %s is ugly...";

    // region Constructors
    public Personality() {
        likes = new LinkedList<>();
        dislikes = new LinkedList<>();
    }

    public static Personality generatePersonality(Level level) {
        Personality newPersonality = new Personality();
        newPersonality.generateLikes(level);
        newPersonality.generateDislikes(level);
        return newPersonality;
    }
    // endregion

    // region Generators
    private void generateLikes(Level level) {
        List<Item> allItems = ItemRegistry.getItems();
        likes = generateSetFromItems(
                allItems
                        .stream()
                        .filter(Item::isFoodItem)
                        .filter(x -> !dislikes.contains(x.getStringID()))
                        .filter(x -> Recipes.getRecipesFromResult(x.getID()).isEmpty())
                        .collect(Collectors.toList()),
                foodLikes
        );
        likes.addAll(
                generateSetFromItems(
                        allItems
                                .stream()
                                .filter(Item::isPlaceable)
                                .filter(x -> !dislikes.contains(x.getStringID()))
                                .filter(x -> !Recipes.getRecipesFromResult(x.getID()).isEmpty())
                                .collect(Collectors.toList()),
                        furnitureLikes
                )
        );
        likes.addAll(
                generateSetFromMobs(
                        MobRegistry
                                .getMobs()
                                .stream()
                                .map(x -> MobRegistry.getMob(x.getStringID(), level))
                                .filter(x -> !dislikes.contains(x.getStringID()))
                                .filter(x -> x instanceof HusbandryMob)
                                .collect(Collectors.toList()),
                        animalLikes
                )
        );
    }

    private void generateDislikes(Level level) {
        List<Item> allItems = ItemRegistry.getItems();
        dislikes = generateSetFromItems(
                ItemRegistry
                        .getItems()
                        .stream()
                        .filter(Item::isFoodItem)
                        .filter(x -> !likes.contains(x.getStringID()))
                        .filter(x -> Recipes.getRecipesFromResult(x.getID()).isEmpty())
                        .collect(Collectors.toList()),
                foodLikes
        );
        dislikes.addAll(
                generateSetFromItems(
                        ItemRegistry
                                .getItems()
                                .stream()
                                .filter(Item::isPlaceable)
                                .filter(x -> !likes.contains(x.getStringID()))
                                .filter(x -> !Recipes.getRecipesFromResult(x.getID()).isEmpty())
                                .collect(Collectors.toList()),
                        furnitureLikes
                )
        );
        dislikes.addAll(
                generateSetFromMobs(
                        MobRegistry
                                .getMobs()
                                .stream()
                                .map(x -> MobRegistry.getMob(x.getStringID(), level))
                                .filter(x -> !likes.contains(x.getStringID()))
                                .filter(x -> x instanceof HusbandryMob)
                                .collect(Collectors.toList()),
                        animalLikes
                )
        );
    }

    /**
     * Generates a set of liked items from a list of items
     * @param items The item list to select from
     * @return a list of item ids, including any recipe results that use them as an ingredient
     */
    private static List<String> generateSetFromItems(List<Item> items, int count) {
        LinkedList<String> liked = new LinkedList<>();
        for (int i = 0; i < count; i++) {
            Item item = items.get(GameRandom.globalRandom.getIntBetween(0, items.size() - 1));
            liked.add(item.getStringID());
            List<Recipe> recipes = Recipes.getRecipesFromIngredient(item.getID());
            for (Recipe recipe : recipes) {
                liked.add(recipe.resultStringID);
            }
        }
        return liked;
    }

    private static List<String> generateSetFromMobs(List<Mob> mobs, int count) {
        LinkedList<String> liked = new LinkedList<>();
        for (int i = 0; i < count; i++) {
            Mob mob = mobs.get(GameRandom.globalRandom.getIntBetween(0, mobs.size() - 1));
            liked.add(mob.getStringID());
        }
        return liked;
    }
    // endregion

    public void setLike(String itemId) {
        likes.add(itemId);
    }

    public void setDislike(String itemId) {
        dislikes.add(itemId);
    }

    public boolean likes(String itemId) {
        return likes.contains(itemId);
    }

    public boolean likes(Item item) {
        return likes(item.getStringID());
    }

    public boolean dislikes(String itemId) {
        return dislikes.contains(itemId);
    }

    public boolean dislikes(Item item) {
        return dislikes(item.getStringID());
    }

    public HappinessModifier getModifierFor(Item item) {
        String message;
        int value;
        if (likes(item)) {
            message = String.format(likeMessageFormat, item.getDisplayName(new InventoryItem(item)).toLowerCase());
            value = -5;
        } else if (dislikes(item)) {
            message = String.format(dislikeMessageFormat, item.getDisplayName(new InventoryItem(item)).toLowerCase());
            value = +5;
        } else {
            return null;
        }
        GameMessage gameMessage = new GameMessageBuilder().append(message);
        return new HappinessModifier(value, gameMessage);
    }

    public GameMessage getRandomMessage(Level level) {
        int randomThing = GameRandom.globalRandom.getIntBetween(0, likes.size() + dislikes.size());
        String thingId;
        boolean liked = randomThing < likes.size();
        if (liked) {
            thingId = likes.get(randomThing);
        } else {
            thingId = dislikes.get(randomThing);
        }
        Item possibleItem = ItemRegistry.getItem(thingId);
        if (possibleItem != null) {
            InventoryItem inventoryItem = new InventoryItem(possibleItem);
            if (possibleItem.isFoodItem()) {
                return new GameMessageBuilder().append("I could really go for a " + inventoryItem.getItemDisplayName().toLowerCase() + " right now...");
            } else if (liked) {
                return new GameMessageBuilder().append("I think a " + inventoryItem.getItemDisplayName().toLowerCase() + " would look great in my room.");
            } else {
                return new GameMessageBuilder().append("I think a " + inventoryItem.getItemDisplayName().toLowerCase() + " just looks so ugly.");
            }
        } else {
            Mob possibleMob = MobRegistry.getMob(thingId, level);
            if (possibleMob != null) {
                if (liked) {
                    return new GameMessageBuilder().append("Have you seen " + possibleMob.getDisplayName().toLowerCase() + "? They are so cute!");
                } else {
                    return new GameMessageBuilder().append("I just think " + possibleMob.getDisplayName().toLowerCase() + " are annoying.");
                }
            }
        }
        return new GameMessageBuilder().append("Hmmm");
    }
}
