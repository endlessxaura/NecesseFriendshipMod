package friendshipMod.data;

import friendshipMod.FriendshipMod;
import friendshipMod.packets.RelationshipPacket;
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

    public static class AdjustmentTypes {
        public static final String Load = "load";
        public static final String Talk = "talk";
        public static final String Gift = "gift";
        public static final String Decay = "decay";
    }

    /**
     * Stores a matrix of unique mob IDs and their friendship scores.
     */
    protected final Hashtable<Association, Integer> associationScores;

    /**
     * Stores when a relationship was last modified by type
     */
    protected final Hashtable<String, Hashtable<Association, Long>> lastModifiedByType;

    /**
     * Contains the last time the decay was checked. Starts at the current world time.
     */
    public long lastDecayCheck;

    // region Constructors
    public Relationships() {
        super();
        associationScores = new Hashtable<>(30);
        lastModifiedByType = new Hashtable<>(5, 1);
        lastModifiedByType.put(AdjustmentTypes.Gift, new Hashtable<>());
        lastModifiedByType.put(AdjustmentTypes.Talk, new Hashtable<>());
        lastModifiedByType.put(AdjustmentTypes.Load, new Hashtable<>());
        lastModifiedByType.put(AdjustmentTypes.Decay, new Hashtable<>());
        lastDecayCheck = getTime();
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
    public void setRelationship(Relationship relationship, String type) {
        setRelationship(relationship.getAssociation(), relationship.score, type);
    }

    public void setRelationship(Association rel, Integer value, String type) {
        if (rel.first() == rel.second()) {
            System.out.println(FriendshipMod.modId + ": WARNING - tried to save self-relationship with " + rel.first());
            return; // We don't store relationships with self
        }
        if (value < min) {
            associationScores.put(rel, min);
        } else if (value > max) {
            associationScores.put(rel, max);
        } else if (value == 0) {
            associationScores.computeIfPresent(rel, (k, i) -> associationScores.remove(k));
        } else {
            associationScores.put(rel, value);
        }
        setRecentlyModified(rel, type);
    }

    public void setRelationship(Mob firstMob, Mob secondMob, Integer value, String type) {
        Association rel = new Association(firstMob.getUniqueID(), secondMob.getUniqueID());
        setRelationship(rel, value, type);
    }

    public void setRelationship(Integer firstMobId, Integer secondMobId, Integer value, String type) {
        Association rel = new Association(firstMobId, secondMobId);
        setRelationship(rel, value, type);
    }
    // endregion

    // region Recently modified
    private void setRecentlyModified(Association rel, String type, Long value) {
        lastModifiedByType.putIfAbsent(type, new Hashtable<>());
        lastModifiedByType.get(type).put(rel, value);
    }

    public void setRecentlyModified(Association rel, String type) {
        lastModifiedByType.putIfAbsent(type, new Hashtable<>());
        lastModifiedByType.get(type).put(rel, getWorldTime());
    }

    public void setRecentlyModified(Mob first, Mob second, String type) {
        setRecentlyModified(new Association(first.getUniqueID(), second.getUniqueID()), type);
    }

    public void setRecentlyModified(Integer firstMobId, Integer secondMobId, String type) {
        setRecentlyModified(new Association(firstMobId, secondMobId), type);
    }

    private long getRecentlyModified(Association rel, String type) {
        lastModifiedByType.putIfAbsent(type, new Hashtable<>());
        Hashtable<Association, Long> lastModified = lastModifiedByType.get(type);
        return lastModified.getOrDefault(rel, 0L);
    }

    private boolean recentlyModified(Association rel, Long timespan) {
        for (String key : lastModifiedByType.keySet()) {
            if (recentlyModified(rel, key, timespan)) {
                return true;
            }
        }
        return false;
    }

    private boolean recentlyModified(Association rel, String type, Long timespan) {
        lastModifiedByType.putIfAbsent(type, new Hashtable<>());
        Hashtable<Association, Long> lastModified = lastModifiedByType.get(type);
        return lastModified.get(rel) != null && getWorldEntity().getWorldTime() - lastModified.get(rel) < timespan;
    }

    public boolean recentlyModified(Association rel, String type) {
        return recentlyModified(rel, type, getWorldEntity().getDayTimeMax() * 1000L);
    }

    public boolean recentlyModified(Mob first, Mob second, String type) {
        return recentlyModified(new Association(first.getUniqueID(), second.getUniqueID()), type);
    }

    public boolean recentlyModified(Integer firstMobId, Integer secondMobId, String type) {
        return recentlyModified(new Association(firstMobId, secondMobId), type);
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
                    FriendshipMod.modId + "Relationship" + i + "LastTalked",
                    getRecentlyModified(association, AdjustmentTypes.Talk)
            );
            save.addLong(
                    FriendshipMod.modId + "Relationship" + i + "LastGifted",
                    getRecentlyModified(association, AdjustmentTypes.Gift)
            );
            i++;
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
            Association association = new Association(first, second);
            setRelationship(association, value, AdjustmentTypes.Load);
            setRecentlyModified(
                    association,
                    "talk",
                    save.getLong(FriendshipMod.modId + "Relationship" + i + "LastTalked", 0)
            );
            setRecentlyModified(
                    association,
                    "talk",
                    save.getLong(FriendshipMod.modId + "Relationship" + i + "LastGifted", 0)
            );
        }
        System.out.println(FriendshipMod.modId + ": Loaded " + associationScores.size() + " relationships");
    }

    @Override
    public void tick() {

        // Relationship decay check every 10 seconds
        if (isServer() && getTime() - lastDecayCheck > 10000L) {
            synchronized (associationScores) {
                for (Association association : associationScores.keySet()) {
                    if (!recentlyModified(association, getWorldEntity().getDayTimeMax() * 1000L * 5)) {
                        associationScores.put(association, associationScores.get(association) - 1);
                        RelationshipPacket packet = new RelationshipPacket(getRelationship(association), AdjustmentTypes.Decay);
                        this.getServer().network.sendToAllClients(packet);
                    }
                }
            }
        }
        super.tick();
    }
    // endregion

    public String associationOutput(Association association) {
        return association.toString() + " = " + associationScores.get(association);
    }
}
