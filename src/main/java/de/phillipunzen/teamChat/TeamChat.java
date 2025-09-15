package de.phillipunzen.teamChat;

import de.phillipunzen.teamChat.Commands.TC;
import de.phillipunzen.teamChat.Commands.TCReload;
import org.bukkit.plugin.java.JavaPlugin;

public final class TeamChat extends JavaPlugin {

    private RedisManager redisManager;

    @Override
    public void onEnable() {
        // Lade/erstelle Standard-Konfiguration
        this.saveDefaultConfig();

        // Redis initialisieren, wenn aktiviert
        if (this.getConfig().getBoolean("redis.enabled", false)) {
            this.redisManager = new RedisManager(this,
                    this.getConfig().getString("redis.host", "127.0.0.1"),
                    this.getConfig().getInt("redis.port", 6379),
                    this.getConfig().getString("redis.password", null),
                    this.getConfig().getString("redis.channel", "teamchat"));
            this.redisManager.startSubscriber(msg -> {
                // Beim Empfang von Redis-Nachrichten: auf Haupt-Thread verteilen
                this.getServer().getScheduler().runTask(this, () -> {
                    for (org.bukkit.entity.Player p : this.getServer().getOnlinePlayers()) {
                        if (p.hasPermission("tc.use")) {
                            p.sendMessage(msg);
                        }
                    }
                });
            });
        }

        // Registriere Befehle
        this.getCommand("tc").setExecutor(new TC(this));
        this.getCommand("tcreload").setExecutor(new TCReload(this));
        this.getLogger().info("[TeamChat] Plugin enabled");
        this.getLogger().info("[TeamChat] Author: Phillip Unzen (Blackiiiii)");
    }

    @Override
    public void onDisable() {
        if (this.redisManager != null) {
            this.redisManager.shutdown();
        }
        this.getLogger().info("[TeamChat] Plugin disabled");
    }

    public RedisManager getRedisManager() {
        return this.redisManager;
    }
}
