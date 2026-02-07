package hgds.stoneauction.managers;

import hgds.stoneauction.StoneAuction;
import hgds.stoneauction.categories.AuctionCategory;
import hgds.stoneauction.categories.CaseAuctionCategory;
import hgds.stoneauction.categories.DonateAuctionCategory;
import hgds.stoneauction.categories.NormalAuctionCategory;
import hgds.stoneauction.gui.AuctionHolder;
import hgds.stoneauction.models.AuctionLot;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;

public class AuctionManager {

    private static final long EXPIRE_MS = 48L * 60 * 60 * 1000;

    private final StoneAuction plugin;
    private final Map<String, AuctionCategory> categories = new HashMap<>();
    private final Map<String, List<AuctionLot>> activeLots = new HashMap<>();
    private final Map<String, List<AuctionLot>> expiredLots = new HashMap<>();

    public AuctionManager(StoneAuction plugin) {
        this.plugin = plugin;
        initializeCategories();
        loadAll();
        Bukkit.getScheduler().runTaskTimer(plugin, this::checkExpiredLots, 20L * 60, 20L * 60);
    }

    private void initializeCategories() {
        categories.put("normal", new NormalAuctionCategory(plugin));
        categories.put("donate", new DonateAuctionCategory(plugin));
        categories.put("case", new CaseAuctionCategory(plugin));
        activeLots.put("normal", new ArrayList<>());
        activeLots.put("donate", new ArrayList<>());
        activeLots.put("case", new ArrayList<>());
        expiredLots.put("normal", new ArrayList<>());
        expiredLots.put("donate", new ArrayList<>());
        expiredLots.put("case", new ArrayList<>());
    }

    public boolean createLot(Player seller, String category, Object item, double price, int amount) {
        AuctionCategory cat = categories.get(category);
        if (cat == null) return false;
        AuctionLot lot = cat.createLot(seller, item, price, amount);
        if (lot != null) {
            activeLots.get(category).add(lot);
            return true;
        }
        return false;
    }

    public boolean buyLot(Player buyer, String category, String lotId, int amount) {
        AuctionCategory cat = categories.get(category);
        if (cat == null) return false;
        AuctionLot lot = findLot(category, lotId);
        if (lot == null) return false;
        if (!cat.buyLot(buyer, lot, amount)) return false;
        if (lot.getAmount() <= 0) {
            activeLots.get(category).remove(lot);
        }
        return true;
    }

    public AuctionLot findLot(String category, String lotId) {
        for (AuctionLot lot : activeLots.getOrDefault(category, Collections.emptyList())) {
            if (lot.getId().equals(lotId)) return lot;
        }
        return null;
    }

    public boolean removeActiveLot(String category, AuctionLot lot) {
        return activeLots.getOrDefault(category, Collections.emptyList()).remove(lot);
    }

    public void moveToExpired(String category, AuctionLot lot) {
        if (activeLots.get(category).remove(lot)) {
            expiredLots.get(category).add(lot);
        }
    }

    public void checkExpiredLots() {
        long now = System.currentTimeMillis();
        for (String cat : activeLots.keySet()) {
            List<AuctionLot> list = new ArrayList<>(activeLots.get(cat));
            for (AuctionLot lot : list) {
                if (now - lot.getCreatedAt() > EXPIRE_MS) {
                    moveToExpired(cat, lot);
                }
            }
        }
    }

    public void saveAll() {
        File file = new File(plugin.getDataFolder(), "lots.yml");
        YamlConfiguration cfg = new YamlConfiguration();
        saveLots(cfg, "active", activeLots);
        saveLots(cfg, "expired", expiredLots);
        try {
            cfg.save(file);
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка сохранения lots.yml: " + e.getMessage());
        }
    }

    private void saveLots(YamlConfiguration cfg, String prefix, Map<String, List<AuctionLot>> map) {
        for (Map.Entry<String, List<AuctionLot>> e : map.entrySet()) {
            String path = prefix + "." + e.getKey();
            cfg.set(path, null);
            int i = 0;
            for (AuctionLot lot : e.getValue()) {
                String p = path + "." + i++;
                cfg.set(p + ".id", lot.getId());
                cfg.set(p + ".seller", lot.getSellerId().toString());
                cfg.set(p + ".category", lot.getCategory());
                cfg.set(p + ".price", lot.getPrice());
                cfg.set(p + ".amount", lot.getAmount());
                cfg.set(p + ".createdAt", lot.getCreatedAt());
                if (lot.getItem() instanceof ItemStack) {
                    cfg.set(p + ".itemType", "item");
                    cfg.set(p + ".item", ((ItemStack) lot.getItem()).serialize());
                } else if (lot.getItem() instanceof String) {
                    cfg.set(p + ".itemType", "string");
                    cfg.set(p + ".item", lot.getItem());
                }
            }
        }
    }

    private void loadAll() {
        File file = new File(plugin.getDataFolder(), "lots.yml");
        if (!file.exists()) return;
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        loadLots(cfg, "active", activeLots);
        loadLots(cfg, "expired", expiredLots);
    }

    private void loadLots(YamlConfiguration cfg, String prefix, Map<String, List<AuctionLot>> map) {
        for (String cat : Arrays.asList("normal", "donate", "case")) {
            ConfigurationSection sec = cfg.getConfigurationSection(prefix + "." + cat);
            if (sec == null) continue;
            List<AuctionLot> list = new ArrayList<>();
            for (String k : sec.getKeys(false)) {
                AuctionLot lot = loadLot(sec.getConfigurationSection(k), cat);
                if (lot != null) list.add(lot);
            }
            map.put(cat, list);
        }
    }

    @SuppressWarnings("unchecked")
    private AuctionLot loadLot(ConfigurationSection s, String category) {
        if (s == null) return null;
        String id = s.getString("id");
        UUID seller = parseUUID(s.getString("seller"));
        if (id == null || seller == null) return null;
        double price = s.getDouble("price");
        int amount = s.getInt("amount", 1);
        long createdAt = s.getLong("createdAt", System.currentTimeMillis());
        String type = s.getString("itemType", "string");
        Object item;
        if ("item".equals(type)) {
            Object raw = s.get("item");
            if (raw instanceof Map) item = ItemStack.deserialize((Map<String, Object>) raw);
            else item = null;
        } else {
            item = s.getString("item");
        }
        if (item == null) return null;
        return new AuctionLot(id, seller, category, item, price, amount, createdAt);
    }

    private static UUID parseUUID(String s) {
        if (s == null) return null;
        try { return UUID.fromString(s); } catch (Exception e) { return null; }
    }

    public List<AuctionLot> getActiveLots(String category) {
        return new ArrayList<>(activeLots.getOrDefault(category, Collections.emptyList()));
    }

    public List<AuctionLot> getExpiredLots(String category) {
        return new ArrayList<>(expiredLots.getOrDefault(category, Collections.emptyList()));
    }

    public AuctionCategory getCategory(String category) {
        return categories.get(category);
    }

    public long getExpireMs() { return EXPIRE_MS; }

    private static final int LIST_PER_PAGE = 45;
    private static final int EXPIRED_PER_PAGE = 45;

    public void openCategorySelector(Player p) {
        Inventory inv = Bukkit.createInventory(AuctionHolder.category(), 54, "§6Аукцион — Категории");
        fill(inv, Material.GRAY_STAINED_GLASS_PANE);
        inv.setItem(11, item(Material.CHEST, "§eОбычный аукцион", "§7Предметы"));
        inv.setItem(13, item(Material.GOLD_INGOT, "§6Донатный аукцион", "§7Ранги"));
        inv.setItem(15, item(Material.ENDER_CHEST, "§dКейсовый аукцион", "§7Кейсы"));
        inv.setItem(31, item(Material.BARRIER, "§cПросроченные товары", "§7Забрать"));
        p.openInventory(inv);
    }

    public void openListGUI(Player p, String category, int page) {
        List<AuctionLot> list = getActiveLots(category);
        int from = page * LIST_PER_PAGE;
        int to = Math.min(from + LIST_PER_PAGE, list.size());
        String title = "§6" + getCategory(category).getName() + " §7(стр. " + (page + 1) + ")";
        Inventory inv = Bukkit.createInventory(AuctionHolder.list(category, page), 54, title.length() > 32 ? title.substring(0, 32) : title);
        fill(inv, Material.GRAY_STAINED_GLASS_PANE);
        for (int i = from; i < to; i++) {
            AuctionLot lot = list.get(i);
            inv.setItem(i - from, toDisplay(lot));
        }
        inv.setItem(45, item(Material.ARROW, "§e← Категории"));
        if (page > 0) inv.setItem(49, item(Material.PAPER, "§7Назад"));
        if (to < list.size()) inv.setItem(53, item(Material.PAPER, "§7Вперёд"));
        p.openInventory(inv);
    }

    public void openViewGUI(Player p, AuctionLot lot, String category) {
        if (lot == null) { openListGUI(p, category, 0); return; }
        Inventory inv = Bukkit.createInventory(AuctionHolder.view(category, lot), 54, "§6Лот");
        fill(inv, Material.GRAY_STAINED_GLASS_PANE);
        inv.setItem(13, toDisplay(lot));
        inv.setItem(19, item(Material.LIME_DYE, "§aКупить 1", "§7Цена: " + (long) lot.getPrice()));
        inv.setItem(23, item(Material.LIME_DYE, "§aКупить всё", "§7" + lot.getAmount() + " шт., " + (long) (lot.getPrice() * lot.getAmount())));
        inv.setItem(45, item(Material.ARROW, "§e← Назад"));
        p.openInventory(inv);
    }

    public void openExpiredGUI(Player p, String category) {
        List<AuctionLot> list = getExpiredLots(category);
        String title = "§cПросроченные — " + (getCategory(category) != null ? getCategory(category).getName() : category);
        Inventory inv = Bukkit.createInventory(AuctionHolder.expired(category, 0), 54, title.length() > 32 ? title.substring(0, 32) : title);
        fill(inv, Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < Math.min(EXPIRED_PER_PAGE, list.size()); i++) {
            inv.setItem(i, toDisplay(list.get(i)));
        }
        inv.setItem(45, item(Material.ARROW, "§e← Назад"));
        p.openInventory(inv);
    }

    public AuctionLot getLotAtSlot(String category, int page, int slot) {
        List<AuctionLot> list = getActiveLots(category);
        int idx = page * LIST_PER_PAGE + slot;
        return idx >= 0 && idx < list.size() ? list.get(idx) : null;
    }

    public AuctionLot getExpiredLotAtSlot(String category, int page, int slot) {
        List<AuctionLot> list = getExpiredLots(category);
        int idx = page * EXPIRED_PER_PAGE + slot;
        return idx >= 0 && idx < list.size() ? list.get(idx) : null;
    }

    public void returnExpiredToPlayer(Player p, String category, AuctionLot lot) {
        if (!lot.getSellerId().equals(p.getUniqueId())) return;
        expiredLots.get(category).remove(lot);
        if (lot.getItem() instanceof ItemStack) {
            p.getInventory().addItem((ItemStack) lot.getItem());
        } else if (lot.getItem() instanceof String && plugin.getIntegrationManager().getStoneCasesAPI() != null) {
            plugin.getIntegrationManager().getStoneCasesAPI().giveCase(p, (String) lot.getItem(), lot.getAmount());
        }
    }

    private ItemStack toDisplay(AuctionLot lot) {
        if (lot.getItem() instanceof ItemStack) {
            ItemStack copy = ((ItemStack) lot.getItem()).clone();
            ItemMeta m = copy.getItemMeta();
            if (m != null) {
                m.setDisplayName("§f" + (long) lot.getPrice() + " §7за " + lot.getAmount() + " шт.");
                m.setLore(java.util.Arrays.asList("§7ID: " + lot.getId().substring(0, 8)));
            }
            copy.setItemMeta(m);
            return copy;
        }
        ItemStack i = new ItemStack(Material.PAPER);
        ItemMeta m = i.getItemMeta();
        if (m != null) {
            m.setDisplayName("§f" + lot.getItem() + " §7× " + lot.getAmount());
            m.setLore(java.util.Arrays.asList("§7" + (long) lot.getPrice() + " за шт.", "§7ID: " + lot.getId().substring(0, 8)));
        }
        i.setItemMeta(m);
        return i;
    }

    private void fill(Inventory inv, Material m) {
        ItemStack g = new ItemStack(m);
        ItemMeta meta = g.getItemMeta();
        if (meta != null) meta.setDisplayName(" ");
        g.setItemMeta(meta);
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, g);
    }

    private ItemStack item(Material mat, String name, String... lore) {
        ItemStack i = new ItemStack(mat);
        ItemMeta m = i.getItemMeta();
        if (m != null) {
            m.setDisplayName(name);
            if (lore.length > 0) m.setLore(java.util.Arrays.asList(lore));
        }
        i.setItemMeta(m);
        return i;
    }
}
