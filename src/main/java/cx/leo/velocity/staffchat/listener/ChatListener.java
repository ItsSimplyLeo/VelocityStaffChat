package cx.leo.velocity.staffchat.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import cx.leo.velocity.staffchat.VelocityStaffChat;

public record ChatListener(VelocityStaffChat plugin) {

    @Subscribe
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getToggledPlayers().contains(player.getUniqueId())) return;

        event.setResult(PlayerChatEvent.ChatResult.denied());

        plugin.sendStaffMessage(player, player.getCurrentServer().orElse(null), event.getMessage());
    }

}
