package cx.leo.velocity.staffchat;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public record StaffChatCommand(VelocityStaffChat plugin) implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        boolean isPlayer = source instanceof Player;
        String[] args = invocation.arguments();

        if (args.length == 0) {
            if (isPlayer) plugin.toggle((Player) source);
            else
                source.sendMessage(Component.text("Usage: /" + invocation.alias() + " <message>").color(NamedTextColor.RED));
        } else {
            String message = String.join(" ", args);
            if (isPlayer) {
                Player player = (Player) source;
                plugin.sendStaffMessage(player, player.getCurrentServer().orElse(null), message);
            } else plugin.sendConsoleMessage(message);
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return SimpleCommand.super.suggest(invocation);
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return SimpleCommand.super.suggestAsync(invocation);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("staffchat.use");
    }

}
