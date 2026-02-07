package hgds.stonebosses.integrations;

import hgds.stonebosses.StoneBosses;
import hgds.stoneevent.api.StoneEventAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class IntegrationManager {
    
    private final StoneBosses plugin;
    private StoneEventAPI stoneEventAPI;
    
    public IntegrationManager(StoneBosses plugin) {
        this.plugin = plugin;
        loadIntegrations();
    }
    
    private void loadIntegrations() {
        Plugin eventPlugin = Bukkit.getPluginManager().getPlugin("StoneEvent");
        if (eventPlugin != null && eventPlugin.isEnabled()) {
            try {
                stoneEventAPI = ((hgds.stoneevent.StoneEvent) eventPlugin).getAPI();
                plugin.getLogger().info("StoneEvent интегрирован!");
            } catch (Exception e) {
                plugin.getLogger().warning("Не удалось загрузить StoneEvent API: " + e.getMessage());
            }
        }
    }
    
    public StoneEventAPI getStoneEventAPI() {
        return stoneEventAPI;
    }
}
