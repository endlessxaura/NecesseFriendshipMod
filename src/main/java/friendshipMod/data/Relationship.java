package friendshipMod.data;

import necesse.engine.localization.message.GameMessage;
import necesse.engine.localization.message.GameMessageBuilder;
import necesse.engine.registries.MobRegistry;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.friendly.human.HappinessModifier;
import necesse.level.maps.Level;

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
            if (score >= 70) {
                relationshipStatus = Status.Beloved;
            } else if (score > 45) {
                relationshipStatus = Status.Companion;
            } else if (score > 25) {
                relationshipStatus = Status.Confidant;
            } else if (score > 10) {
                relationshipStatus = Status.Friend;
            } else if (score > -10) {
                relationshipStatus = Status.Acquaintance;
            } else if (score > -25) {
                relationshipStatus = Status.Irritant;
            } else if (score > -45) {
                relationshipStatus = Status.Opponent;
            } else if (score > -70) {
                relationshipStatus = Status.Enemy;
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

    public boolean entitiesExist(Level level) {
        Mob first = level.entityManager.mobs.get(association.first(), true);
        Mob second = level.entityManager.mobs.get(association.second(), true);
        return first != null && second != null;
    }

    public GameMessage getRandomMessageFor(Mob mob) {
        if (!entitiesExist(mob.getLevel())) {
            return null;
        }
        GameMessageBuilder message = new GameMessageBuilder();
        Mob about;
        Status status = getStatus();
        if (association.isFor(mob.getUniqueID())) {
            if (mob.getUniqueID() == association.first()) {
                about = mob.getLevel().entityManager.mobs.get(association.first(), true);
            } else {
                about = mob.getLevel().entityManager.mobs.get(association.second(), true);
            }
            if (about != null) {
                if (status == Status.Beloved) {
                    message.append("I absolutely adore " + about.getDisplayName() + ".");
                } else if (status == Status.Companion) {
                    message.append(about.getDisplayName() + " is so lovely, don't you think?");
                } else if (status == Status.Confidant) {
                    message.append(about.getDisplayName() + " does excellent work. I highly recommend them.");
                } else if (status == Status.Friend) {
                    message.append("Hey, have you seen " + about.getDisplayName() + "? We're supposed to hang out later.");
                } else if (status == Status.Acquaintance) {
                    message.append("Yeah, I've met " + about.getDisplayName() + ". What do you think of them?");
                } else if (status == Status.Irritant) {
                    message.append("Ugh, I know, " + about.getDisplayName() + " is so annoying.");
                } else if (status == Status.Opponent) {
                    message.append("I swear, every time I do a task, " + about.getDisplayName() + " just comes and messes it up.");
                } else if (status == Status.Enemy) {
                    message.append("You know what I think?! This village would be better without " + about.getDisplayName() + " in it.");
                } else if (status == Status.Nemesis) {
                    message.append("I can't stand " + about.getDisplayName() + "! Just keep them away from me!");
                }
            }
        } else {
            Mob first = mob.getLevel().entityManager.mobs.get(association.first(), true);
            Mob second = mob.getLevel().entityManager.mobs.get(association.second(), true);
            if (first != null && second != null) {
                if (status == Status.Beloved) {
                    message.append("Have you seen " + first.getDisplayName() + " and " + second.getDisplayName() + "? Such love birds, they are!");
                } else if (status == Status.Companion) {
                    message.append(first.getDisplayName() + " and " + second.getDisplayName() + " - like two peas in a pod");
                } else if (status == Status.Confidant) {
                    message.append("I've seen " + first.getDisplayName() + " and " + second.getDisplayName() + " gossiping lately. I wonder what they're whispering about?");
                } else if (status == Status.Friend) {
                    message.append(first.getDisplayName() + " and " + second.getDisplayName() + "? I hear they're good friends.");
                } else if (status == Status.Acquaintance) {
                    message.append("I saw " + first.getDisplayName() + " and " + second.getDisplayName() + " chatting. Glad to see they new person opening up.");
                } else if (status == Status.Irritant) {
                    message.append(first.getDisplayName() + " and " + second.getDisplayName() + " were bickering again. Typical...");
                } else if (status == Status.Opponent) {
                    message.append(second.getDisplayName() + " has said some nasty things about " + first.getDisplayName() + ". Are they true?");
                } else if (status == Status.Enemy) {
                    message.append(first.getDisplayName() + " doesn't even like being in the same room at " + second.getDisplayName() + ". I'd stay clear of them.");
                } else if (status == Status.Nemesis) {
                    message.append("You need to do something about " + first.getDisplayName() + " and " + second.getDisplayName() + ". They'll tear apart the town!");
                }
            }
        }
        return message;
    }
}
