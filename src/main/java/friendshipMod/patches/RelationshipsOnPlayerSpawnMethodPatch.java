package friendshipMod.patches;

import friendshipMod.data.Personalities;
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
    // TODO: I probably shouldn't be doing this. On a large server, this packet could be huge.
    @Advice.OnMethodExit
    static void onExit(
        @Advice.Argument(1) Server server,
        @Advice.Argument(2) ServerClient client
    ) {
        Relationships relationships = Relationships.getInstance(server.world.worldEntity);
        RelationshipsPacket packet = new RelationshipsPacket(relationships.getAll());
        server.network.sendPacket(packet, client);

        // TODO: send personality packet?
    }
}
