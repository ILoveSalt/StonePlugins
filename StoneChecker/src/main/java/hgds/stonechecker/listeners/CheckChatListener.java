package hgds.stonechecker.listeners;

import hgds.stonechecker.StoneChecker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class CheckChatListener implements Listener {
    
    private final StoneChecker plugin;
    
    public CheckChatListener(StoneChecker plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (plugin.getCheckManager().getByTarget(event.getPlayer().getUniqueId()) != null) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getConfig().getString("messages.contact_blocked", "§cИспользуйте /contact <сообщение>").replace("&", "§"));
        }
    }
}
