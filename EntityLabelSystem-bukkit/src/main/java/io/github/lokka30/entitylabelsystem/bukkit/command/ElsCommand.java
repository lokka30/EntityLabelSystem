package io.github.lokka30.entitylabelsystem.bukkit.command;

import static io.github.lokka30.entitylabelsystem.bukkit.EntityLabelSystem.instance;
import static org.bukkit.ChatColor.AQUA;
import static org.bukkit.ChatColor.GRAY;
import static org.bukkit.ChatColor.GREEN;
import static org.bukkit.ChatColor.RED;

import io.github.lokka30.entitylabelsystem.bukkit.util.DebugStat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

public class ElsCommand implements TabExecutor {

    private static final String PERMISSION_ELS_ADMIN = "els.admin";

    @Override
    public boolean onCommand(
        final @NotNull CommandSender sender,
        final @NotNull Command cmd,
        final @NotNull String label,
        final @NotNull String[] args
    ) {
        if(!sender.hasPermission(PERMISSION_ELS_ADMIN)) {
            sender.sendMessage(RED + "You don't have access to that.");
            return true;
        }

        if(args.length == 0) {
            final PluginDescriptionFile pdf = instance().getDescription();
            sender.sendMessage(
                AQUA + "EntityLabelSystem v" + pdf.getVersion(),
                GRAY + " • Authors: " + pdf.getAuthors(),
                GRAY + " • For a list of subcommands, run '/" + label + " help'."
            );
            return true;
        }

        switch (args[0].toUpperCase(Locale.ROOT)) {
            case "HELP" -> {
                if (args.length > 1) {
                    sender.sendMessage(RED + "Invalid usage: '/" + label + " help'");
                    return true;
                }
                sender.sendMessage(
                    AQUA + "[/" + label + " • Command Help]",
                    AQUA + "help" + GRAY + " • View command help.",
                    AQUA + "reload" + GRAY + " • Reload the plugin's configuration.",
                    AQUA + "zombie" + GRAY + " • Spawn in a test dummy zombie.",
                    AQUA + "list" + GRAY + " • List all entities in your world.",
                    AQUA + "metric" + GRAY + " • Metrics collected during this session."
                );
            }
            case "RELOAD" -> {
                if (args.length > 1) {
                    sender.sendMessage(RED + "Invalid usage: '/" + label + " help'");
                    return true;
                }
                sender.sendMessage(GRAY + "Reloading...");
                try {
                    instance().reload();
                } catch (final Exception ex) {
                    instance().getLogger().severe("Reload subcommand failed. Stack trace:");
                    ex.printStackTrace();
                    sender.sendMessage(RED + "Reload failed; see server console for more info.");
                    return true;
                }
                sender.sendMessage(GREEN + "Reload complete.");
            }
            case "ZOMBIE" -> {
                if (args.length > 1) {
                    sender.sendMessage(RED + "Invalid usage: '/" + label + " zombie'");
                    return true;
                }
                if (!(sender instanceof final Player player)) {
                    sender.sendMessage(RED + "Invalid usage: Only players can run this command.");
                    return true;
                }
                final Zombie zombie = (Zombie) player.getWorld()
                    .spawnEntity(player.getLocation(), EntityType.ZOMBIE);
                zombie.setAI(false);
                zombie.setCollidable(false);
                zombie.setGravity(false);
                sender.sendMessage(GREEN + "A test zombie has spawned at your location.");
            }
            case "LIST" -> {
                if (args.length > 1) {
                    sender.sendMessage(RED + "Invalid usage: '/" + label + " list'");
                    return true;
                }
                if (!(sender instanceof final Player player)) {
                    sender.sendMessage(RED + "Invalid usage: Only players can run this command.");
                    return true;
                }
                sender.sendMessage(AQUA + "Entities in world '%s':"
                    .formatted(player.getWorld().getName()));
                for (final LivingEntity entity : player.getWorld().getLivingEntities()) {
                    sender.sendMessage(
                        GRAY + " • • Type: " + entity.getType().name(),
                        GRAY + "   • ID: " + entity.getEntityId(),
                        GRAY + "   • CustomName: " + entity.getCustomName(),
                        GRAY + "   • CustomNameVisible: " + entity.isCustomNameVisible(),
                        GRAY + "   • Distance: " + (player.getLocation().distance(entity.getLocation()) * 100d) / 100d
                    );
                }
                sender.sendMessage(GRAY + "Scanned '%s' living entities."
                    .formatted(player.getWorld().getLivingEntities().size()));
            }
            case "METRIC" -> {
                sender.sendMessage(
                    AQUA + "Test metrics for the current session:",
                    GRAY + " • Entity metadata updates sent: " + DebugStat.metadataUpdates,
                    GRAY + " • Entity metadata packets modified: " + DebugStat.metadataModified
                );
            }
            default -> {
                sender.sendMessage(
                    RED + "Invalid usage: unknown subcommand '" + args[0] + "'.",
                    GRAY + "For a list of subcommands, run '/" + label + " help'."
                );
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(
        final @NotNull CommandSender sender,
        final @NotNull Command cmd,
        final @NotNull String label,
        final @NotNull String[] args
    ) {
        if(!sender.hasPermission(PERMISSION_ELS_ADMIN))
            return Collections.emptyList();

        if(args.length == 0) {
            return List.of("help", "reload", "zombie", "metric");
        } else {
            //noinspection EnhancedSwitchMigration,SwitchStatementWithTooFewBranches
            switch(args[0].toUpperCase(Locale.ROOT)) {
                default:
                    return Collections.emptyList();
            }
        }
    }
}
