package io.github.lokka30.entitylabelsystem.bukkit.packet;

import io.github.lokka30.entitylabelsystem.bukkit.EntityLabelSystem;
import java.text.DecimalFormat;
import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public interface LabelUtil {

    void registerListeners();

    void unregisterListeners();

    default void sendUpdatePacket(
        final LivingEntity entity
    ) {
        if(!EntityLabelSystem.DO_UPDATE_PACKETS) return;

        //debugLog("--- START sending update packets ---");

        for(final Player player : entity.getWorld().getPlayers()) {
            /*
            debugLog("--- START sending update packet ---");
            debugLog("entityId: " + entity.getEntityId());
            debugLog("entityType: " + entity.getType().name());
            debugLog("packetRecipient: " + player.getName());
             */

            sendUpdatePacket(entity, player);

            //debugLog("--- DONE sending update packet ---");
        }

        //debugLog("--- DONE sending update packets ---");
    }

    void sendUpdatePacket(
        final LivingEntity entity,
        final Player player
    );

    default Component generateEntityLabelComponent(
        final LivingEntity entity
    ) {
        final int level = entity.getEntityId();

        final Component name = LegacyComponentSerializer.legacySection()
            .deserialize("§f" + entity.getName());

        final DecimalFormat df = new DecimalFormat("#,##0.00");

        final String health = df.format(entity.getHealth());

        //noinspection deprecation
        final String maxHealth = df.format(entity.getMaxHealth()) + " ♥";

        return Component.text("Lvl." + level).color(NamedTextColor.BLUE)
            .appendSpace()
            .append(name)
            .appendSpace()
            .append(Component.text("(").color(NamedTextColor.DARK_GRAY))
            .append(Component.text(health).color(NamedTextColor.RED))
            .append(Component.text("/").color(NamedTextColor.DARK_GRAY))
            .append(Component.text(maxHealth).color(NamedTextColor.RED))
            .append(Component.text(")").color(NamedTextColor.DARK_GRAY));
    }

    default String generateEntityLabel(
        final LivingEntity entity
    ) {
        //noinspection deprecation
        final double maxHealth = entity.getMaxHealth();

        return "%sLvl.%s%s %s%s %s(%s%s%s/%s%s❤%s)".formatted(
            // 1     2 3
            // |_    |_|_
            // %sLvl.%s%s
            ChatColor.BLUE,                                     // 1
            ThreadLocalRandom.current().nextInt(1, 101),  // 2
            ChatColor.RESET,                                           // 3

            // 1 2
            // |_|_
            // %s%s
            entity.getName(), // 1
            ChatColor.RESET,  // 2

            // 1  2 3 4  5 6  7
            // |_ |_|_|_ |_|_ |_
            // %s(%s%s%s/%s%s❤%s)
            ChatColor.DARK_GRAY,                 // 1
            ChatColor.RED,                       // 2
            (entity.getHealth() * 100d) / 100d,  // 3
            ChatColor.DARK_GRAY,                 // 4
            ChatColor.RED,                       // 5
            (maxHealth * 100d) / 100d,           // 6
            ChatColor.DARK_GRAY                  // 7
        );
    }

}
