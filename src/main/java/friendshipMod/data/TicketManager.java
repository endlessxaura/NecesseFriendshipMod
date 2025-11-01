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
    private Hashtable<Integer, Ticket> decidedTickets;

    // region Constructors
    public TicketManager() {
        decidedTickets = new Hashtable<>(10);
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

    public void setDecidedTicket(int mobId, String aboutId, Ticket.Kind kind) {
        decidedTickets.put(mobId, new Ticket(aboutId, kind));
    }

    public Optional<Ticket> popDecidedTicket(int mobId) {
        Ticket decidedTicket = null;
        if (decidedTickets.containsKey(mobId)) {
            decidedTicket = decidedTickets.get(mobId);
            decidedTickets.remove(mobId);
        }
        return Optional.ofNullable(decidedTicket);
    }
}
