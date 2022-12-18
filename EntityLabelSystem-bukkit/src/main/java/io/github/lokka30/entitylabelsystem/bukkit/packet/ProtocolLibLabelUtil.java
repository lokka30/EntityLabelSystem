package io.github.lokka30.entitylabelsystem.bukkit.packet;

import static io.github.lokka30.entitylabelsystem.bukkit.EntityLabelSystem.debugLog;
import static io.github.lokka30.entitylabelsystem.bukkit.EntityLabelSystem.protocolManager;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import io.github.lokka30.entitylabelsystem.bukkit.EntityLabelSystem;
import io.github.lokka30.entitylabelsystem.bukkit.util.Metric;
import java.util.Optional;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;

public class ProtocolLibLabelUtil implements LabelUtil {

    @Override
    public void registerListeners() {
        debugLog("Registering packet listener");

        protocolManager().addPacketListener(new PacketAdapter(
            EntityLabelSystem.instance(),
            ListenerPriority.NORMAL,
            Server.ENTITY_METADATA
        ) {
            @Override
            public void onPacketSending(final PacketEvent event) {
                if(event.isCancelled()) return;
                if(event.getPacketType() != Server.ENTITY_METADATA) return;

                final PacketContainer packet = event.getPacket();
                final Entity entity = packet.getEntityModifier(event).read(0);

                if(entity == null) return;
                if(entity.getType() != EntityType.ZOMBIE) return;

                debugLog("*** BEGIN Packet Modification ***");

                final Zombie zombie = (Zombie) entity;
                final String label = zombie.getName() + " (" + zombie.getHealth() + "♥)";

                debugLog("Name: " + zombie.getName());
                debugLog("CustomName: " + zombie.getCustomName());
                debugLog("Label: " + label);

                final WrappedDataWatcher dataWatcher =
                    WrappedDataWatcher.getEntityWatcher(zombie).deepClone();

                final WrappedDataWatcher.Serializer chatSerializer =
                    Registry.getChatComponentSerializer(true);

                final WrappedDataWatcherObject optChatFieldWatcher =
                    new WrappedDataWatcher.WrappedDataWatcherObject(2, chatSerializer);

                final Optional<Object> optChatField =
                    Optional.of(WrappedChatComponent.fromChatMessage(label)[0].getHandle());

                dataWatcher.setObject(optChatFieldWatcher, optChatField);

                dataWatcher.setObject(3, true); // set CustomNameVisible=true

                packet.getWatchableCollectionModifier()
                    .write(0, dataWatcher.getWatchableObjects());
                
                event.setPacket(packet);

                debugLog("Done: onPacketSending modified " + ChatColor.GREEN + "[✔]");

                Metric.metadataModified++;
            }
        });
    }

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
        debugLog("Begin: Sending update packet");
        final PacketContainer packet = protocolManager().createPacket(Server.ENTITY_METADATA);
        packet.getIntegers().write(0, entity.getEntityId());
        protocolManager().sendServerPacket(player, packet);
        debugLog("Done: Update packet sent " + ChatColor.GREEN + "[✔]");
    }
}
