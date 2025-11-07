package friendshipMod.data;

import friendshipMod.FriendshipMod;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.engine.world.WorldEntity;
import necesse.engine.world.worldData.WorldData;
import necesse.entity.mobs.Mob;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 * The world data for all the relationships between mobs
 * Each relationship has a score of -100 to 100, unfriendly to friendly.
 */
public class Relationships extends WorldData {
    private static final Integer defaultScore = 0;
    public static final String dataKey = FriendshipMod.modId + "Relationships";
    private static Relationships instance;
    public static final Integer max = 100;
    public static final Integer min = -100;

    /**
     * Stores a matrix of unique mob IDs and their friendship scores.
     */
    protected Hashtable<Association, Integer> associationScores;

    /**
     * Stores when a relationship was last modified
     */
    protected Hashtable<Association, Long> lastModified;

    // region Constructors
    public Relationships() {
        super();
        associationScores = new Hashtable<>(30);
        lastModified = new Hashtable<>(30);
    }

    /**
     * Retrieves the same instance of relationships
     * @param worldEntity The world to fetch relationships for
     * @return the singleton instance for relationships
     */
    public static Relationships getInstance(WorldEntity worldEntity) {
        if (instance == null) {
            if (worldEntity.isServer()) {
                WorldData worldData = worldEntity.getWorldData(dataKey);
                if (worldData != null) {
                    instance = (Relationships) worldData;
                } else {
                    Relationships newRelationships = new Relationships();
                    worldEntity.addWorldData(dataKey, newRelationships);
                    instance = newRelationships;
                }
            } else {
                instance = new Relationships();
                instance.setWorldEntity(worldEntity);
            }
        }
        return instance;
    }
    // endregion

    // region Accessors
    public static int getRange() {
        return Math.abs(max) + Math.abs(min);
    }

    public List<Relationship> getAll() {
        List<Relationship> associations = new LinkedList<Relationship>();
        for (Association key : associationScores.keySet()) {
            associations.add(new Relationship(key, associationScores.get(key)));
        }
        return associations;
    }

    public List<Relationship> getRelationshipsFor(Mob mob) {
        return getRelationshipsFor(mob.getUniqueID());
    }

    public List<Relationship> getRelationshipsFor(Integer mobId) {
        List<Relationship> associationsFor = new LinkedList<Relationship>();
        for (Association key : associationScores.keySet()) {
            if (key.isFor(mobId)) {
                associationsFor.add(new Relationship(key, associationScores.get(key)));
            }
        }
        return associationsFor;
    }

    public Relationship getRelationship(Association association) {
        return getRelationship(association.mobIds[0], association.mobIds[1]);
    }

    public Relationship getRelationship(Mob firstMob, Mob secondMob) {
        return getRelationship(firstMob.getUniqueID(), secondMob.getUniqueID());
    }

    public Relationship getRelationship(Integer firstMobId, Integer secondMobId) {
        Association rel = new Association(firstMobId, secondMobId);
        if (firstMobId.equals(secondMobId)) {
            System.out.println(FriendshipMod.modId + ": WARNING - tried to get self-relationship with " + rel.first());
            return new Relationship(rel, 0); // Relationships with self is always 0
        } else if (associationScores.containsKey(rel)) {
            return new Relationship(rel, associationScores.get(rel));
        }
        else {
            associationScores.put(rel, defaultScore);
            return new Relationship(rel, associationScores.get(rel));
        }
    }
    // endregion

    // region Mutators
    public void setRelationship(Relationship relationship) {
        setRelationship(relationship.getAssociation(), relationship.score);
    }

    public void setRelationship(Association rel, Integer value) {
        if (rel.first() == rel.second()) {
            System.out.println(FriendshipMod.modId + ": WARNING - tried to save self-relationship with " + rel.first());
            return; // We don't store relationships with self
        }
        if (value < min) {
            associationScores.put(rel, min);
        } else if (value > max) {
            associationScores.put(rel, max);
        } else {
            associationScores.put(rel, value);
        }
        lastModified.put(rel, getWorldTime());
    }

    public void setRelationship(Mob firstMob, Mob secondMob, Integer value) {
        Association rel = new Association(firstMob.getUniqueID(), secondMob.getUniqueID());
        setRelationship(rel, value);
    }

    public void setRelationship(Integer firstMobId, Integer secondMobId, Integer value) {
        Association rel = new Association(firstMobId, secondMobId);
        setRelationship(rel, value);
    }
    // endregion

    // region Helpers
    public boolean recentlyModified(Association rel) {
        return lastModified.get(rel) != null && getWorldEntity().getWorldTime() - lastModified.get(rel) < getWorldEntity().getDayTimeMax() * 1000L;
    }

    public boolean recentlyModified(Mob first, Mob second) {
        return recentlyModified(new Association(first.getUniqueID(), second.getUniqueID()));
    }

    public boolean recentlyModified(Integer firstMobId, Integer secondMobId) {
        return recentlyModified(new Association(firstMobId, secondMobId));
    }
    // endregion

    // region WorldEntity
    @Override
    public void addSaveData(SaveData save) {
        super.addSaveData(save);
        save.addInt(FriendshipMod.modId + "RelationshipsSize", associationScores.size());
        int i = 0;
        for (Association association : associationScores.keySet()) {
            save.addInt(FriendshipMod.modId + "Relationship" + i + "First", association.first());
            save.addInt(FriendshipMod.modId + "Relationship" + i + "Second", association.second());
            save.addInt(FriendshipMod.modId + "Relationship" + i + "Value", associationScores.get(association));
            save.addLong(
                    FriendshipMod.modId + "Relationship" + i + "Time",
                    lastModified.get(association) != null ? lastModified.get(association) : 0
            );
            i++;
//            System.out.println(FriendshipMod.modId + ": Saved " + associationOutput((association)));
        }
        System.out.println(FriendshipMod.modId + ": Saved " + associationScores.size() + " relationships");
    }

    @Override
    public void applyLoadData(LoadData save) {
        super.applyLoadData(save);
        int size = save.getInt(FriendshipMod.modId + "RelationshipsSize", 0);
        for (int i = 0; i < size; i++) {
            int first = save.getInt(FriendshipMod.modId + "Relationship" + i + "First");
            int second = save.getInt(FriendshipMod.modId + "Relationship" + i + "Second");
            int value = save.getInt(FriendshipMod.modId + "Relationship" + i + "Value");
            long time = save.getLong(FriendshipMod.modId + "Relationship" + i + "Time", 0);
            Association association = new Association(first, second);
            setRelationship(association, value);
//            System.out.println(FriendshipMod.modId + ": Loaded " + associationOutput(association));
        }
        System.out.println(FriendshipMod.modId + ": Loaded " + associationScores.size() + " relationships");
    }
    // endregion

    public String associationOutput(Association association) {
        return association.toString() + " = " + associationScores.get(association);
    }
}
