package com.example.discordstatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Level;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * DiscordStatusPlugin - Changes a Discord channel name on server start and stop.
 */
public final class DiscordStatusPlugin extends JavaPlugin {

    private static final String DISCORD_API_BASE = "https://discord.com/api/v10";
    private static final String DEFAULT_TOKEN = "YOUR_BOT_TOKEN_HERE";
    private static final String DEFAULT_CHANNEL = "YOUR_CHANNEL_ID_HERE";

    @Override
    public void onEnable() {
        saveDefaultConfig();

        final String name = getConfig().getString("online-channel-name", "server-online");
        scheduleChannelUpdate(name);

        getLogger().info("DiscordStatusPlugin enabled.");
    }

    @Override
    public void onDisable() {
        final String name = getConfig().getString("offline-channel-name", "server-offline");
        sendPatchRequest(name);

        getLogger().info("DiscordStatusPlugin disabled.");
    }

    @Override
    public boolean onCommand(
            final CommandSender sender,
            final Command command,
            final String label,
            final String[] args) {

        if (!command.getName().equalsIgnoreCase("discordstatus")) {
            return false;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("discordstatus.reload")) {
                sender.sendMessage("\u00a7cNo permission.");
                return true;
            }
            reloadConfig();
            sender.sendMessage("\u00a7aConfig reloaded.");
            return true;
        }
        sender.sendMessage("\u00a7eUsage: /discordstatus reload");
        return true;
    }

    /**
     * Schedules an asynchronous channel name update.
     *
     * @param channelName the new channel name to set
     */
    private void scheduleChannelUpdate(final String channelName) {
        getServer().getScheduler().runTaskAsynchronously(
            this, () -> sendPatchRequest(channelName));
    }

    /**
     * Sends a PATCH request to the Discord API to rename the channel.
     *
     * @param channelName the new channel name to set
     */
    private void sendPatchRequest(final String channelName) {
        final String token = getConfig().getString("bot-token", "");
        final String channelId = getConfig().getString("channel-id", "");
        final String reason = getConfig().getString(
            "audit-log-reason", "Minecraft Server Status Update");

        if (token.isEmpty() || DEFAULT_TOKEN.equals(token)) {
            getLogger().warning("bot-token is not configured in config.yml");
            return;
        }
        if (channelId.isEmpty() || DEFAULT_CHANNEL.equals(channelId)) {
            getLogger().warning("channel-id is not configured in config.yml");
            return;
        }

        final String body = "{\"name\":\"" + escapeJson(channelName.trim()) + "\"}";
        final String url = DISCORD_API_BASE + "/channels/" + channelId;

        try {
            final HttpClient client = HttpClient.newHttpClient();
            final HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bot " + token)
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("X-Audit-Log-Reason", reason)
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                .build();

            final HttpResponse<String> res =
                client.send(req, HttpResponse.BodyHandlers.ofString());

            logResult(res.statusCode(), channelName);

        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "IO error while calling Discord API", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            getLogger().log(Level.SEVERE, "Discord API request interrupted", ex);
        }
    }

    /**
     * Logs the result of the Discord API call.
     *
     * @param code        HTTP status code returned by Discord
     * @param channelName the channel name that was attempted
     */
    private void logResult(final int code, final String channelName) {
        if (code == 200) {
            getLogger().info("Channel renamed to: " + channelName);
        } else if (code == 429) {
            getLogger().warning("Discord API rate limit reached.");
        } else if (code == 403) {
            getLogger().severe("Missing 'Manage Channels' permission on the Bot.");
        } else if (code == 404) {
            getLogger().severe("Channel not found. Check channel-id in config.yml.");
        } else {
            getLogger().warning("Unexpected status code: " + code);
        }
    }

    /**
     * Escapes special characters for use inside a JSON string value.
     *
     * @param input raw string to escape
     * @return JSON-safe escaped string
     */
    private static String escapeJson(final String input) {
        return input
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }
}