package friendshipMod.data;

import friendshipMod.FriendshipMod;
import necesse.engine.world.WorldEntity;
import necesse.engine.world.worldData.WorldData;
import necesse.entity.mobs.Mob;
import necesse.inventory.item.Item;

import java.util.Hashtable;
import java.util.Optional;

public class TicketManager extends WorldData {
    private static TicketManager instance;
    public static final String dataKey = FriendshipMod.modId + "TicketManager";
    private Hashtable<Integer, Mob> decidedMobs;
    private Hashtable<Integer, Item> decidedItems;

    // region Constructors
    public TicketManager() {
        decidedMobs = new Hashtable<>(10);
        decidedItems = new Hashtable<>(10);
    }

    public static TicketManager getTicketManager(WorldEntity worldEntity) {
        if (instance == null) {
            if (worldEntity.isServer()) {
                WorldData worldData = worldEntity.getWorldData(dataKey);
                if (worldData != null) {
                    instance = (TicketManager) worldData;
                } else {
                    TicketManager newInstance = new TicketManager();
                    worldEntity.addWorldData(dataKey, newInstance);
                    instance = newInstance;
                }
            } else {
                instance = new TicketManager();
            }
        }
        return instance;
    }
    // endregion
    
    public void setDecidedTicket(int mobId, Mob decidedMob) {
        decidedMobs.put(mobId, decidedMob);
        decidedItems.remove(mobId);
    }

    public void setDecidedTicket(int mobId, Item decidedItem) {
        decidedMobs.remove(mobId);
        decidedItems.put(mobId, decidedItem);
    }

    public Optional<Ticket> popDecidedTicket(int mobId) {
        Ticket decidedTicket = null;
        if (decidedMobs.containsKey(mobId)) {
            decidedTicket = new Ticket(decidedMobs.get(mobId).getUniqueID(), Ticket.Kind.Human);
            decidedMobs.remove(mobId);
        } else if (decidedItems.containsKey(mobId)) {
            decidedTicket = new Ticket(decidedItems.get(mobId).getID(), Ticket.Kind.Item);
            decidedItems.remove(mobId);
        }
        return Optional.ofNullable(decidedTicket);
    }
}
