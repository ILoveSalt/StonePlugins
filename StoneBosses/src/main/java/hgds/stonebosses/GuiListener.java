package hgds.stonebosses;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class GuiListener implements Listener {
    private final StoneBosses plugin;

    public GuiListener(StoneBosses plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (e.getView().getTitle() == null) return;
        String title = ChatColor.stripColor(e.getView().getTitle());
        String expected = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', "&6Призыватель боссов"));
        if (!title.equals(expected)) return;
        e.setCancelled(true);
        int slot = e.getRawSlot();
        int size = e.getInventory().getSize();
        if (slot == size - 5) {
            Player p = (Player) e.getWhoClicked();
            p.closeInventory();
            if (plugin.getBossManager().getCurrentBoss() != null && plugin.getBossManager().getCurrentBoss().isValid()) {
                p.sendMessage(ChatColor.RED + "Босс уже призван!");
                return;
            }
            plugin.getGuiManager().trySummonFromGui(p);
        }
    }
}
