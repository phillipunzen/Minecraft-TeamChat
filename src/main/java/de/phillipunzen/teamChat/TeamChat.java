package de.phillipunzen.teamChat;

import de.phillipunzen.teamChat.Commands.TC;
import de.phillipunzen.teamChat.Commands.TCReload;
import org.bukkit.plugin.java.JavaPlugin;

public final class TeamChat extends JavaPlugin {

    @Override
    public void onEnable() {
        // Lade/erstelle Standard-Konfiguration
        this.saveDefaultConfig();
        // Registriere Befehle
        this.getCommand("tc").setExecutor(new TC(this));
        this.getCommand("tcreload").setExecutor(new TCReload(this));
        this.getLogger().info("[TeamChat] Plugin enabled");
        this.getLogger().info("[TeamChat] Author: Phillip Unzen (Blackiiiii)");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("[TeamChat] Plugin disabled");
    }
}
