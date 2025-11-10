package friendshipMod.utilities;

import friendshipMod.data.Personalities;
import friendshipMod.data.Personality;
import friendshipMod.data.Relationship;
import friendshipMod.data.Relationships;
import necesse.engine.localization.message.GameMessage;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.Mob;

import java.util.List;

public class Messages {
    public static GameMessage getRandomMessageFor(Mob mob) {
        Personalities personalities = Personalities.getInstance(mob.getWorldEntity());
        Relationships relationships = Relationships.getInstance(mob.getWorldEntity());
        Personality personality = personalities.getPersonalityFor(mob);
        List<Relationship> allRelationships = relationships.getAll();
        int allTickets = personality.likes.size() + personality.dislikes.size() + allRelationships.size() - 1;
        int ticket = GameRandom.globalRandom.getIntBetween(0, allTickets);
        if (ticket < personality.likes.size()) {
            return personality.getMessageFor(personality.likes.get(ticket), mob.getLevel());
        } else {
            ticket -= personality.likes.size();
        }
        if (ticket < personality.dislikes.size()) {
            return personality.getMessageFor(personality.dislikes.get(ticket), mob.getLevel());
        } else {
            ticket -= personality.dislikes.size();
        }
        if (allRelationships.get(ticket).entitiesExist(mob.getLevel())) {
            return allRelationships.get(ticket).getRandomMessageFor(mob);
        } else {
            return getRandomMessageFor(mob);
        }
    }
}
