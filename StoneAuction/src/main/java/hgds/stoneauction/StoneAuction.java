package hgds.stoneauction;

import hgds.stoneauction.commands.AuctionCommand;
import hgds.stoneauction.integrations.IntegrationManager;
import hgds.stoneauction.managers.AuctionManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class StoneAuction extends JavaPlugin {
    
    private static StoneAuction instance;
    private AuctionManager auctionManager;
    private IntegrationManager integrationManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        saveDefaultConfig();
        
        integrationManager = new IntegrationManager(this);

        auctionManager = new AuctionManager(this);

        getCommand("ah").setExecutor(new AuctionCommand(this));
        getCommand("dah").setExecutor(new AuctionCommand(this));
        getCommand("cah").setExecutor(new AuctionCommand(this));
        getServer().getPluginManager().registerEvents(new hgds.stoneauction.gui.AuctionGUIListener(this), this);

        getLogger().info("StoneAuction включен!");
    }
    
    @Override
    public void onDisable() {
        if (auctionManager != null) {
            auctionManager.saveAll();
        }
        getLogger().info("StoneAuction выключен!");
    }
    
    public static StoneAuction getInstance() {
        return instance;
    }
    
    public AuctionManager getAuctionManager() {
        return auctionManager;
    }
    
    public IntegrationManager getIntegrationManager() {
        return integrationManager;
    }
}
