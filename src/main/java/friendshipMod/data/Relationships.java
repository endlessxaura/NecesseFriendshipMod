package friendshipMod.data;

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
    private static final String firstFormatString = "relationship_%d_first";
    private static final String secondFormatString = "relationship_%d_second";
    private static final String valueFormatString = "relationship_%d_value";
    private static final Integer max = 100;
    private static final Integer min = -100;
    private static final Integer defaultScore = 0;
    public static final String dataKey = "friendshipModRelationships";
    private static Relationships instance;

    /**
     * Stores a matrix of unique mob IDs and their friendship scores.
     */
    protected Hashtable<Association, Integer> associations;

    public Relationships() {
        associations = new Hashtable<Association, Integer>(30);
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

    public Hashtable<Association, Integer> getRelationshipsFor(Integer mobId) {
        Hashtable<Association, Integer> associationsFor = new Hashtable<Association, Integer>();
        for (Association key : associations.keySet()) {
            associationsFor.put(key, associations.get(key));
        }
        return associationsFor;
    }

    public int getRelationship(Mob firstMob, Mob secondMob) {
        return getRelationship(firstMob.getUniqueID(), secondMob.getUniqueID());
    }

    public int getRelationship(Integer firstMobId, Integer secondMobId) {
        Association rel = new Association(firstMobId, secondMobId);
        if (associations.containsKey(rel)) {
            return associations.get(rel);
        }
        // TODO: packet request
//        else if (isClient()) {
//        }
        else {
            associations.put(rel, defaultScore);
            return associations.get(rel);
        }
    }

    public void setRelationship(Association rel, Integer value) {
        if (value < min) {
            associations.put(rel, min);
        } else if (value > max) {
            associations.put(rel, max);
        } else {
            associations.put(rel, value);
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
    }

    @Override
    public void applyLoadData(LoadData save) {
        super.applyLoadData(save);
    }
}
