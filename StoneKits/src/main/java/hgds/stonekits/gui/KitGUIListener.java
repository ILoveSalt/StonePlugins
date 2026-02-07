package hgds.stonekits.gui;

import hgds.stonekits.StoneKits;
import hgds.stonekits.models.Kit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class KitGUIListener implements Listener {

    private final StoneKits plugin;

    public KitGUIListener(StoneKits plugin) {
        this.plugin = plugin;
    }

    private static ItemStack cloneOrNull(ItemStack is) {
        return is != null && is.getType() != org.bukkit.Material.AIR ? is.clone() : null;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof KitGUI.KitHolder)) return;
        KitGUI.KitHolder h = (KitGUI.KitHolder) e.getInventory().getHolder();
        if (h.isPreview()) {
            e.setCancelled(true);
            if (e.getRawSlot() == 53) e.getWhoClicked().closeInventory();
            return;
        }
        if (!h.isPreview()) {
            if (e.getRawSlot() == 52) {
                e.setCancelled(true);
                Kit kit = plugin.getKitManager().getKit(h.getKitId());
                if (kit != null) {
                    java.util.List<ItemStack> items = new java.util.ArrayList<>();
                    for (int i = 0; i < 36; i++) {
                        ItemStack is = e.getInventory().getItem(i);
                        if (is != null && is.getType() != org.bukkit.Material.AIR) items.add(is.clone());
                    }
                    ItemStack[] armor = new ItemStack[]{
                        cloneOrNull(e.getInventory().getItem(45)),
                        cloneOrNull(e.getInventory().getItem(46)),
                        cloneOrNull(e.getInventory().getItem(47)),
                        cloneOrNull(e.getInventory().getItem(48))
                    };
                    kit.setItems(items);
                    kit.setArmor(armor);
                    kit.setOffhand(cloneOrNull(e.getInventory().getItem(49)));
                    java.util.Map<Integer, ItemStack> hotbar = new java.util.HashMap<>();
                    for (int i = 0; i < 9; i++) {
                        ItemStack is = e.getInventory().getItem(50 + i);
                        if (is != null && is.getType() != org.bukkit.Material.AIR) hotbar.put(i, is.clone());
                    }
                    kit.setHotbar(hotbar);
                    plugin.getKitManager().saveKits();
                    ((Player) e.getWhoClicked()).sendMessage("§aНабор §f" + kit.getDisplayName() + " §aуспешно сохранён!");
                }
                e.getWhoClicked().closeInventory();
                return;
            }
            if (e.getRawSlot() == 53) {
                e.setCancelled(true);
                e.getWhoClicked().closeInventory();
                return;
            }
        }
    }
}
