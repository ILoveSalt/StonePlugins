package hgds.stonechecker.gui;

import hgds.stonechecker.StoneChecker;
import hgds.stonechecker.models.CheckSession;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class CheckGUI {
    
    public static void openMain(StoneChecker plugin, Player mod, CheckSession session) {
        String title = plugin.getConfig().getString("gui.main.title", "§bПанель проверки");
        if (title.length() > 32) title = title.substring(0, 32);
        Inventory inv = Bukkit.createInventory(new CheckHolder(session.getTargetId(), "main"), 27, title);
        fill(inv, Material.GRAY_STAINED_GLASS_PANE);
        
        int suspectSlot = plugin.getConfig().getInt("gui.main.items.suspect.slot", 11);
        int durationSlot = plugin.getConfig().getInt("gui.main.items.duration.slot", 13);
        int reasonSlot = plugin.getConfig().getInt("gui.main.items.reason.slot", 15);
        
        Player target = Bukkit.getPlayer(session.getTargetId());
        String name = target != null ? target.getName() : "?";
        inv.setItem(suspectSlot, skull(name, "§eПодозреваемый: " + name, "§7Метод: §b" + (session.getMethod() != null ? session.getMethod().name() : ""), "§7Проверяющий: §e" + Bukkit.getOfflinePlayer(session.getModeratorId()).getName()));
        inv.setItem(durationSlot, item(Material.CLOCK, "§eВыбрать срок бана", "§7Срок: " + (session.getBanDuration() != null ? session.getBanDuration() + " дн." : "—")));
        inv.setItem(reasonSlot, item(Material.BOOK, "§eВыбрать причину", "§7Причина: " + (session.getBanReason() != null ? session.getBanReason() : "—")));
        if (session.getBanDuration() != null && session.getBanReason() != null) {
            inv.setItem(22, item(Material.NETHER_STAR, "§cПодтвердить бан", "§7Бан на " + session.getBanDuration() + " дн. | " + session.getBanReason()));
        }
        mod.openInventory(inv);
    }
    
    public static void openDurations(StoneChecker plugin, Player mod, UUID targetId) {
        String title = plugin.getConfig().getString("gui.durations.title", "§bВыбор срока бана");
        if (title.length() > 32) title = title.substring(0, 32);
        Inventory inv = Bukkit.createInventory(new CheckHolder(targetId, "durations"), 27, title);
        fill(inv, Material.GRAY_STAINED_GLASS_PANE);
        inv.setItem(11, item(Material.CLOCK, "§e55 дней", "§7XRAY"));
        inv.setItem(13, item(Material.REDSTONE, "§c90 дней", "§7DELTA, SIGMA, NURSULTAN"));
        mod.openInventory(inv);
    }
    
    public static void openReasons(StoneChecker plugin, Player mod, UUID targetId) {
        String title = plugin.getConfig().getString("gui.reasons.title", "§bВыбор причины");
        if (title.length() > 32) title = title.substring(0, 32);
        Inventory inv = Bukkit.createInventory(new CheckHolder(targetId, "reasons"), 27, title);
        fill(inv, Material.GRAY_STAINED_GLASS_PANE);
        String[] reasons = {"XRAY", "DELTA", "SIGMA", "NURSULTAN", "ASTOLFO", "RAVEN", "LIQUIDBOUNCE"};
        Material[] mats = {Material.IRON_ORE, Material.REDSTONE, Material.NETHER_STAR, Material.TOTEM_OF_UNDYING, Material.PINK_WOOL, Material.FEATHER, Material.SLIME_BALL};
        for (int i = 0; i < reasons.length && i < 9; i++) {
            inv.setItem(i, item(mats[i], "§f" + reasons[i], "§7Нажмите"));
        }
        mod.openInventory(inv);
    }
    
    private static void fill(Inventory inv, Material m) {
        ItemStack g = new ItemStack(m);
        ItemMeta meta = g.getItemMeta();
        if (meta != null) meta.setDisplayName(" ");
        g.setItemMeta(meta);
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, g);
    }
    
    private static ItemStack item(Material m, String name, String... lore) {
        ItemStack i = new ItemStack(m);
        ItemMeta meta = i.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name.replace("&", "§"));
            if (lore.length > 0) meta.setLore(Arrays.stream(lore).map(s -> s.replace("&", "§")).toList());
            i.setItemMeta(meta);
        }
        return i;
    }
    
    private static ItemStack skull(String owner, String name, String... lore) {
        ItemStack i = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) i.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name.replace("&", "§"));
            if (lore.length > 0) meta.setLore(Arrays.stream(lore).map(s -> s.replace("&", "§")).toList());
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(owner));
            i.setItemMeta(meta);
        }
        return i;
    }
    
    public static class CheckHolder implements org.bukkit.inventory.InventoryHolder {
        private final UUID targetId;
        private final String type;
        public CheckHolder(UUID targetId, String type) { this.targetId = targetId; this.type = type; }
        public UUID getTargetId() { return targetId; }
        public String getType() { return type; }
        @Override public Inventory getInventory() { return null; }
    }
}
