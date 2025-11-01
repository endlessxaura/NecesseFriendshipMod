package friendshipMod.data;

import necesse.engine.registries.ItemRegistry;
import necesse.engine.registries.MobRegistry;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.friendly.HusbandryMob;
import necesse.inventory.item.Item;
import necesse.inventory.recipe.Recipe;
import necesse.inventory.recipe.Recipes;
import necesse.level.maps.Level;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Personality {
    public List<String> likes;
    public List<String> dislikes;
    public static final int foodLikes = 5;
    public static final int furnitureLikes = 2;
    public static final int animalLikes = 1;

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
                                .filter(x -> x.isCritter)
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
            Item item = items.get(GameRandom.globalRandom.getIntBetween(0, items.size()));
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
            Mob mob = mobs.get(GameRandom.globalRandom.getIntBetween(0, mobs.size()));
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
}
