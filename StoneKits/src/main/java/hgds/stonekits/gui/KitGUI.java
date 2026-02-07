package hgds.stonekits.gui;

import hgds.stonekits.StoneKits;
import hgds.stonekits.models.Kit;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class KitGUI {

    public static final String PREVIEW_TITLE = "§6Предпросмотр: ";
    public static final String EDIT_TITLE = "§6Редактор: ";

    public static void openPreview(Player player, Kit kit) {
        String title = PREVIEW_TITLE + kit.getDisplayName();
        if (title.length() > 32) title = title.substring(0, 32);
        Inventory inv = Bukkit.createInventory(new KitHolder(kit.getId(), true), 54, title);
        fillPreview(inv, kit);
        player.openInventory(inv);
    }

    public static void openEdit(Player player, Kit kit) {
        String title = EDIT_TITLE + kit.getDisplayName();
        if (title.length() > 32) title = title.substring(0, 32);
        Inventory inv = Bukkit.createInventory(new KitHolder(kit.getId(), false), 54, title);
        fillEdit(inv, kit);
        player.openInventory(inv);
    }

    private static void fillPreview(Inventory inv, Kit kit) {
        for (int i = 0; i < 54; i++) inv.setItem(i, null);
        int idx = 0;
        for (ItemStack is : kit.getItems()) {
            if (idx < 36 && is != null) inv.setItem(idx++, is.clone());
        }
        ItemStack[] arm = kit.getArmor();
        if (arm != null) {
            if (arm.length > 0 && arm[0] != null) inv.setItem(45, arm[0].clone());
            if (arm.length > 1 && arm[1] != null) inv.setItem(46, arm[1].clone());
            if (arm.length > 2 && arm[2] != null) inv.setItem(47, arm[2].clone());
            if (arm.length > 3 && arm[3] != null) inv.setItem(48, arm[3].clone());
        }
        if (kit.getOffhand() != null) inv.setItem(49, kit.getOffhand().clone());
        kit.getHotbar().forEach((slot, is) -> {
            int invSlot = 50 + slot;
            if (invSlot >= 50 && invSlot <= 58 && is != null) inv.setItem(invSlot, is.clone());
        });
        inv.setItem(53, btn(Material.BARRIER, "§cЗакрыть"));
    }

    private static void fillEdit(Inventory inv, Kit kit) {
        for (int i = 0; i < 54; i++) inv.setItem(i, null);
        int idx = 0;
        for (ItemStack is : kit.getItems()) {
            if (idx < 36 && is != null) inv.setItem(idx++, is.clone());
        }
        ItemStack[] arm = kit.getArmor();
        if (arm != null) {
            if (arm.length > 0 && arm[0] != null) inv.setItem(45, arm[0].clone());
            if (arm.length > 1 && arm[1] != null) inv.setItem(46, arm[1].clone());
            if (arm.length > 2 && arm[2] != null) inv.setItem(47, arm[2].clone());
            if (arm.length > 3 && arm[3] != null) inv.setItem(48, arm[3].clone());
        }
        if (kit.getOffhand() != null) inv.setItem(49, kit.getOffhand().clone());
        kit.getHotbar().forEach((slot, is) -> {
            int invSlot = 50 + slot;
            if (invSlot >= 50 && invSlot <= 58 && is != null) inv.setItem(invSlot, is.clone());
        });
        inv.setItem(52, btn(Material.LIME_DYE, "§aСохранить"));
        inv.setItem(53, btn(Material.BARRIER, "§cОтмена"));
    }

    private static ItemStack btn(Material m, String name) {
        ItemStack i = new ItemStack(m);
        ItemMeta meta = i.getItemMeta();
        if (meta != null) meta.setDisplayName(name);
        i.setItemMeta(meta);
        return i;
    }

    public static class KitHolder implements InventoryHolder {
        private final String kitId;
        private final boolean preview;
        public KitHolder(String kitId, boolean preview) { this.kitId = kitId; this.preview = preview; }
        public String getKitId() { return kitId; }
        public boolean isPreview() { return preview; }
        @Override public Inventory getInventory() { return null; }
    }
}
