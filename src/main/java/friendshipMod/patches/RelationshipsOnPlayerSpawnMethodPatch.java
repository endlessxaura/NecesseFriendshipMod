package friendshipMod.patches;

import friendshipMod.data.Relationships;
import friendshipMod.packets.RelationshipsPacket;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.network.NetworkPacket;
import necesse.engine.network.packet.PacketSpawnPlayer;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import net.bytebuddy.asm.Advice;

@ModMethodPatch(target = PacketSpawnPlayer.class, name = "processServer", arguments = {NetworkPacket.class, Server.class, ServerClient.class})
public class RelationshipsOnPlayerSpawnMethodPatch {
    @Advice.OnMethodExit
    static void onExit(
        @Advice.Argument(1) Server server,
        @Advice.Argument(2) ServerClient client
    ) {
        Relationships relationships = Relationships.getRelationships(server.world.worldEntity);
        RelationshipsPacket packet = new RelationshipsPacket(relationships.getAll());
        server.network.sendPacket(packet, client);
    }
}
