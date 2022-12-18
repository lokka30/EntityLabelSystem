package io.github.lokka30.entitylabelsystem.bukkit.packet;

import com.google.gson.JsonElement;
import io.netty.buffer.Unpooled;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.TextComponentSerializer;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

@SuppressWarnings("all") //TODO remove. this class is on hold until the protocol lib implementation works.
public class NmsLabelUtil implements LabelUtil {

    public void registerListeners() {}

    @Override
    public void sendUpdatePacket(
        final LivingEntity entity
    ) {
        for(final Player player : entity.getWorld().getPlayers()) {
            sendUpdatePacket(entity, player);
        }
    }

    @Override
    public void sendUpdatePacket(
        final LivingEntity entity,
        final Player player
    ) {
        final String label = "%s/%s❤ (randint: %s)".formatted(
            entity.getHealth(),
            Objects.requireNonNull(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue(),
            ThreadLocalRandom.current().nextInt(1, 100)
        );

        final TextComponent labelComponent = new TextComponent(label);
        final TextComponentSerializer labelComponentSerializer = new TextComponentSerializer();
        final JsonElement labelComponentJson = labelComponentSerializer
            .serialize(labelComponent, labelComponent.getClass(), null);
        final Optional<IChatBaseComponent> icomp = Optional.ofNullable(
            IChatBaseComponent.ChatSerializer.a(labelComponentJson));

        final PacketPlayOutEntityMetadata packet;
        final PacketDataSerializer pdSerializer = new PacketDataSerializer(Unpooled.buffer());

    }
}
