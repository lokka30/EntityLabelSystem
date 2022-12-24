package io.github.lokka30.entitylabelsystem.bukkit.packet.nms;

import io.github.lokka30.entitylabelsystem.bukkit.EntityLabelSystem;
import io.github.lokka30.entitylabelsystem.bukkit.packet.LabelUtil;
import io.github.lokka30.entitylabelsystem.bukkit.util.DebugStat;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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

    /*
    Originally written by DMan16 @ SpigotMC.org
     */
    @Nullable
    private MutableComponent adventureToNmsComponent(
        final @Nullable net.kyori.adventure.text.Component advComp
    ) {
        if(advComp == null) return null;

        final ComponentContents contents;
        MutableComponent nmsComp;
        String textContent = null;

        if(advComp instanceof final TextComponent textComponent) {
            textContent = textComponent.content();
            contents = new LiteralContents(textContent);
        } else if(advComp instanceof final TranslatableComponent translatableComponent) {
            if(translatableComponent.args().isEmpty()) {
                contents = new TranslatableContents(translatableComponent.key());
            } else {
                contents = new TranslatableContents(
                    translatableComponent.key(),
                    translatableComponent.args().stream()
                        .map(this::adventureToNmsComponent)
                        .filter(Objects::nonNull)
                        .toArray()
                );
            }
        } else {
            return null;
        }

        nmsComp = MutableComponent.create(contents);

        net.kyori.adventure.text.format.TextColor advColor = advComp.color();
        TextColor nmsColor = null;
        if(advColor != null) {
            if(advColor instanceof final NamedTextColor named) {
                nmsColor = TextColor.fromLegacyFormat(
                    ChatFormatting.getByName(named.toString())
                );
            } else {
                nmsColor = TextColor.fromRgb(advColor.value());
            }
        }
        Style nmsStyle = Style.EMPTY;
        if(nmsColor != null)
            nmsStyle = nmsStyle.withColor(nmsColor);

        final Function<TextDecoration, Boolean> hasDecoration = (decoration) ->
            advComp.decoration(decoration) == State.TRUE;

        if(hasDecoration.apply(TextDecoration.BOLD))
            nmsStyle = nmsStyle.withBold(true);
        if(hasDecoration.apply(TextDecoration.ITALIC))
            nmsStyle = nmsStyle.withItalic(true);
        if(hasDecoration.apply(TextDecoration.OBFUSCATED))
            nmsStyle = nmsStyle.withObfuscated(true);
        if(hasDecoration.apply(TextDecoration.STRIKETHROUGH))
            nmsStyle = nmsStyle.withStrikethrough(true);
        if(hasDecoration.apply(TextDecoration.UNDERLINED))
            nmsStyle = nmsStyle.withUnderlined(true);

        nmsComp.setStyle(nmsStyle);

        if(advComp.children().isEmpty())
            return nmsComp;

        List<MutableComponent> nmsChildren = advComp.children().stream()
            .map(this::adventureToNmsComponent)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        if(textContent != null && textContent.isEmpty()) {
            if(nmsChildren.isEmpty())
                return null;

            nmsComp = nmsChildren.get(0);
            nmsChildren.remove(0);
        }

        nmsChildren.forEach(nmsComp::append);
        return nmsComp;
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
                    ✔ Retrieve entity instance
                    ✔ Ensure entity is instanceof LivingEntity
                    ✔ Ensure entity type is not a Player
                    ✔ Generate CustomName NMS component.
                    ⨯ Replace the custom name field in the entity metadata packet with
                      the component.
                 */

                // We want to ensure that the packet we are handling is an entity metadata packet.
                if(!(message instanceof final ClientboundSetEntityDataPacket packet)) {
                    super.write(context, message, promise);
                    return;
                }

                final int entityId = packet.id();

                // Debug information (ignore).
                System.out.println("[Intercepted Entity Metadata Packet]");
                System.out.println("Entity ID: " + entityId);
                if(packet.packedItems() == null) {
                    System.out.println("packedItems == null");
                } else {
                    System.out.println("packedItems-size: " + packet.packedItems().size());
                    System.out.println("packedItems: " + packet.packedItems());
                }

                final Entity entity = getEntityById(entityId);

                if(entity == null) {
                    System.out.println("Unable to find entity by integer ID.");
                    super.write(context, message, promise);
                    return;
                }

                if(!(entity instanceof final LivingEntity lent)) {
                    System.out.printf("'%s' is not a LivingEntity", entity.getType());
                    super.write(context, message, promise);
                    return;
                }

                if(lent.getType() == EntityType.PLAYER) {
                    super.write(context, message, promise);
                    return;
                }

                final Component customNameComponent = generateEntityLabelComponent(lent);
                System.out.printf("Generated custom name: '%s'",
                    PlainTextComponentSerializer.plainText().serialize(customNameComponent));

                final net.minecraft.network.chat.Component customNameNmsComponent =
                    adventureToNmsComponent(customNameComponent);

                //TODO set custom name to component.
                //TODO set customnamevisible to true

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
            new SynchedEntityData(null).getNonDefaultValues()
        );
        playerHandle.connection.send(packet);
        DebugStat.metadataUpdates++;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(final PlayerJoinEvent event) {
        addPacketListenerToPipeline(event.getPlayer());
    }

    private static @Nullable Entity getEntityById(final int entityId) {
        try {
            return Bukkit.getScheduler().callSyncMethod(
                EntityLabelSystem.instance(),
                () -> {
                    for(final World world : Bukkit.getServer().getWorlds()) {
                        final ServerLevel nmsWorld = ((CraftWorld) world).getHandle();
                        final net.minecraft.world.entity.Entity entity =
                            nmsWorld.getEntity(entityId);
                        if(entity == null) continue;
                        return entity.getBukkitEntity();
                    }

                    return null;
                }
            ).get();
        } catch(final Exception ex) {
            EntityLabelSystem.instance().getLogger().severe("Unable to get entity by ID.");
            ex.printStackTrace();
            return null;
        }
    }
}
