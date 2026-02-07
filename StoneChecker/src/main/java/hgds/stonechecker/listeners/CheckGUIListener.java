package hgds.stonechecker.listeners;

import hgds.stonechecker.StoneChecker;
import hgds.stonechecker.gui.CheckGUI;
import hgds.stonechecker.models.CheckSession;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public class CheckGUIListener implements Listener {
    
    private final StoneChecker plugin;
    
    public CheckGUIListener(StoneChecker plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof CheckGUI.CheckHolder)) return;
        event.setCancelled(true);
        
        CheckGUI.CheckHolder h = (CheckGUI.CheckHolder) holder;
        UUID targetId = h.getTargetId();
        CheckSession session = plugin.getCheckManager().getByTarget(targetId);
        if (session == null) return;
        
        Player mod = (Player) event.getWhoClicked();
        if (!session.getModeratorId().equals(mod.getUniqueId())) return;
        
        if ("main".equals(h.getType())) {
            int slot = event.getSlot();
            int durationSlot = plugin.getConfig().getInt("gui.main.items.duration.slot", 13);
            int reasonSlot = plugin.getConfig().getInt("gui.main.items.reason.slot", 15);
            if (slot == durationSlot) {
                CheckGUI.openDurations(plugin, mod, targetId);
            } else if (slot == reasonSlot) {
                CheckGUI.openReasons(plugin, mod, targetId);
            } else if (slot == 22 && session.getBanDuration() != null && session.getBanReason() != null) {
                String cmd = plugin.getConfig().getString("litebans.ban_command", "ban %player% %stonechecker_duration% Читы (%stonechecker_reason%)");
                Player target = Bukkit.getPlayer(targetId);
                if (target != null) {
                    cmd = cmd.replace("%player%", target.getName())
                            .replace("%stonechecker_duration%", String.valueOf(session.getBanDuration()))
                            .replace("%stonechecker_reason%", session.getBanReason());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                }
                plugin.getCheckManager().endSession(session, true);
                mod.sendMessage(plugin.getConfig().getString("messages.ban_applied", "§cБан применён.").replace("%player%", target != null ? target.getName() : "").replace("%stonechecker_duration%", String.valueOf(session.getBanDuration())).replace("%stonechecker_reason%", session.getBanReason()).replace("&", "§"));
                mod.closeInventory();
            }
            return;
        }
        
        if ("durations".equals(h.getType())) {
            int slot = event.getSlot();
            if (slot == 11) { session.setBanDuration(55); CheckGUI.openMain(plugin, mod, session); }
            else if (slot == 13) { session.setBanDuration(90); CheckGUI.openMain(plugin, mod, session); }
            return;
        }
        
        if ("reasons".equals(h.getType())) {
            if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;
            String reason = event.getCurrentItem().getItemMeta().getDisplayName().replaceAll("§[0-9a-fk-or]", "").trim();
            if (!reason.isEmpty()) {
                session.setBanReason(reason);
                CheckGUI.openMain(plugin, mod, session);
            }
            return;
        }
    }
}
