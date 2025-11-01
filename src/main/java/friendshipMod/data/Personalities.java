package friendshipMod.data;

import friendshipMod.FriendshipMod;
import friendshipMod.packets.PersonalityPacket;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.engine.world.WorldEntity;
import necesse.engine.world.worldData.WorldData;
import necesse.entity.mobs.Mob;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

public class Personalities extends WorldData {
    public static final String dataKey = FriendshipMod.modId + "Personalities";
    private static Personalities instance;
    private Hashtable<Integer, Personality> personalities;

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
            }
        }
        return instance;
    }
    //endregion

    //region WorldData
    private void addListToSave(SaveData save, List<Integer> list, String prefix, int index) {
        save.addInt(FriendshipMod.modId + "Personalities" + index + prefix + "Size", list.size());
        for (int i = 0; i < list.size(); i++) {
            save.addInt(FriendshipMod.modId + "Personalities" + index + prefix + "Value" + i, list.get(i));
        }
    }

    private List<Integer> getListFromSave(LoadData save, String prefix, int index) {
        int size = save.getInt(FriendshipMod.modId + "Personalities" + index + prefix + "Size");
        LinkedList<Integer> list = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            list.add(save.getInt(FriendshipMod.modId + "Personalities" + index + prefix + "Value" + i));
        }
        return list;
    }

    private void addPersonalityToSave(SaveData save, Personality personality, int index) {
        addListToSave(save, personality.likes, "Likes", index);
        addListToSave(save, personality.dislikes, "Dislikes", index);
    }

    private Personality loadPersonalityFromSave(LoadData save, int index) {
        Personality personality = new Personality();
        personality.likes = getListFromSave(save, "Likes", index);
        personality.dislikes = getListFromSave(save, "Dislikes", index);
        return personality;
    }

    @Override
    public void addSaveData(SaveData save) {
        super.addSaveData(save);
        save.addInt(FriendshipMod.modId + "PersonalitiesSize", personalities.size());
        int i = 0;
        for (Integer mobId : personalities.keySet()) {
            save.addInt(FriendshipMod.modId + "Personality" + i + "Mob", mobId);
            addPersonalityToSave(save, personalities.get(mobId), i);
            i++;
        }
        System.out.println(FriendshipMod.modId + ": Saved " + personalities.size() + " personalities");
    }

    @Override
    public void applyLoadData(LoadData save) {
        super.applyLoadData(save);
        int size = save.getInt(FriendshipMod.modId + "PersonalitiesSize");
        for (int i = 0; i < size; i++) {
            int mobId = save.getInt(FriendshipMod.modId + "Personality" + i + "Mob");
            Personality personality = loadPersonalityFromSave(save, i);
            personalities.put(mobId, personality);
        }
        System.out.println(FriendshipMod.modId + ": Loaded " + personalities.size() + " personalities");
    }
    //endregion

    private void generatePersonalityFor(Mob mob) {
        Personality newPersonality = Personality.generatePersonality();
        personalities.put(mob.getUniqueID(), newPersonality);
        mob.getServer().network.sendToClientsWithEntity(new PersonalityPacket(mob.getUniqueID(), newPersonality), mob);
        System.out.println("Generated personality for " + mob.getDisplayName());
    }

    public Dictionary<Integer, Personality> getAll() {
        return personalities;
    }

    public Personality getPersonalityFor(Mob mob) {
        if (!personalities.containsKey(mob.getUniqueID())) {
            generatePersonalityFor(mob);
        }
        System.out.println("Got personality for " + mob.getDisplayName());
        return personalities.get(mob.getUniqueID());
    }

    public void setPersonality(Mob mob, Personality personality) {
        setPersonality(mob.getUniqueID(), personality);
    }

    public void setPersonality(Integer mobId, Personality personality) {
        personalities.put(mobId, personality);
    }
}
