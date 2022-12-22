package io.github.lokka30.entitylabelsystem.bukkit.listener;

import static io.github.lokka30.entitylabelsystem.bukkit.EntityLabelSystem.LABEL_UTIL_IMPL;

import io.github.lokka30.entitylabelsystem.bukkit.EntityLabelSystem;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

public class PacketLabelUpdateListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void handle(final EntityDamageEvent event) {
        if(!(event.getEntity() instanceof LivingEntity entity)) return;
        sendDelayedUpdatePacket(entity);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void handle(final EntityRegainHealthEvent event) {
        if(!(event.getEntity() instanceof LivingEntity entity)) return;
        sendDelayedUpdatePacket(entity);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void handle(final EntitySpawnEvent event) {
        if(!(event.getEntity() instanceof LivingEntity entity)) return;
        sendDelayedUpdatePacket(entity);
    }

    private void sendDelayedUpdatePacket(
        final LivingEntity entity
    ) {
        Bukkit.getScheduler().runTaskLater(
            EntityLabelSystem.instance(),
            () -> {
                if(entity == null) return;
                LABEL_UTIL_IMPL.sendUpdatePacket(entity);
            },
            1L
        );
    }

}
