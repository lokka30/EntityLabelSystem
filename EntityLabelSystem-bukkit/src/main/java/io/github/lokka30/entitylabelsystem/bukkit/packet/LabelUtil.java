package io.github.lokka30.entitylabelsystem.bukkit.packet;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public interface LabelUtil {

    void registerListeners();

    void sendUpdatePacket(
        final LivingEntity entity
    );

    void sendUpdatePacket(
        final LivingEntity entity,
        final Player player
    );

}
