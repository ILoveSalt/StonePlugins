package hgds.stonebosses;

import hgds.stonebosses.commands.BossCommand;
import hgds.stonebosses.integrations.IntegrationManager;
import hgds.stonebosses.managers.BossManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class StoneBosses extends JavaPlugin {
    
    private static StoneBosses instance;
    private BossManager bossManager;
    private IntegrationManager integrationManager;
    
    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        
        integrationManager = new IntegrationManager(this);
        bossManager = new BossManager(this);
        
        getCommand("stonebosses").setExecutor(new BossCommand(this));
        
        getLogger().info("StoneBosses включен!");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("StoneBosses выключен!");
    }
    
    public static StoneBosses getInstance() {
        return instance;
    }
    
    public BossManager getBossManager() {
        return bossManager;
    }
    
    public IntegrationManager getIntegrationManager() {
        return integrationManager;
    }
}
