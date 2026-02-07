package hgds.stonekits;

import hgds.stonekits.commands.KitCommand;
import hgds.stonekits.gui.KitGUIListener;
import hgds.stonekits.managers.KitManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class StoneKits extends JavaPlugin {

    private KitManager kitManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        kitManager = new KitManager(this);
        getCommand("kit").setExecutor(new KitCommand(this));
        getCommand("kit").setTabCompleter(new KitCommand(this));
        getServer().getPluginManager().registerEvents(new KitGUIListener(this), this);
        getLogger().info("StoneKits включен!");
    }

    @Override
    public void onDisable() {
        if (kitManager != null) {
            kitManager.saveKits();
            kitManager.savePlayerData();
        }
        getLogger().info("StoneKits выключен!");
    }

    public KitManager getKitManager() { return kitManager; }
}
