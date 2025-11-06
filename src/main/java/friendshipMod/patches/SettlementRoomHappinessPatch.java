package friendshipMod.patches;

import friendshipMod.data.Personalities;
import friendshipMod.data.Personality;
import friendshipMod.data.Relationship;
import friendshipMod.data.Relationships;
import friendshipMod.utilities.Furniture;
import friendshipMod.utilities.Roommates;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.friendly.human.HappinessModifier;
import necesse.entity.mobs.friendly.human.HumanMob;
import necesse.level.gameObject.GameObject;
import necesse.level.gameObject.furniture.RoomFurniture;
import net.bytebuddy.asm.Advice;

import java.util.LinkedList;
import java.util.List;

/**
 * This adds the roommate relationship modifier to the happiness modifiers.
 */
@ModMethodPatch(target = HumanMob.class, name = "getHappinessModifiers", arguments = {})
public class SettlementRoomHappinessPatch {
    @Advice.OnMethodExit
    static void onExit(
        @Advice.This HumanMob humanMob,
        @Advice.Return(readOnly = false) List<HappinessModifier> happinessModifiers
    ) {
        List<Mob> roommates = Roommates.get(humanMob);
        if (!roommates.isEmpty()) {
            Relationships relationships = Relationships.getInstance(humanMob.getWorldEntity());
            for (Mob roommate : roommates) {
                Relationship relationship = relationships.getRelationship(humanMob, roommate);
                HappinessModifier modifier = relationship.getRoommateHappinessModifier(roommate);
                happinessModifiers.add(modifier);
            }
        }

        List<String> checkedIds = new LinkedList<>();
        List<GameObject> furniture = Furniture.get(humanMob);
        if (!furniture.isEmpty()) {
            Personalities personalities = Personalities.getInstance(humanMob.getWorldEntity());
            Personality personality = personalities.getPersonalityFor(humanMob);
            if (personality != null) {
                for (GameObject furnitureItem : furniture) {
                    if (!checkedIds.contains(furnitureItem.getStringID()) && personality.likes(furnitureItem.getStringID())) {
                        HappinessModifier modifier = personality.getModifierFor(furnitureItem.getObjectItem());
                        happinessModifiers.add(modifier);
                    }
                    checkedIds.add(furnitureItem.getStringID());
                }
            }
        }
    }
}
