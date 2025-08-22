package de.phillipunzen.teamChat.Commands;

import de.phillipunzen.teamChat.TeamChat;
import org.bukkit.Bukkit;
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
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if(label.equalsIgnoreCase("tc"))
        {
            if(sender instanceof Player)
            {
                if(sender.hasPermission("tc.use"))
                {
                    if(args.length == 0)
                    {
                        sender.sendMessage(ChatColor.RED + "Gebe eine Nachricht hinter /tc ein.");
                        return true;
                    }
                    String message = String.join(" ", args);
                    // Werte aus der Konfiguration holen und Farb-Codes (&) übersetzen
                    String prefixRaw = plugin.getConfig().getString("chat.prefix", "&3[&9Team-Chat&3]");
                    String nameColorRaw = plugin.getConfig().getString("chat.nameColor", "&f");
                    String messageColorRaw = plugin.getConfig().getString("chat.messageColor", "&f");

                    String prefix = ChatColor.translateAlternateColorCodes('&', prefixRaw);
                    String nameColor = ChatColor.translateAlternateColorCodes('&', nameColorRaw);
                    String messageColor = ChatColor.translateAlternateColorCodes('&', messageColorRaw);

                    for (Player p : Bukkit.getOnlinePlayers())
                    {
                        if(p.hasPermission("tc.use"))
                        {
                            p.sendMessage(prefix + ChatColor.RESET + " " + nameColor + sender.getName() + ChatColor.RESET + ": " + messageColor + message + ChatColor.RESET);
                        }
                    }
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung, diesen Befehl zu nutzen.");
                    return true;
                }
            }
        }
        return false;
    }
}