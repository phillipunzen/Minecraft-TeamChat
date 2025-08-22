package de.phillipunzen.teamChat.Commands;

import de.phillipunzen.teamChat.TeamChat;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class TCReload implements CommandExecutor {

    private final TeamChat plugin;

    public TCReload(TeamChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("tc.admin")) {
            sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung, diesen Befehl zu nutzen.");
            return true;
        }
        plugin.reloadConfig();
        sender.sendMessage(ChatColor.GREEN + "TeamChat-Konfiguration wurde neu geladen.");
        return true;
    }
}
