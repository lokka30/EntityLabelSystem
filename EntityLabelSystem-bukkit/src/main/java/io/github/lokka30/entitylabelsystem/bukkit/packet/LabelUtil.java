package io.github.lokka30.entitylabelsystem.bukkit.packet;

import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public interface LabelUtil {

    void registerListeners();

    void unregisterListeners();

    default void sendUpdatePacket(
        final LivingEntity entity
    ) {
        for(final Player player : entity.getWorld().getPlayers()) {
            sendUpdatePacket(entity, player);
        }
    }

    void sendUpdatePacket(
        final LivingEntity entity,
        final Player player
    );

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
