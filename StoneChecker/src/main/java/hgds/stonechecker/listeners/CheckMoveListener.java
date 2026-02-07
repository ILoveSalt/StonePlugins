package hgds.stonechecker.listeners;

import hgds.stonechecker.StoneChecker;
import hgds.stonechecker.models.CheckSession;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class CheckMoveListener implements Listener {
    
    private final StoneChecker plugin;
    
    public CheckMoveListener(StoneChecker plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getCheckManager().recordActivity(event.getPlayer());
    }
    
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        plugin.getCheckManager().recordActivity(event.getPlayer());
        CheckSession s = plugin.getCheckManager().getByTarget(event.getPlayer().getUniqueId());
        if (s != null && s.isFreeze()) {
            if (event.getFrom().getBlockX() != event.getTo().getBlockX() || event.getFrom().getBlockZ() != event.getTo().getBlockZ() || event.getFrom().getBlockY() != event.getTo().getBlockY()) {
                event.setTo(event.getFrom());
            }
        }
    }
}
