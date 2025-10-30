package friendshipMod.data;

import friendshipMod.FriendshipMod;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.engine.world.WorldEntity;
import necesse.engine.world.worldData.WorldData;
import necesse.entity.mobs.Mob;

import java.util.Hashtable;

/**
 * The world data for all the relationships between mobs
 * Each relationship has a score of -100 to 100, unfriendly to friendly.
 */
public class Relationships extends WorldData {
    private static final Integer max = 100;
    private static final Integer min = -100;
    private static final Integer defaultScore = 0;
    public static final String dataKey = "friendshipModRelationships";
    private static Relationships instance;

    /**
     * Stores a matrix of unique mob IDs and their friendship scores.
     */
    protected Hashtable<Association, Integer> associationScores;

    public Relationships() {
        associationScores = new Hashtable<Association, Integer>(30);
    }

    public String associationOutput(Association association) {
        return association.toString() + " = " + associationScores.get(association);
    }

    public static Relationships getRelationships(WorldEntity worldEntity) {
        if (worldEntity.isServer()) {
            WorldData worldData = worldEntity.getWorldData(dataKey);
            if (worldData != null) {
                return (Relationships) worldData;
            } else {
                Relationships newRelationships = new Relationships();
                worldEntity.addWorldData(dataKey, newRelationships);
                return newRelationships;
            }
        } else {
            if (instance == null) {
                instance = new Relationships();
            }
            return instance;
        }
    }

    public Hashtable<Association, Integer> getAll() {
        return associationScores;
    }

    public Hashtable<Association, Integer> getRelationshipsFor(Mob mob) {
        return getRelationshipsFor(mob.getUniqueID());
    }

    public Hashtable<Association, Integer> getRelationshipsFor(Integer mobId) {
        Hashtable<Association, Integer> associationsFor = new Hashtable<Association, Integer>();
        for (Association key : associationScores.keySet()) {
            associationsFor.put(key, associationScores.get(key));
        }
        return associationsFor;
    }

    public int getRelationship(Association association) {
        return getRelationship(association.mobIds[0], association.mobIds[1]);
    }

    public int getRelationship(Mob firstMob, Mob secondMob) {
        return getRelationship(firstMob.getUniqueID(), secondMob.getUniqueID());
    }

    public int getRelationship(Integer firstMobId, Integer secondMobId) {
        Association rel = new Association(firstMobId, secondMobId);
        if (associationScores.containsKey(rel)) {
            return associationScores.get(rel);
        }
        else {
            associationScores.put(rel, defaultScore);
            return associationScores.get(rel);
        }
    }

    public void setRelationship(Association rel, Integer value) {
        if (value < min) {
            associationScores.put(rel, min);
        } else if (value > max) {
            associationScores.put(rel, max);
        } else {
            associationScores.put(rel, value);
        }
    }

    public void setRelationship(Mob firstMob, Mob secondMob, Integer value) {
        Association rel = new Association(firstMob.getUniqueID(), secondMob.getUniqueID());
        setRelationship(rel, value);
    }

    public void setRelationship(Integer firstMobId, Integer secondMobId, Integer value) {
        Association rel = new Association(firstMobId, secondMobId);
        setRelationship(rel, value);
    }

    @Override
    public void addSaveData(SaveData save) {
        super.addSaveData(save);
        save.addInt(FriendshipMod.modId + "RelationshipsSize", associationScores.size());
        int i = 0;
        for (Association association : associationScores.keySet()) {
            save.addInt(FriendshipMod.modId + "Relationship" + i + "First", association.first());
            save.addInt(FriendshipMod.modId + "Relationship" + i + "Second", association.second());
            save.addInt(FriendshipMod.modId + "Relationship" + i + "Value", associationScores.get(association));
            i++;
            System.out.println(FriendshipMod.modId + ": Saved " + associationOutput((association)));
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
            associationScores.put(association, value);
            System.out.println(FriendshipMod.modId + ": Loaded " + associationOutput(association));
        }
        System.out.println(FriendshipMod.modId + ": Loaded " + associationScores.size() + " relationships");
    }
}
