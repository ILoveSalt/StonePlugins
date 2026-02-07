package hgds.stonekits.models;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Kit {

    private final String id;
    private String displayName;
    private String icon; // material name
    private long cooldownSeconds;
    private int maxUses;
    private String permission;
    private List<ItemStack> items = new ArrayList<>();
    private ItemStack[] armor = new ItemStack[4]; // boots, leggings, chestplate, helmet
    private ItemStack offhand;
    private Map<Integer, ItemStack> hotbar = new HashMap<>();

    public Kit(String id) {
        this.id = id;
        this.displayName = id;
        this.icon = "CHEST";
        this.cooldownSeconds = 0;
        this.maxUses = -1;
        this.permission = "stonekits.kit." + id.toLowerCase();
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public long getCooldownSeconds() { return cooldownSeconds; }
    public void setCooldownSeconds(long cooldownSeconds) { this.cooldownSeconds = cooldownSeconds; }
    public int getMaxUses() { return maxUses; }
    public void setMaxUses(int maxUses) { this.maxUses = maxUses; }
    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = permission; }
    public List<ItemStack> getItems() { return items; }
    public void setItems(List<ItemStack> items) { this.items = items != null ? items : new ArrayList<>(); }
    public ItemStack[] getArmor() { return armor; }
    public void setArmor(ItemStack[] armor) { this.armor = armor != null ? armor : new ItemStack[4]; }
    public ItemStack getOffhand() { return offhand; }
    public void setOffhand(ItemStack offhand) { this.offhand = offhand; }
    public Map<Integer, ItemStack> getHotbar() { return hotbar; }
    public void setHotbar(Map<Integer, ItemStack> hotbar) { this.hotbar = hotbar != null ? hotbar : new HashMap<>(); }
}
