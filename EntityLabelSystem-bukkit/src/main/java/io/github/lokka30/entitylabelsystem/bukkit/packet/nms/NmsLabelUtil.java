package io.github.lokka30.entitylabelsystem.bukkit.packet.nms;

import io.github.lokka30.entitylabelsystem.bukkit.packet.LabelUtil;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.syncher.DataWatcher;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class NmsLabelUtil implements LabelUtil {

    /*
    Useful links:
        - https://wiki.vg/Entity_metadata#Entity
        - https://wiki.vg/Entity_metadata#Entity_Metadata_Format
        - https://wiki.vg/Protocol#Set_Entity_Metadata

    TODO List:
        - Consider using LightInjector:
          https://github.com/frengor/LightInjector
          This library is a TinyProtocol replacement which makes it easier for us to intercept
          packets without using ProtocolLib. It seems to be version independent.
        - Create packet interceptor in registerListeners method
        - Unregister listener in unregisterListeners method
        - Convert to reflection.
     */

    public void registerListeners() {
        //TODO
    }

    public void unregisterListeners() {

    }

    @Override
    public void sendUpdatePacket(
        final LivingEntity entity,
        final Player player
    ) {
        final CraftLivingEntity nmsEntity = (CraftLivingEntity) entity;
        final CraftPlayer nmsPlayer = (CraftPlayer) player;

        final PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(
            nmsEntity.getEntityId(),
            getEntityDataWatcher(nmsEntity),
            true
        );

        sendPacketToPlayer(nmsPlayer, packet);
    }

    private static DataWatcher getEntityDataWatcher(
        final CraftLivingEntity nmsEntity
    ) {
        return nmsEntity.getHandle().ai();
    }

    private static void sendPacketToPlayer(
        final CraftPlayer nmsPlayer,
        final Packet<?> packet
    ) {
        nmsPlayer.getHandle().b.a(packet);
    }
}
