package hgds.stonechecker;

import hgds.stonechecker.commands.CheckCommand;
import hgds.stonechecker.commands.ContactCommand;
import hgds.stonechecker.listeners.CheckChatListener;
import hgds.stonechecker.listeners.CheckMoveListener;
import hgds.stonechecker.listeners.CheckGUIListener;
import hgds.stonechecker.managers.CheckManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class StoneChecker extends JavaPlugin {

    private static StoneChecker instance;
    private CheckManager checkManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        checkManager = new CheckManager(this);

        getCommand("check").setExecutor(new CheckCommand(this));
        getCommand("contact").setExecutor(new ContactCommand(this));
        getServer().getPluginManager().registerEvents(new CheckChatListener(this), this);
        getServer().getPluginManager().registerEvents(new CheckMoveListener(this), this);
        getServer().getPluginManager().registerEvents(new CheckGUIListener(this), this);

        getLogger().info("StoneChecker включен!");
    }

    @Override
    public void onDisable() {
        getLogger().info("StoneChecker выключен!");
    }

    public static StoneChecker getInstance() { return instance; }
    public CheckManager getCheckManager() { return checkManager; }
}
