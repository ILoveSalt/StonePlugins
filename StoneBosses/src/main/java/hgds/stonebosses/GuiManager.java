package hgds.stonebosses;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class GuiManager {
    private final StoneBosses plugin;
    private final ConfigManager configManager;
    private static final String GUI_TITLE = ChatColor.translateAlternateColorCodes('&', "&6Призыватель боссов");

    public GuiManager(StoneBosses plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    public void openGui(Player player) {
        FileConfiguration gui = configManager.getGuiConfig();
        FileConfiguration guiItems = configManager.getGuiItemsConfig();
        int size = Math.min(54, Math.max(9, gui.getInt("size", 45)));
        Inventory inv = Bukkit.createInventory(null, size, GUI_TITLE);

        ConfigurationSection items = gui.getConfigurationSection("items");
        if (items != null) {
            for (String slotStr : items.getKeys(false)) {
                int slot = Integer.parseInt(slotStr);
                ConfigurationSection itemSection = items.getConfigurationSection(slotStr);
                if (itemSection == null) continue;
                int id = itemSection.getInt("id", -1);
                int amount = itemSection.getInt("item", 1);
                ItemStack display = getGuiItem(guiItems, id, amount);
                if (display != null && !display.getType().isAir()) {
                    inv.setItem(slot, display);
                }
            }
        }

        ItemStack summonBtn = new ItemStack(Material.LIME_WOOL);
        ItemMeta meta = summonBtn.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "Призвать босса");
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Положите нужные предметы в инвентарь",
                ChatColor.GRAY + "и нажмите сюда для призыва."
            ));
            summonBtn.setItemMeta(meta);
        }
        inv.setItem(size - 5, summonBtn);

        player.openInventory(inv);
    }

    public ItemStack getGuiItem(FileConfiguration guiItems, int id, int amount) {
        ConfigurationSection items = guiItems.getConfigurationSection("items");
        if (items == null) return null;
        String idStr = String.valueOf(id);
        if (!items.contains(idStr)) return null;
        ConfigurationSection s = items.getConfigurationSection(idStr);
        if (s == null) return null;
        String matStr = s.getString("material", "STONE");
        Material mat;
        try {
            mat = Material.valueOf(matStr);
        } catch (Exception e) {
            mat = Material.STONE;
        }
        if (matStr.startsWith("basehead-") || mat == Material.PLAYER_HEAD) {
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD, amount);
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
            if (skullMeta != null) {
                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.randomUUID()));
                skullMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', s.getString("display_name", "Предмет")));
                skull.setItemMeta(skullMeta);
            }
            return skull;
        }
        ItemStack stack = new ItemStack(mat, amount);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', s.getString("display_name", mat.name())));
            stack.setItemMeta(meta);
        }
        return stack;
    }

    public boolean trySummonFromGui(Player player) {
        FileConfiguration gui = configManager.getGuiConfig();
        FileConfiguration guiItems = configManager.getGuiItemsConfig();
        ConfigurationSection items = gui.getConfigurationSection("items");
        if (items == null) return false;

        Map<Integer, Integer> required = new HashMap<>();
        for (String slotStr : items.getKeys(false)) {
            ConfigurationSection itemSection = items.getConfigurationSection(slotStr);
            if (itemSection == null) continue;
            int id = itemSection.getInt("id", -1);
            int amount = itemSection.getInt("item", 1);
            if (id >= 0) required.merge(id, amount, Integer::sum);
        }

        Map<Integer, Integer> toRemoveById = new HashMap<>();
        for (Map.Entry<Integer, Integer> e : required.entrySet()) {
            int id = e.getKey();
            int need = e.getValue();
            ItemStack template = getGuiItem(guiItems, id, 1);
            if (template == null) continue;
            int has = countSimilar(player, template);
            if (has < need) {
                player.sendMessage(ChatColor.RED + "У вас недостаточно предметов для призыва босса.");
                return false;
            }
            toRemoveById.put(id, need);
        }

        for (Map.Entry<Integer, Integer> e : toRemoveById.entrySet()) {
            ItemStack template = getGuiItem(guiItems, e.getKey(), 1);
            if (template == null) continue;
            removeSimilar(player, template, e.getValue());
        }
        plugin.getBossManager().spawnBoss();
        player.sendMessage(ChatColor.GREEN + "Босс призван!");
        return true;
    }

    private int countSimilar(Player player, ItemStack template) {
        int count = 0;
        for (ItemStack content : player.getInventory().getContents()) {
            if (content != null && !content.getType().isAir() && isSimilar(content, template)) {
                count += content.getAmount();
            }
        }
        return count;
    }

    private void removeSimilar(Player player, ItemStack template, int amount) {
        int left = amount;
        for (int i = 0; i < player.getInventory().getSize() && left > 0; i++) {
            ItemStack content = player.getInventory().getItem(i);
            if (content == null || content.getType().isAir() || !isSimilar(content, template)) continue;
            int take = Math.min(content.getAmount(), left);
            content.setAmount(content.getAmount() - take);
            left -= take;
        }
    }

    private boolean isSimilar(ItemStack a, ItemStack b) {
        if (a.getType() != b.getType()) return false;
        if (a.hasItemMeta() && b.hasItemMeta() && a.getItemMeta() != null && b.getItemMeta() != null) {
            if (a.getItemMeta().hasDisplayName() != b.getItemMeta().hasDisplayName()) return false;
            if (a.getItemMeta().hasDisplayName() && !Objects.equals(a.getItemMeta().getDisplayName(), b.getItemMeta().getDisplayName())) return false;
        }
        return true;
    }
}
