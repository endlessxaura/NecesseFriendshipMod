package friendshipMod.ai;

import friendshipMod.data.Personalities;
import friendshipMod.data.Personality;
import friendshipMod.data.Relationship;
import friendshipMod.data.Relationships;
import necesse.engine.network.server.ServerClient;
import necesse.engine.registries.ItemRegistry;
import necesse.engine.util.GameMath;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.ai.behaviourTree.AINode;
import necesse.entity.mobs.ai.behaviourTree.AINodeResult;
import necesse.entity.mobs.ai.behaviourTree.Blackboard;
import necesse.entity.mobs.ai.behaviourTree.decorators.MoveTaskAINode;
import necesse.entity.mobs.ai.path.TilePathfinding;
import necesse.entity.mobs.friendly.human.HumanMob;
import necesse.entity.pickup.ItemPickupEntity;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.Item;
import necesse.level.maps.levelData.settlementData.ZoneTester;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class HumanGiveGiftAINode<T extends HumanMob> extends MoveTaskAINode<T> {
    PlayerMob playerInteractingWith = null;
    private int searchTime = 0;
    private long nextPathFindTime = 0L;

    private List<PlayerMob> findNearbyPlayers(T mob) {
        ZoneTester zoneTester = mob.getJobRestrictZone();
        return mob
                .getLevel()
                .entityManager
                .mobs
                .streamAreaTileRange(mob.getX(), mob.getY(), 30)
                .filter((m) -> m != mob)
                .filter((m) -> m instanceof PlayerMob)
                .map((m) -> (PlayerMob)m)
                .filter((m) -> mob.estimateCanMoveTo(m.getTileX(), m.getTileY(), true))
                .filter((m) -> zoneTester.containsTile(m.getTileX(), m.getTileY()))
                .findExtraDistance(1, Collectors.toList());
    }

    private InventoryItem getRandomGift(T mob) {
        Personality personality = Personalities.getInstance(mob.getWorldEntity()).getPersonalityFor(mob);
        List<Item> items = personality
                .likes
                .stream()
                .filter(ItemRegistry::itemExists)
                .map(ItemRegistry::getItem)
                .collect(Collectors.toList());
        Item item = items.get(GameRandom.globalRandom.getIntBetween(0, personality.likes.size() - 1));
        return new InventoryItem(item);
    }

    @Override
    public AINodeResult tickNode(T mob, Blackboard<T> blackboard) {
        // Finding player
        if (playerInteractingWith == null) {
            --searchTime;
            if (searchTime <= 0) {
                List<PlayerMob> nearbyPlayers = findNearbyPlayers(mob);
                Relationships relationships = Relationships.getInstance(mob.getWorldEntity());
                boolean foundPlayer = false;
                for (int i = 0; i < nearbyPlayers.size() && !foundPlayer; i++) {
                    PlayerMob nearbyPlayer = nearbyPlayers.get(i);
                    Relationship relationshipWithPlayer = relationships.getRelationship(nearbyPlayer, mob);
                    if (relationshipWithPlayer.getStatus() == Relationship.Status.Companion) {
                        playerInteractingWith = nearbyPlayer;
                        foundPlayer = true;
                    }
                }
            }
        }
        if (searchTime > 0) {
            return AINodeResult.FAILURE;
        }

        // Moving to player
        if (playerInteractingWith != null && mob.getDistance(this.playerInteractingWith) > 48.0F) {
            if (this.nextPathFindTime < mob.getLocalTime()) {
                this.nextPathFindTime = mob.getLocalTime() + 2000L;
                return moveToTileTask(
                        playerInteractingWith.getTileX(),
                        playerInteractingWith.getTileY(),
                        TilePathfinding.isAtOrAdjacentObject(
                                playerInteractingWith.getLevel(),
                                playerInteractingWith.getTileX(),
                                playerInteractingWith.getTileY()
                        ),
                        path -> {
                            if (path.moveIfWithin(-1, 1, () -> this.nextPathFindTime = 0L)) {
                                int nextPathTimeAdd = path.getNextPathTimeBasedOnPathTime(mob.getSpeed(), 1.5F, 2000, 0.1F);
                                this.nextPathFindTime = mob.getLocalTime() + (long)nextPathTimeAdd;
                                return AINodeResult.SUCCESS;
                            } else {
                                return AINodeResult.FAILURE;
                            }
                        }
                );
            }
        }

        // Interacting with player
        if (playerInteractingWith != null && mob.getDistance(this.playerInteractingWith) <= 48.0F) {
            Point2D.Float interactionDir = GameMath.normalize(
                    this.playerInteractingWith.x - mob.x,
                    this.playerInteractingWith.y - mob.y
            );
            if (blackboard.mover.isMoving()) {
                blackboard.mover.stopMoving(mob);
            }
            mob.setFacingDir(interactionDir.x, interactionDir.y);
            mob.showLoveThoughtAbility.runAndSend(6000);
            InventoryItem gift = getRandomGift(mob);
            Point publicLootPosition = mob.getLootDropsPosition((ServerClient)null);
            publicLootPosition.x = mob.getLevel().limitLevelXToBounds(publicLootPosition.x, 0, 32);
            publicLootPosition.y = mob.getLevel().limitLevelYToBounds(publicLootPosition.y, 0, 32);
            ItemPickupEntity entity = gift.getPickupEntity(mob.getLevel(), publicLootPosition.x, publicLootPosition.y);
            mob.getLevel().entityManager.pickups.add(entity);
            return AINodeResult.SUCCESS;
        }

        // Returning failure by default
        return AINodeResult.FAILURE;
    }

    @Override
    protected void onRootSet(AINode<T> aiNode, T t, Blackboard<T> blackboard) {

    }

    @Override
    public void init(T mob, Blackboard<T> blackboard) {
        mob.canInteractWithOtherSettlers = false;
        playerInteractingWith = null;
        searchTime = 20 * GameRandom.globalRandom.getIntBetween(30, 240);
        nextPathFindTime = 0L;
    }
}
