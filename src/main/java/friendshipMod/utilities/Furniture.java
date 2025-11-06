package friendshipMod.utilities;

import necesse.engine.registries.ObjectLayerRegistry;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.friendly.human.HumanMob;
import necesse.inventory.item.placeableItem.PlaceableItem;
import necesse.level.gameObject.GameObject;
import necesse.level.gameObject.furniture.RoomFurniture;
import necesse.level.maps.levelData.settlementData.SettlementBed;
import necesse.level.maps.levelData.settlementData.SettlementRoom;
import necesse.level.maps.regionSystem.ConnectedSubRegionsResult;
import necesse.level.maps.regionSystem.SubRegion;

import java.awt.*;
import java.util.LinkedList;

public class Furniture {
    public static LinkedList<GameObject> get(HumanMob person) {
        LinkedList<GameObject> furniture = new LinkedList<>();
        SettlementRoom room = person.levelSettler.getBed().getRoom();
        if (room.getLevel().isOutside(room.tileX, room.tileY)) {
            return furniture;
        }
        ConnectedSubRegionsResult roomRegion = room.getLevel().regionManager.getRoomConnectedByTile(room.tileX, room.tileY, true, 2000);
        if (roomRegion == null) {
            return furniture;
        }
        for(SubRegion subRegion : roomRegion.connectedRegions) {
            for(Point p : subRegion.getLevelTiles()) {
                if (room.data.networkData.isTileWithinBounds(p.x, p.y)) {
                    for(int layer = 0; layer < ObjectLayerRegistry.getTotalLayers(); ++layer) {
                        GameObject object = room.getLevel().getObject(layer, p.x, p.y);
                        if (object instanceof RoomFurniture) {
                            furniture.add(object);
                        }
                    }
                }
            }
        }
        return furniture;
    }
}
