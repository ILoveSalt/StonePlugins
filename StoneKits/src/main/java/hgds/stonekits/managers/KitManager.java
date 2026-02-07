package hgds.stonekits.managers;

import hgds.stonekits.StoneKits;
import hgds.stonekits.models.Kit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class KitManager {

    private final StoneKits plugin;
    private final Map<String, Kit> kits = new HashMap<>();
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    private final Map<UUID, Map<String, Integer>> uses = new HashMap<>();

    public KitManager(StoneKits plugin) {
        this.plugin = plugin;
        loadKits();
        loadPlayerData();
    }

    private void loadKits() {
        File file = new File(plugin.getDataFolder(), "kits.yml");
        if (!file.exists()) return;
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = cfg.getConfigurationSection("kits");
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            Kit kit = loadKit(key, section.getConfigurationSection(key));
            if (kit != null) kits.put(key.toLowerCase(), kit);
        }
    }

    private Kit loadKit(String id, ConfigurationSection s) {
        if (s == null) return null;
        Kit kit = new Kit(id);
        kit.setDisplayName(s.getString("display-name", id).replace("&", "§"));
        kit.setIcon(s.getString("icon", "CHEST"));
        kit.setCooldownSeconds(s.getLong("cooldown", 0));
        kit.setMaxUses(s.getInt("max-uses", -1));
        kit.setPermission(s.getString("permission", "stonekits.kit." + id.toLowerCase()));
        List<?> itemsRaw = s.getList("items");
        List<ItemStack> items = new ArrayList<>();
        if (itemsRaw != null) {
            for (Object o : itemsRaw) {
                ItemStack is = parseItem(o);
                if (is != null && is.getType() != Material.AIR) items.add(is);
            }
        }
        kit.setItems(items);
        if (s.contains("armor")) {
            ConfigurationSection arm = s.getConfigurationSection("armor");
            if (arm != null) {
                ItemStack[] arr = new ItemStack[4];
                arr[0] = parseItem(arm.get("boots"));
                arr[1] = parseItem(arm.get("leggings"));
                arr[2] = parseItem(arm.get("chestplate"));
                arr[3] = parseItem(arm.get("helmet"));
                kit.setArmor(arr);
            }
        }
        if (s.contains("offhand")) kit.setOffhand(parseItem(s.get("offhand")));
        if (s.contains("hotbar")) {
            ConfigurationSection hb = s.getConfigurationSection("hotbar");
            if (hb != null) {
                Map<Integer, ItemStack> map = new HashMap<>();
                for (String k : hb.getKeys(false)) {
                    int slot = Integer.parseInt(k);
                    ItemStack is = parseItem(hb.get(k));
                    if (is != null && is.getType() != Material.AIR) map.put(slot, is);
                }
                kit.setHotbar(map);
            }
        }
        return kit;
    }

    private ItemStack parseItem(Object o) {
        if (o == null) return null;
        if (o instanceof ItemStack) return (ItemStack) o;
        if (o instanceof String) {
            String str = (String) o;
            if (str.startsWith("BASE64:")) return null;
            String[] parts = str.split(" ");
            String[] matAmount = parts[0].split(":");
            Material m = Material.matchMaterial(matAmount[0]);
            if (m == null) return null;
            int amt = matAmount.length > 1 ? Integer.parseInt(matAmount[1]) : 1;
            return new ItemStack(m, amt);
        }
        return null;
    }

    public void saveKits() {
        File file = new File(plugin.getDataFolder(), "kits.yml");
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("kits", null);
        for (Kit kit : kits.values()) {
            String path = "kits." + kit.getId();
            cfg.set(path + ".display-name", kit.getDisplayName().replace("§", "&"));
            cfg.set(path + ".icon", kit.getIcon());
            cfg.set(path + ".cooldown", kit.getCooldownSeconds());
            cfg.set(path + ".max-uses", kit.getMaxUses());
            cfg.set(path + ".permission", kit.getPermission());
            List<String> itemsStr = new ArrayList<>();
            for (ItemStack is : kit.getItems()) {
                if (is != null && is.getType() != Material.AIR)
                    itemsStr.add(is.getType().name() + ":" + is.getAmount());
            }
            cfg.set(path + ".items", itemsStr);
            cfg.set(path + ".armor.boots", slotStr(kit.getArmor()[0]));
            cfg.set(path + ".armor.leggings", slotStr(kit.getArmor()[1]));
            cfg.set(path + ".armor.chestplate", slotStr(kit.getArmor()[2]));
            cfg.set(path + ".armor.helmet", slotStr(kit.getArmor()[3]));
            cfg.set(path + ".offhand", slotStr(kit.getOffhand()));
            Map<String, String> hb = new HashMap<>();
            kit.getHotbar().forEach((k, v) -> hb.put(String.valueOf(k), slotStr(v)));
            cfg.set(path + ".hotbar", hb);
        }
        try { cfg.save(file); } catch (Exception e) { plugin.getLogger().warning(e.getMessage()); }
    }

    private String slotStr(ItemStack is) {
        return is != null && is.getType() != Material.AIR ? is.getType().name() + ":" + is.getAmount() : null;
    }

    private void loadPlayerData() {
        File file = new File(plugin.getDataFolder(), "playerdata.yml");
        if (!file.exists()) return;
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection cd = cfg.getConfigurationSection("cooldowns");
        if (cd != null) {
            for (String uuidStr : cd.getKeys(false)) {
                try {
                    UUID u = UUID.fromString(uuidStr);
                    Map<String, Long> map = new HashMap<>();
                    ConfigurationSection sec = cd.getConfigurationSection(uuidStr);
                    if (sec != null) for (String k : sec.getKeys(false)) map.put(k, sec.getLong(k));
                    cooldowns.put(u, map);
                } catch (Exception ignored) {}
            }
        }
        ConfigurationSection us = cfg.getConfigurationSection("uses");
        if (us != null) {
            for (String uuidStr : us.getKeys(false)) {
                try {
                    UUID u = UUID.fromString(uuidStr);
                    Map<String, Integer> map = new HashMap<>();
                    ConfigurationSection sec = us.getConfigurationSection(uuidStr);
                    if (sec != null) for (String k : sec.getKeys(false)) map.put(k, sec.getInt(k));
                    uses.put(u, map);
                } catch (Exception ignored) {}
            }
        }
    }

    public void savePlayerData() {
        File file = new File(plugin.getDataFolder(), "playerdata.yml");
        YamlConfiguration cfg = new YamlConfiguration();
        cooldowns.forEach((uuid, map) -> {
            map.forEach((kitId, time) -> cfg.set("cooldowns." + uuid + "." + kitId, time));
        });
        uses.forEach((uuid, map) -> {
            map.forEach((kitId, count) -> cfg.set("uses." + uuid + "." + kitId, count));
        });
        try { cfg.save(file); } catch (Exception e) { plugin.getLogger().warning(e.getMessage()); }
    }

    public Kit getKit(String name) { return name != null ? kits.get(name.toLowerCase()) : null; }
    public Collection<Kit> getAllKits() { return kits.values(); }

    public boolean giveKit(Player player, Kit kit) {
        if (kit == null) return false;
        if (!player.hasPermission(kit.getPermission())) return false;
        long now = System.currentTimeMillis();
        Map<String, Long> cdMap = cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        Long until = cdMap.get(kit.getId().toLowerCase());
        if (until != null && until > now) return false;
        Map<String, Integer> useMap = uses.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        int used = useMap.getOrDefault(kit.getId().toLowerCase(), 0);
        if (kit.getMaxUses() >= 0 && used >= kit.getMaxUses()) return false;
        int free = 0;
        for (ItemStack is : player.getInventory().getStorageContents()) {
            if (is == null || is.getType() == Material.AIR) free++;
        }
        int needed = kit.getItems().size() + (kit.getOffhand() != null ? 1 : 0) + kit.getHotbar().size();
        for (ItemStack a : kit.getArmor()) if (a != null && a.getType() != Material.AIR) needed++;
        if (free < needed) return false;

        for (ItemStack is : kit.getItems()) {
            if (is != null) player.getInventory().addItem(is.clone());
        }
        if (kit.getOffhand() != null) player.getInventory().setItemInOffHand(kit.getOffhand().clone());
        ItemStack[] arm = kit.getArmor();
        if (arm[0] != null) player.getInventory().setBoots(arm[0].clone());
        if (arm[1] != null) player.getInventory().setLeggings(arm[1].clone());
        if (arm[2] != null) player.getInventory().setChestplate(arm[2].clone());
        if (arm[3] != null) player.getInventory().setHelmet(arm[3].clone());
        kit.getHotbar().forEach((slot, is) -> {
            if (slot >= 0 && slot < 9 && is != null) player.getInventory().setItem(slot, is.clone());
        });

        if (kit.getCooldownSeconds() > 0) cdMap.put(kit.getId().toLowerCase(), now + kit.getCooldownSeconds() * 1000);
        if (kit.getMaxUses() >= 0) useMap.put(kit.getId().toLowerCase(), used + 1);
        return true;
    }

    public long getCooldownLeft(Player player, Kit kit) {
        if (kit == null || kit.getCooldownSeconds() <= 0) return 0;
        Map<String, Long> cd = cooldowns.get(player.getUniqueId());
        if (cd == null) return 0;
        Long until = cd.get(kit.getId().toLowerCase());
        if (until == null) return 0;
        long left = until - System.currentTimeMillis();
        return left > 0 ? left / 1000 : 0;
    }

    public boolean createKit(String name) {
        if (name == null || !name.matches("[a-zA-Z0-9_]+")) return false;
        if (kits.containsKey(name.toLowerCase())) return false;
        Kit kit = new Kit(name);
        kits.put(name.toLowerCase(), kit);
        return true;
    }

    public boolean deleteKit(String name) {
        return kits.remove(name.toLowerCase()) != null;
    }

    public void setKitFromInventory(Kit kit, ItemStack[] contents, ItemStack[] armor, ItemStack offhand) {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < 36; i++) {
            if (contents != null && i < contents.length && contents[i] != null && contents[i].getType() != Material.AIR)
                items.add(contents[i].clone());
        }
        kit.setItems(items);
        kit.setArmor(armor != null ? armor : new ItemStack[4]);
        kit.setOffhand(offhand);
        kit.setHotbar(new HashMap<>());
    }
}
