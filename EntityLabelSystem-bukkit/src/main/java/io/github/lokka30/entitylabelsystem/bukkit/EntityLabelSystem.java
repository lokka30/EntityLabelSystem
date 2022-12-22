package io.github.lokka30.entitylabelsystem.bukkit;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import io.github.lokka30.entitylabelsystem.bukkit.command.ElsCommand;
import io.github.lokka30.entitylabelsystem.bukkit.listener.PacketLabelUpdateListener;
import io.github.lokka30.entitylabelsystem.bukkit.packet.LabelUtil;
import io.github.lokka30.entitylabelsystem.bukkit.packet.protocollib.ProtocolLibLabelUtil;
import io.github.lokka30.entitylabelsystem.bukkit.util.DebugStat;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class EntityLabelSystem extends JavaPlugin {

    public static final LabelUtil LABEL_UTIL_IMPL = new ProtocolLibLabelUtil();

    private static EntityLabelSystem instance;
    private static ProtocolManager protocolManager;

    public static EntityLabelSystem instance() {
        return Objects.requireNonNull(instance, "instance");
    }

    public static ProtocolManager protocolManager() {
        return Objects.requireNonNull(protocolManager, "protocolManager");
    }

    public void reload() {
        saveDefaultConfig();
        reloadConfig();
    }

    @Override
    public void onLoad() {
        instance = this;
        protocolManager = ProtocolLibrary.getProtocolManager();
    }

    @Override
    public void onEnable() {
        loadConfigs();
        loadCommands();
        loadListeners();
        loadTasks();
    }

    private void loadConfigs() {
        saveDefaultConfig();
    }

    private void loadCommands() {
        Objects.requireNonNull(getCommand("els")).setExecutor(new ElsCommand());
    }

    private void loadListeners() {
        getServer().getPluginManager().registerEvents(new PacketLabelUpdateListener(), this);
        LABEL_UTIL_IMPL.registerListeners();
    }

    private void loadTasks() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for(final World world : Bukkit.getWorlds()) {
                    for(final LivingEntity entity : world.getLivingEntities()) {
                        if(entity.getType() != EntityType.ZOMBIE) continue;
                        LABEL_UTIL_IMPL.sendUpdatePacket(entity);
                        DebugStat.metadataUpdates++;
                    }
                }
            }
        }.runTaskTimer(this, 0L, 20L * 3);
    }

    public static void debugLog(final String msg) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GRAY + msg);
    }

}
