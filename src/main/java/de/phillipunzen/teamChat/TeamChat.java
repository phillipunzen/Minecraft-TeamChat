package de.phillipunzen.teamChat;

import de.phillipunzen.teamChat.Commands.TC;
import org.bukkit.plugin.java.JavaPlugin;

public final class TeamChat extends JavaPlugin {

    @Override
    public void onEnable() {
        this.getCommand("tc").setExecutor(new TC());
        this.getLogger().info("[TeamChat] Plugin enabled");
        this.getLogger().info("[TeamChat] Author: Phillip Unzen (Blackiiiii)");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("[TeamChat] Plugin disabled");
    }
}
