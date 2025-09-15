package de.phillipunzen.teamChat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;

import java.util.UUID;
import java.util.function.Consumer;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class RedisManager {
    private final TeamChat plugin;
    private final String host;
    private final int port;
    private final String password;
    private final String channel;
    private final String serverId;

    private Thread subThread;
    private JedisPubSub pubSub;
    private final Gson gson = new Gson();

    public RedisManager(TeamChat plugin, String host, int port, String password, String channel) {
        this.plugin = plugin;
        this.host = host;
        this.port = port;
        this.password = password;
        this.channel = channel;
        this.serverId = UUID.randomUUID().toString();
    }

    public String getServerId() {
        return serverId;
    }

    public void startSubscriber(Consumer<String> messageHandler) {
        pubSub = new JedisPubSub() {
            @Override
            public void onMessage(String ch, String message) {
                try {
                    JsonObject obj = gson.fromJson(message, JsonObject.class);
                    if (obj == null || !obj.has("serverId") || !obj.has("message")) return;
                    String origin = obj.get("serverId").getAsString();
                    String msg = obj.get("message").getAsString();
                    if (origin.equals(serverId)) return; // Nachricht vom selben Server ignorieren
                    messageHandler.accept(msg);
                } catch (Exception e) {
                    plugin.getLogger().severe("Fehler beim Verarbeiten einer Redis-Nachricht: " + e.getMessage());
                }
            }
        };

        subThread = new Thread(() -> {
            try (Jedis jedis = new Jedis(host, port)) {
                if (password != null && !password.isEmpty()) {
                    jedis.auth(password);
                }
                jedis.subscribe(pubSub, channel);
            } catch (Exception e) {
                plugin.getLogger().severe("Redis-Subscriber beendet mit Fehler: " + e.getMessage());
            }
        }, "TeamChat-Redis-Subscriber");
        subThread.setDaemon(true);
        subThread.start();
    }

    public void publish(String message) {
        try (Jedis jedis = new Jedis(host, port)) {
            if (password != null && !password.isEmpty()) {
                jedis.auth(password);
            }
            JsonObject obj = new JsonObject();
            obj.addProperty("serverId", serverId);
            obj.addProperty("message", message);
            String json = gson.toJson(obj);
            jedis.publish(channel, json);
        } catch (Exception e) {
            plugin.getLogger().severe("Redis-Publish fehlgeschlagen: " + e.getMessage());
        }
    }

    public void shutdown() {
        try {
            if (pubSub != null) pubSub.unsubscribe();
        } catch (Exception ignored) {}
        try {
            if (subThread != null) subThread.interrupt();
        } catch (Exception ignored) {}
    }
}

