package hgds.stonebosses;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private FileConfiguration guiConfig;
    private FileConfiguration guiItemsConfig;
    private final File bossesFolder;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.bossesFolder = new File(plugin.getDataFolder(), "bosses");
        if (!bossesFolder.exists()) bossesFolder.mkdirs();
        if (bossesFolder.list() == null || bossesFolder.list().length == 0) {
            try (InputStream in = plugin.getResource("bosses/zombi_king.yml")) {
                if (in != null) {
                    Files.copy(in, new File(bossesFolder, "zombi_king.yml").toPath());
                }
            } catch (Exception ignored) {
            }
        }
        reload();
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        File guiFile = new File(plugin.getDataFolder(), "gui.yml");
        if (!guiFile.exists()) plugin.saveResource("gui.yml", false);
        guiConfig = YamlConfiguration.loadConfiguration(guiFile);
        File guiItemsFile = new File(plugin.getDataFolder(), "gui_items.yml");
        if (!guiItemsFile.exists()) plugin.saveResource("gui_items.yml", false);
        guiItemsConfig = YamlConfiguration.loadConfiguration(guiItemsFile);
    }

    public int getBossesSpawnDelay() {
        return config.getInt("bosses_spawn", 3600);
    }

    public Location getSpawnLocation() {
        String worldName = config.getString("spawn.arena", "world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) world = Bukkit.getWorlds().get(0);
        double x = config.getDouble("spawn.x", 0);
        double y = config.getDouble("spawn.y", 64);
        double z = config.getDouble("spawn.z", 0);
        float yaw = (float) config.getDouble("spawn.yaw", 0);
        return new Location(world, x, y, z, yaw, 0);
    }

    public void setSpawnLocation(Location loc) {
        config.set("spawn.arena", loc.getWorld() != null ? loc.getWorld().getName() : "world");
        config.set("spawn.x", loc.getX());
        config.set("spawn.y", loc.getY());
        config.set("spawn.z", loc.getZ());
        config.set("spawn.yaw", (double) loc.getYaw());
        plugin.saveConfig();
    }

    public List<String> getSpawnBossesMessages() {
        return config.getStringList("message.spawn_bosses");
    }

    public List<String> getDamageBossMessages() {
        return config.getStringList("message.damage_boss");
    }

    public List<String> getKillBossesMessages() {
        return config.getStringList("message.kill_bosses");
    }

    public List<String> getNoKillBossesMessages() {
        return config.getStringList("message.no_kill_bosses");
    }

    public int getDamageMessageInterval() {
        return config.getInt("damage_message_interval", 180);
    }

    public int getCoinsPerDamage() {
        return config.getInt("coins_per_damage", 2);
    }

    public String getActiveBossId() {
        return config.getString("active_boss", null);
    }

    public void setActiveBossId(String id) {
        config.set("active_boss", id);
        plugin.saveConfig();
    }

    public FileConfiguration getGuiConfig() {
        return guiConfig;
    }

    public FileConfiguration getGuiItemsConfig() {
        return guiItemsConfig;
    }

    public void saveGuiItems() {
        try {
            guiItemsConfig.save(new File(plugin.getDataFolder(), "gui_items.yml"));
        } catch (Exception e) {
            plugin.getLogger().warning("Could not save gui_items.yml: " + e.getMessage());
        }
    }

    public File getBossesFolder() {
        return bossesFolder;
    }

    public BossConfig getBossConfig(String id) {
        File f = new File(bossesFolder, id + ".yml");
        if (!f.exists()) return null;
        return new BossConfig(f);
    }

    public List<String> getBossIds() {
        List<String> ids = new ArrayList<>();
        File[] files = bossesFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File f : files) {
                ids.add(f.getName().replace(".yml", ""));
            }
        }
        return ids;
    }
}
