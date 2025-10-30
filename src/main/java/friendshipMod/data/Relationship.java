package friendshipMod.data;

import necesse.engine.localization.message.GameMessage;
import necesse.engine.localization.message.GameMessageBuilder;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.friendly.human.HappinessModifier;

public class Relationship {
    public enum Status {
        Beloved,
        Companion,
        Confidant,
        Friend,
        Acquaintance,
        Irritant,
        Opponent,
        Enemy,
        Nemesis
    }

    private static final String roommateMessageFormat = "I am roommates with my %s, %s";
    private final Association association;
    private Status relationshipStatus;
    public int score;

    public Relationship(Association association, int score) {
        this.association = association;
        this.score = score;
    }

    public Association getAssociation() {
        return association;
    }

    public Status getStatus() {
        if (relationshipStatus == null) {
            if (score >= 100) {
                relationshipStatus = Status.Beloved;
            } else if (score > 75) {
                relationshipStatus = Status.Companion;
            } else if (score > 50) {
                relationshipStatus = Status.Confidant;
            } else if (score > 25) {
                relationshipStatus = Status.Friend;
            } else if (score > -50) {
                relationshipStatus = Status.Acquaintance;
            } else if (score > -75) {
                relationshipStatus = Status.Irritant;
            } else if (score > -100) {
                relationshipStatus = Status.Opponent;
            } else {
                relationshipStatus = Status.Nemesis;
            }
        }
        return relationshipStatus;
    }

    private int getRoommateHappiness() {
        Status relationshipStatus = getStatus();
        switch (relationshipStatus) {
            case Beloved:
                return 50;
            case Companion:
                return 30;
            case Confidant:
                return 20;
            case Friend:
                return 10;
            case Irritant:
                return -10;
            case Opponent:
                return -20;
            case Enemy:
                return -30;
            case Nemesis:
                return -50;
            case Acquaintance:
            default:
                return 0;
        }
    }

    public HappinessModifier getRoommateHappinessModifier(Mob about) {
        String message = String.format(roommateMessageFormat, getStatus().toString().toLowerCase(), about.getDisplayName());
        GameMessage gameMessage = new GameMessageBuilder().append(message);
        return new HappinessModifier(getRoommateHappiness(), gameMessage);
    }
}
