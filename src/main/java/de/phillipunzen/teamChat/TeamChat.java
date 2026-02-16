package de.phillipunzen.teamChat;

import de.phillipunzen.teamChat.Commands.TC;
import de.phillipunzen.teamChat.Commands.TCReload;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class TeamChat extends JavaPlugin {

    private RedisManager redisManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        setupRedis();

        getCommand("tc").setExecutor(new TC(this));
        getCommand("tcreload").setExecutor(new TCReload(this));

        getLogger().info("[TeamChat] Plugin enabled");
        getLogger().info("[TeamChat] Author: Phillip Unzen (Blackiiiii)");
    }

    @Override
    public void onDisable() {
        shutdownRedis();
        getLogger().info("[TeamChat] Plugin disabled");
    }

    public void reloadTeamChat() {
        reloadConfig();
        shutdownRedis();
        setupRedis();
    }

    public RedisManager getRedisManager() {
        return redisManager;
    }

    private void setupRedis() {
        if (!getConfig().getBoolean("redis.enabled", false)) {
            getLogger().info("Redis-Integration ist deaktiviert.");
            return;
        }

        String host = getConfig().getString("redis.host", "127.0.0.1");
        int port = getConfig().getInt("redis.port", 6379);
        String password = getConfig().getString("redis.password", null);
        String channel = getConfig().getString("redis.channel", "teamchat");
        String serverName = getConfig().getString("redis.serverName", "unknown-server");

        redisManager = new RedisManager(this, host, port, password, channel, serverName);
        redisManager.startSubscriber(payload ->
                getServer().getScheduler().runTask(this, () -> {
                    String formatted = formatMessage(payload.getSender(), payload.getMessage(), payload.getServerName());
                    for (Player player : getServer().getOnlinePlayers()) {
                        if (player.hasPermission("tc.use")) {
                            player.sendMessage(formatted);
                        }
                    }
                })
        );
    }

    private void shutdownRedis() {
        if (redisManager != null) {
            redisManager.shutdown();
            redisManager = null;
        }
    }

    public String formatMessage(String sender, String message, String sourceServer) {
        String prefixRaw = getConfig().getString("chat.prefix", "&3[&9Team-Chat&3]");
        String nameColorRaw = getConfig().getString("chat.nameColor", "&f");
        String messageColorRaw = getConfig().getString("chat.messageColor", "&f");
        String serverTagColorRaw = getConfig().getString("chat.serverTagColor", "&8");

        String prefix = ChatColor.translateAlternateColorCodes('&', prefixRaw);
        String nameColor = ChatColor.translateAlternateColorCodes('&', nameColorRaw);
        String messageColor = ChatColor.translateAlternateColorCodes('&', messageColorRaw);
        String serverTagColor = ChatColor.translateAlternateColorCodes('&', serverTagColorRaw);

        String serverTag = "";
        if (sourceServer != null && !sourceServer.isBlank()) {
            serverTag = " " + serverTagColor + "[" + sourceServer + "]" + ChatColor.RESET;
        }

        return prefix + ChatColor.RESET + serverTag + " " + nameColor + sender + ChatColor.RESET + ": " + messageColor + message + ChatColor.RESET;
    }
}
