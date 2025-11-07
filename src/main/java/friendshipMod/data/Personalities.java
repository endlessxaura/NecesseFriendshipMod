package friendshipMod.data;

import friendshipMod.FriendshipMod;
import friendshipMod.packets.PersonalityPacket;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.engine.world.WorldEntity;
import necesse.engine.world.worldData.WorldData;
import necesse.entity.mobs.Mob;

import javax.naming.OperationNotSupportedException;
import java.util.*;

public class Personalities extends WorldData {
    public static final String dataKey = FriendshipMod.modId + "Personalities";
    private static Personalities instance;
    private final Hashtable<Integer, Personality> personalities;

    //region Constructors
    public Personalities() {
        personalities = new Hashtable<>();
    }

    /**
     * Retrieves the same instance of personalities
     * @param worldEntity The world to fetch personalities for
     * @return the singleton instance for personalities
     */
    public static Personalities getInstance(WorldEntity worldEntity) {
        if (instance == null) {
            if (worldEntity.isServer()) {
                WorldData worldData = worldEntity.getWorldData(dataKey);
                if (worldData != null) {
                    instance = (Personalities) worldData;
                } else {
                    Personalities newInstance = new Personalities();
                    worldEntity.addWorldData(dataKey, newInstance);
                    instance = newInstance;
                }
            } else {
                instance = new Personalities();
                instance.setWorldEntity(worldEntity);
            }
        }
        return instance;
    }
    //endregion

    //region WorldData
    @Override
    public void addSaveData(SaveData save) {
        super.addSaveData(save);
        save.addInt(FriendshipMod.modId + "PersonalitiesSize", personalities.size());
        int i = 0;
        for (Integer mobId : personalities.keySet()) {
            save.addInt(FriendshipMod.modId + "Personality" + i + "Mob", mobId);
            save.addStringArray(FriendshipMod.modId + "Personality" + i + "Likes", personalities.get(mobId).likes.toArray(new String[0]));
            save.addStringArray(FriendshipMod.modId + "Personality" + i + "Dislikes", personalities.get(mobId).dislikes.toArray(new String[0]));
            i++;
        }
        System.out.println(FriendshipMod.modId + ": Saved " + personalities.size() + " personalities");
    }

    @Override
    public void applyLoadData(LoadData save) {
        super.applyLoadData(save);
        int size = save.getInt(FriendshipMod.modId + "PersonalitiesSize", 0);
        for (int i = 0; i < size; i++) {
            int mobId = save.getInt(FriendshipMod.modId + "Personality" + i + "Mob");
            Personality personality = new Personality();
            personality.likes = Arrays.asList(save.getStringArray(FriendshipMod.modId + "Personality" + i + "Likes"));
            personality.dislikes = Arrays.asList(save.getStringArray(FriendshipMod.modId + "Personality" + i + "Dislikes"));
            personalities.put(mobId, personality);
        }
        System.out.println(FriendshipMod.modId + ": Loaded " + personalities.size() + " personalities");
    }
    //endregion

    public void generatePersonalityFor(Mob mob) {
        if (mob.isClient()) {
            throw new RuntimeException();
        }
        Personality newPersonality = Personality.generatePersonality(mob.getLevel());
        personalities.put(mob.getUniqueID(), newPersonality);
        mob.getServer().network.sendToClientsWithEntity(new PersonalityPacket(mob.getUniqueID(), newPersonality), mob);
    }

    public Dictionary<Integer, Personality> getAll() {
        return personalities;
    }

    public boolean hasPersonalityFor(int mobId) {
        return personalities.containsKey(mobId);
    }

    public boolean hasPersonalityFor(Mob mob) {
        return hasPersonalityFor(mob.getUniqueID());
    }

    public Personality getPersonalityFor(int mobId) {
        return personalities.get(mobId);
    }

    public Personality getPersonalityFor(Mob mob) {
        return getPersonalityFor(mob.getUniqueID());
    }

    public void setPersonality(Mob mob, Personality personality) {
        setPersonality(mob.getUniqueID(), personality);
    }

    public void setPersonality(Integer mobId, Personality personality) {
        personalities.put(mobId, personality);
    }
}
