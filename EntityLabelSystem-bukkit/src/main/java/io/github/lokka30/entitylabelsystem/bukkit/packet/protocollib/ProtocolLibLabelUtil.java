package io.github.lokka30.entitylabelsystem.bukkit.packet.protocollib;

import static io.github.lokka30.entitylabelsystem.bukkit.EntityLabelSystem.protocolManager;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import io.github.lokka30.entitylabelsystem.bukkit.EntityLabelSystem;
import io.github.lokka30.entitylabelsystem.bukkit.packet.LabelUtil;
import io.github.lokka30.entitylabelsystem.bukkit.util.ClassUtils;
import io.github.lokka30.entitylabelsystem.bukkit.util.Metric;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;

public class ProtocolLibLabelUtil implements LabelUtil {

    @Override
    public void registerListeners() {
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

                final Zombie zombie = (Zombie) entity;
                final WrappedDataWatcher dataWatcher =
                    WrappedDataWatcher.getEntityWatcher(zombie).deepClone();

                final WrappedDataWatcher.Serializer chatSerializer =
                    Registry.getChatComponentSerializer(true);

                final WrappedDataWatcherObject optChatFieldWatcher =
                    new WrappedDataWatcherObject(2, chatSerializer);

                final Optional<Object> optChatField = Optional.of(
                    WrappedChatComponent.fromChatMessage(
                        generateEntityLabel(zombie)
                    )[0].getHandle()
                );

                dataWatcher.setObject(optChatFieldWatcher, optChatField);

                dataWatcher.setObject(3, true); // set CustomNameVisible=true

                if(ClassUtils
                    .classExists("com.comphenix.protocol.wrappers.WrappedDataValue")
                ) {
                    final List<WrappedDataValue> wrappedDataValueList = new ArrayList<>();

                    for(final WrappedWatchableObject entry : dataWatcher.getWatchableObjects()) {
                        if(entry == null) continue;

                        final WrappedDataWatcherObject watcherObject = entry.getWatcherObject();
                        wrappedDataValueList.add(
                            new WrappedDataValue(
                                watcherObject.getIndex(),
                                watcherObject.getSerializer(),
                                entry.getRawValue()
                            )
                        );
                    }

                    packet.getDataValueCollectionModifier().write(0, wrappedDataValueList);
                } else {
                    packet.getWatchableCollectionModifier()
                        .write(0, dataWatcher.getWatchableObjects());
                }
                
                event.setPacket(packet);
                Metric.metadataModified++;

            }
        });
    }

    @Override
    public void unregisterListeners() {}

    @Override
    public void sendUpdatePacket(
        final LivingEntity entity,
        final Player player
    ) {
        final PacketContainer packet = protocolManager().createPacket(Server.ENTITY_METADATA);
        packet.getIntegers().write(0, entity.getEntityId());
        protocolManager().sendServerPacket(player, packet);
    }
}
