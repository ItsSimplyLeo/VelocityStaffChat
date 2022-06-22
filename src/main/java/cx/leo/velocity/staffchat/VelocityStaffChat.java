package cx.leo.velocity.staffchat;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import cx.leo.velocity.staffchat.listener.ChatListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Plugin(id = "staffchat", name = "StaffChat", version = "1.0")
public class VelocityStaffChat {

    private final Set<UUID> toggledPlayers = new HashSet<>();
    private final ProxyServer server;
    private final Logger logger;

    @Inject
    public VelocityStaffChat(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        server.getEventManager().register(this, new ChatListener(this));

        CommandManager commandManager = server.getCommandManager();
        CommandMeta command = commandManager.metaBuilder("staffchat")
                .aliases("sc")
                .plugin(this)
                .build();

        commandManager.register(command, new StaffChatCommand(this));
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

        this.server.getAllPlayers().stream().filter(player -> player.hasPermission("staffchat.use")).forEach(player -> player.sendMessage(Component.text()
                .append(Component.text("[" + serverName + "]").color(NamedTextColor.YELLOW))
                .append(Component.space())
                .append(Component.text(displayName).color(NamedTextColor.WHITE))
                .append(Component.text(": ").color(NamedTextColor.GRAY))
                .append(Component.space())
                .append(Component.text(message))));
    }

    public void toggle(Player player) {
        UUID uuid = player.getUniqueId();
        if (toggledPlayers.contains(uuid)) toggledPlayers.remove(uuid);
        else toggledPlayers.add(uuid);
    }
}
