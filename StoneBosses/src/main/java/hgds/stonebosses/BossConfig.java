package hgds.stonebosses;

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.ChatColor;

import java.io.File;
import java.util.*;

/**
 * Конфиг одного босса (например zombi_king.yml).
 */
public class BossConfig {
    private final String id;
    private final File file;
    private FileConfiguration config;
    private String name;
    private EntityType mobType;
    private double health;
    private double armor;
    private double damage;
    private double speed;
    private List<String> effectDamage; // "SLOWNESS:5:5"
    private double spawnChildrenHealth;
    private int childrenQuantity;
    private String childrenName;
    private String childrenMob;
    private double childrenHealth;
    private double childrenArmor;
    private double childrenDamage;
    private boolean childrenSpawned;

    public BossConfig(File file) {
        this.file = file;
        this.id = file.getName().replace(".yml", "");
        this.effectDamage = new ArrayList<>();
        this.childrenSpawned = false;
        load();
    }

    public void load() {
        config = YamlConfiguration.loadConfiguration(file);
        name = colorize(getStr("Name", "&cБосс"));
        String mobStr = getStr("mob", "ZOMBIE").toUpperCase();
        try {
            mobType = EntityType.valueOf(mobStr);
        } catch (Exception e) {
            mobType = EntityType.ZOMBIE;
        }
        health = config.getDouble("heatlh", config.getDouble("health", 2048));
        armor = config.getDouble("armor", 40);
        damage = config.getDouble("damage", 20);
        speed = config.getDouble("speed", 0.3);
        effectDamage = config.getStringList("effect_damage");
        spawnChildrenHealth = config.getDouble("spawn_children_heatlh", config.getDouble("spawn_children_health", 500));
        childrenQuantity = config.getInt("children.quantity", 5);
        childrenName = colorize(config.getString("children.name", "&cПриспешник"));
        childrenMob = config.getString("children.mob", "ZOMBIE");
        childrenHealth = config.getDouble("children.heatlh", config.getDouble("children.health", 128));
        childrenArmor = config.getDouble("children.armor", 5);
        childrenDamage = config.getDouble("children.damage", 10);
    }

    private String getStr(String path, String def) {
        return config.getString(path, def);
    }

    private static String colorize(String s) {
        if (s == null) return "";
        return ChatColor.translateAlternateColorCodes('&', s)
            .replace("&x", "§x");
    }

    public LivingEntity spawnBoss(Location loc) {
        if (loc.getWorld() == null) return null;
        LivingEntity entity = (LivingEntity) loc.getWorld().spawnEntity(loc, mobType);
        entity.setCustomName(name);
        entity.setCustomNameVisible(true);
        entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
        entity.setHealth(health);
        entity.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(armor);
        entity.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).setBaseValue(10);
        entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(damage);
        if (entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED) != null) {
            entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speed);
        }
        entity.setRemoveWhenFarAway(false);
        entity.setPersistent(true);
        entity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));
        childrenSpawned = false;
        return entity;
    }

    public void spawnChildren(Location loc) {
        if (loc.getWorld() == null) return;
        EntityType childType;
        try {
            childType = EntityType.valueOf(childrenMob.toUpperCase());
        } catch (Exception e) {
            childType = EntityType.ZOMBIE;
        }
        for (int i = 0; i < childrenQuantity; i++) {
            LivingEntity child = (LivingEntity) loc.getWorld().spawnEntity(loc, childType);
            child.setCustomName(childrenName);
            child.setCustomNameVisible(true);
            child.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(childrenHealth);
            child.setHealth(childrenHealth);
            child.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(childrenArmor);
            child.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(childrenDamage);
            child.setRemoveWhenFarAway(false);
            child.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));
        }
        childrenSpawned = true;
    }

    public boolean shouldSpawnChildren(double currentHealth) {
        return !childrenSpawned && currentHealth <= spawnChildrenHealth;
    }

    public void applyEffectDamage(org.bukkit.entity.Player player) {
        for (String s : effectDamage) {
            String[] parts = s.split(":");
            if (parts.length >= 3) {
                PotionEffectType type = PotionEffectType.getByName(parts[0]);
                if (type != null) {
                    int duration = Integer.parseInt(parts[1]) * 20;
                    int level = Math.max(0, Integer.parseInt(parts[2]) - 1);
                    player.addPotionEffect(new org.bukkit.potion.PotionEffect(type, duration, level));
                }
            }
        }
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public EntityType getMobType() { return mobType; }
    public double getHealth() { return health; }
    public double getArmor() { return armor; }
    public double getDamage() { return damage; }
    public List<String> getEffectDamage() { return effectDamage; }
    public double getSpawnChildrenHealth() { return spawnChildrenHealth; }
    public int getChildrenQuantity() { return childrenQuantity; }
    public String getChildrenName() { return childrenName; }
    public String getChildrenMob() { return childrenMob; }
    public double getChildrenHealth() { return childrenHealth; }
    public double getChildrenArmor() { return childrenArmor; }
    public double getChildrenDamage() { return childrenDamage; }
}
