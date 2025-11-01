package friendshipMod.utilities;

import necesse.entity.mobs.Mob;
import necesse.entity.mobs.friendly.human.HumanMob;
import necesse.level.maps.levelData.settlementData.SettlementBed;
import necesse.level.maps.levelData.settlementData.SettlementRoom;
import necesse.level.maps.regionSystem.ConnectedSubRegionsResult;
import necesse.level.maps.regionSystem.SubRegion;

import java.awt.*;
import java.util.LinkedList;

public class Roommates {
    public static LinkedList<Mob> get(HumanMob person) {
        LinkedList<Mob> roommates = new LinkedList<>();
        SettlementRoom room = person.levelSettler.getBed().getRoom();
        if (room.getLevel().isOutside(room.tileX, room.tileY)) {
            return roommates;
        }
        ConnectedSubRegionsResult roomRegion = room.getLevel().regionManager.getRoomConnectedByTile(room.tileX, room.tileY, true, 2000);
        if (roomRegion == null) {
            return roommates;
        }
        for(SubRegion subRegion : roomRegion.connectedRegions) {
            for(Point p : subRegion.getLevelTiles()) {
                if (room.data.networkData.isTileWithinBounds(p.x, p.y)) {
                    SettlementBed bed = room.data.addOrValidateBed(p.x, p.y, true);
                    if (bed != null && bed.getSettler() != null) {
                        Mob bedMob = bed.getSettler().getMob().getMob();
                        if (bedMob.getUniqueID() != person.getUniqueID()) {
                            roommates.add(bed.getSettler().getMob().getMob());
                        }
                    }
                }
            }
        }
        return roommates;
    }
}
