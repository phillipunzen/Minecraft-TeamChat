package de.phillipunzen.teamChat.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TC implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
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
                    String prefix = ChatColor.DARK_AQUA + "[" + ChatColor.BLUE + "Team-Chat" + ChatColor.DARK_AQUA + "]";
                    for (Player p : Bukkit.getOnlinePlayers())
                    {
                        if(p.hasPermission("tc.use"))
                        {
                            p.sendMessage(prefix + " " + sender.getName() + ": " + message);
                        }
                    }
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED + "You don't have the permission to use that command");
                    return true;
                }
            }
        }
        return false;
    }
}