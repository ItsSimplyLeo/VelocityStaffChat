package cx.leo.velocity.staffchat;

import com.google.inject.Inject;
import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import cx.leo.velocity.staffchat.listener.ChatListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Plugin(id = "staffchat", name = "StaffChat", version = "1.0")
public class VelocityStaffChat {

    public static MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final Set<UUID> toggledPlayers = new HashSet<>();
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    private String chatMessage;
    private String toggleMessage;

    private Toml config;

    @Inject
    public VelocityStaffChat(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.config = loadConfig(dataDirectory);
        if (config == null) {
            return; // TODO handle
        }
        this.chatMessage = config.getString("format");
        this.toggleMessage = config.getString("toggle");

        this.server.getEventManager().register(this, new ChatListener(this));

        CommandManager commandManager = server.getCommandManager();
        CommandMeta command = commandManager.metaBuilder("staffchat")
                .aliases("sc")
                .plugin(this)
                .build();

        commandManager.register(command, new StaffChatCommand(this));
    }

    private Toml loadConfig(Path path) {
        File folder = path.toFile();
        File file = new File(folder, "config.toml");
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        if (!file.exists()) {
            try (InputStream input = getClass().getResourceAsStream("/" + file.getName())) {
                if (input != null) {
                    Files.copy(input, file.toPath());
                } else {
                    file.createNewFile();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
                return null;
            }
        }

        return new Toml().read(file);
    }

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }

    public Set<UUID> getToggledPlayers() {
        return toggledPlayers;
    }

    public void sendConsoleMessage(String message) {
        sendStaffMessage("Console", null, message);
    }

    public void sendStaffMessage(Player sender, ServerConnection server, String message) {
        sendStaffMessage(sender.getUsername(), server, message);
    }

    public void sendStaffMessage(String displayName, ServerConnection server, String message) {
        String serverName;
        if (server == null) serverName = "N/A";
        else serverName = server.getServerInfo().getName();

        this.server.getAllPlayers().stream().filter(player -> player.hasPermission("staffchat.use")).forEach(player -> player.sendMessage(
                MINI_MESSAGE.deserialize(chatMessage,
                        Placeholder.unparsed("server", serverName),
                        Placeholder.unparsed("player", displayName),
                        Placeholder.unparsed("message", message)
                )
        ));
    }

    public void toggle(Player player) {
        UUID uuid = player.getUniqueId();
        if (toggledPlayers.contains(uuid)) {
            toggledPlayers.remove(uuid);
            player.sendMessage(MINI_MESSAGE.deserialize(toggleMessage, Placeholder.component("status", Component.text("disabled").color(NamedTextColor.RED))));
        }
        else {
            toggledPlayers.add(uuid);
            player.sendMessage(MINI_MESSAGE.deserialize(toggleMessage, Placeholder.component("status", Component.text("enabled").color(NamedTextColor.GREEN))));
        }
    }
}
