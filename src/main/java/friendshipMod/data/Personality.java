package friendshipMod.data;

import necesse.engine.registries.ItemRegistry;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.Mob;
import necesse.inventory.item.Item;
import necesse.inventory.recipe.Recipe;
import necesse.inventory.recipe.Recipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Personality {
    public List<Integer> likes;
    public List<Integer> dislikes;
    public static final int foodLikes = 5;
    public static final int furnitureLikes = 2;

    // region Constructors
    public Personality() {
        likes = new LinkedList<>();
        dislikes = new LinkedList<>();
    }

    public static Personality generatePersonality() {
        Personality newPersonality = new Personality();
        newPersonality.generateLikes();
        newPersonality.generateDislikes();
        return newPersonality;
    }
    // endregion

    // region Generators
    private void generateLikes() {
        List<Item> allItems = ItemRegistry.getItems();
        likes = generateSetFrom(
                allItems
                        .stream()
                        .filter(Item::isFoodItem)
                        .filter(x -> !dislikes.contains(x.getID()))
                        .filter(x -> Recipes.getRecipesFromResult(x.getID()).isEmpty())
                        .collect(Collectors.toList()),
                foodLikes
        );
        likes.addAll(
                generateSetFrom(
                        allItems
                                .stream()
                                .filter(Item::isPlaceable)
                                .filter(x -> !dislikes.contains(x.getID()))
                                .filter(x -> !Recipes.getRecipesFromResult(x.getID()).isEmpty())
                                .collect(Collectors.toList()),
                        furnitureLikes
                )
        );
    }

    private void generateDislikes() {
        List<Item> allItems = ItemRegistry.getItems();
        dislikes = generateSetFrom(
                ItemRegistry
                        .getItems()
                        .stream()
                        .filter(Item::isFoodItem)
                        .filter(x -> !likes.contains(x.getID()))
                        .filter(x -> Recipes.getRecipesFromResult(x.getID()).isEmpty())
                        .collect(Collectors.toList()),
                foodLikes
        );
        dislikes.addAll(
                generateSetFrom(
                        ItemRegistry
                                .getItems()
                                .stream()
                                .filter(Item::isPlaceable)
                                .filter(x -> !likes.contains(x.getID()))
                                .filter(x -> !Recipes.getRecipesFromResult(x.getID()).isEmpty())
                                .collect(Collectors.toList()),
                        furnitureLikes
                )
        );
    }

    /**
     * Generates a set of liked items from a list of items
     * @param items The item list to select from
     * @return a list of item ids, including any recipe results that use them as an ingredient
     */
    private static List<Integer> generateSetFrom(List<Item> items, int count) {
        LinkedList<Integer> liked = new LinkedList<>();
        for (int i = 0; i < count; i++) {
            Item item = items.get(GameRandom.globalRandom.getIntBetween(0, items.size()));
            liked.add(item.getID());
            List<Recipe> recipes = Recipes.getRecipesFromIngredient(item.getID());
            for (Recipe recipe : recipes) {
                liked.add(recipe.resultID);
            }
        }
        return liked;
    }
    // endregion

    public void setLike(Integer itemId) {
        likes.add(itemId);
    }

    public void setDislike(Integer itemId) {
        dislikes.add(itemId);
    }

    public boolean likes(Integer itemId) {
        return likes.contains(itemId);
    }

    public boolean likes(Item item) {
        return likes(item.getID());
    }

    public boolean dislikes(Integer itemId) {
        return dislikes.contains(itemId);
    }

    public boolean dislikes(Item item) {
        return dislikes(item.getID());
    }
}
