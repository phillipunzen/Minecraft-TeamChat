package de.phillipunzen.teamChat.Commands;

import de.phillipunzen.teamChat.TeamChat;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TC implements CommandExecutor {
    private final TeamChat plugin;

    public TC(TeamChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!label.equalsIgnoreCase("tc")) {
            return false;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Dieser Befehl kann nur von Spielern ausgeführt werden.");
            return true;
        }

        if (!sender.hasPermission("tc.use")) {
            sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung, diesen Befehl zu nutzen.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Gebe eine Nachricht hinter /tc ein.");
            return true;
        }

        String rawMessage = String.join(" ", args);
        String formattedMessage = plugin.formatMessage(sender.getName(), rawMessage, null);

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (player.hasPermission("tc.use")) {
                    player.sendMessage(formattedMessage);
                }
            }
        });

        if (plugin.getConfig().getBoolean("redis.enabled", false) && plugin.getRedisManager() != null) {
            plugin.getRedisManager().publish(sender.getName(), rawMessage);
        }

        return true;
    }
}
