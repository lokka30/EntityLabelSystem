package io.github.lokka30.entitylabelsystem.bukkit.packet.nms;

import io.github.lokka30.entitylabelsystem.bukkit.EntityLabelSystem;
import io.github.lokka30.entitylabelsystem.bukkit.packet.LabelUtil;
import io.github.lokka30.entitylabelsystem.bukkit.util.DebugStat;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import java.util.ArrayList;
import javax.annotation.Nonnull;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class NmsLabelUtil implements LabelUtil, Listener {

    private static final String PACKET_LISTENER_NAME = "EntityLabelSystem_EntityMetadata";

    public void registerListeners() {
        for(final Player player : Bukkit.getOnlinePlayers())
            addPacketListenerToPipeline(player);

        Bukkit.getPluginManager().registerEvents(this, EntityLabelSystem.instance());
    }

    public void unregisterListeners() {
        for(final Player player : Bukkit.getOnlinePlayers()) {
            try {
                ((CraftPlayer) player).getHandle().connection.connection.channel.pipeline()
                    .remove(PACKET_LISTENER_NAME);
            } catch (final Exception ignored) {
                // player didn't have the packet listener in the pipeline - that's completely fine.
            }
        }
    }

    private void addPacketListenerToPipeline(final @Nonnull Player player) {
        final ServerPlayer playerHandle = ((CraftPlayer) player).getHandle();

        final ChannelDuplexHandler handler = new ChannelDuplexHandler() {

            @Override
            public void write(
                final ChannelHandlerContext context,
                final Object message,
                final ChannelPromise promise
            ) throws Exception {
                /*
                TODO:
                    ✔ Ensure the outgoing packet is an entity metadata packet
                    ⨯ Retrieve entity instance
                    ⨯ Ensure entity is instanceof LivingEntity
                    ⨯ Ensure entity type is not a Player
                    ⨯ Replace the custom name field in the entity metadata packet with
                      the component.
                 */

                // We want to ensure that the packet we are handling is an entity metadata packet.
                if(!(message instanceof final ClientboundSetEntityDataPacket packet)) {
                    super.write(context, message, promise);
                    return;
                }

                // Debug information (ignore).
                System.out.printf("[Intercepted Entity Metadata Packet - ID: %s]%n", packet.id());
                System.out.printf("packedItems-size: %s%n", packet.packedItems().size());
                System.out.printf("packedItems: %s%n", packet.packedItems());

                // TODO:
                // final LivingEntity entity = ???;
                // final Component customName = generateEntityLabelComponent(entity);

                super.write(context, message, promise);
            }

        };

        playerHandle.connection.connection.channel.pipeline()
            .addBefore("packet_handler", PACKET_LISTENER_NAME, handler);
    }

    /*
    Send an empty* entity metadata packet to the player.
    (* The Entity ID field will be populated, of course.)

    This packet will be intercepted by the packet listener below,
    so it does not need to populate the custom name fields.

    The purpose of this is to update the information shown in the name tag, e.g. health.
     */
    @Override
    public void sendUpdatePacket(
        final LivingEntity entity,
        final Player player
    ) {
        final ServerPlayer playerHandle = ((CraftPlayer) player).getHandle();
        final ClientboundSetEntityDataPacket packet = new ClientboundSetEntityDataPacket(
            entity.getEntityId(),
            new ArrayList<>()
        );
        playerHandle.connection.send(packet);
        DebugStat.metadataUpdates++;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(final PlayerJoinEvent event) {
        addPacketListenerToPipeline(event.getPlayer());
    }
}
