package friendshipMod.patches;

import friendshipMod.data.Relationship;
import friendshipMod.data.Relationships;
import friendshipMod.utilities.Roommates;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.friendly.human.HappinessModifier;
import necesse.entity.mobs.friendly.human.HumanMob;
import net.bytebuddy.asm.Advice;

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
                if (modifier.happiness != 0) {
                    happinessModifiers.add(modifier);
                }
            }
        }
    }
}
