package de.phillipunzen.teamChat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class RedisManager {

    public static class ChatPayload {
        private final String serverId;
        private final String serverName;
        private final String sender;
        private final String message;

        public ChatPayload(String serverId, String serverName, String sender, String message) {
            this.serverId = serverId;
            this.serverName = serverName;
            this.sender = sender;
            this.message = message;
        }

        public String getServerId() {
            return serverId;
        }

        public String getServerName() {
            return serverName;
        }

        public String getSender() {
            return sender;
        }

        public String getMessage() {
            return message;
        }
    }

    private final TeamChat plugin;
    private final String host;
    private final int port;
    private final String password;
    private final String channel;
    private final String serverId;
    private final String serverName;

    private Thread subThread;
    private JedisPubSub pubSub;
    private final Gson gson = new Gson();
    private final AtomicBoolean running = new AtomicBoolean(false);

    public RedisManager(TeamChat plugin, String host, int port, String password, String channel, String serverName) {
        this.plugin = plugin;
        this.host = host;
        this.port = port;
        this.password = password;
        this.channel = channel;
        this.serverId = UUID.randomUUID().toString();
        this.serverName = serverName;
    }

    public void startSubscriber(Consumer<ChatPayload> messageHandler) {
        if (running.get()) {
            return;
        }

        running.set(true);
        subThread = new Thread(() -> {
            while (running.get()) {
                try (Jedis jedis = new Jedis(host, port)) {
                    if (password != null && !password.isBlank()) {
                        jedis.auth(password);
                    }

                    pubSub = new JedisPubSub() {
                        @Override
                        public void onMessage(String ch, String message) {
                            handleIncomingMessage(messageHandler, message);
                        }
                    };

                    plugin.getLogger().info("Verbunden mit Redis-Broker " + host + ":" + port + " auf Channel '" + channel + "'.");
                    jedis.subscribe(pubSub, channel);
                } catch (Exception e) {
                    if (!running.get()) {
                        break;
                    }
                    plugin.getLogger().warning("Redis-Subscriber Verbindung verloren: " + e.getMessage() + " | Neuer Verbindungsversuch in 5 Sekunden...");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }, "TeamChat-Redis-Subscriber");

        subThread.setDaemon(true);
        subThread.start();
    }

    private void handleIncomingMessage(Consumer<ChatPayload> messageHandler, String message) {
        try {
            JsonObject obj = gson.fromJson(message, JsonObject.class);
            if (obj == null || !obj.has("serverId") || !obj.has("sender") || !obj.has("message")) {
                return;
            }

            String originId = obj.get("serverId").getAsString();
            if (originId.equals(serverId)) {
                return;
            }

            String originServerName = obj.has("serverName") ? obj.get("serverName").getAsString() : "unknown";
            String sender = obj.get("sender").getAsString();
            String content = obj.get("message").getAsString();

            messageHandler.accept(new ChatPayload(originId, originServerName, sender, content));
        } catch (Exception e) {
            plugin.getLogger().severe("Fehler beim Verarbeiten einer Redis-Nachricht: " + e.getMessage());
        }
    }

    public void publish(String sender, String message) {
        try (Jedis jedis = new Jedis(host, port)) {
            if (password != null && !password.isBlank()) {
                jedis.auth(password);
            }

            JsonObject obj = new JsonObject();
            obj.addProperty("serverId", serverId);
            obj.addProperty("serverName", serverName);
            obj.addProperty("sender", sender);
            obj.addProperty("message", message);

            jedis.publish(channel, gson.toJson(obj));
        } catch (Exception e) {
            plugin.getLogger().severe("Redis-Publish fehlgeschlagen: " + e.getMessage());
        }
    }

    public void shutdown() {
        running.set(false);

        try {
            if (pubSub != null) {
                pubSub.unsubscribe();
            }
        } catch (Exception ignored) {
        }

        try {
            if (subThread != null) {
                subThread.interrupt();
            }
        } catch (Exception ignored) {
        }
    }
}
