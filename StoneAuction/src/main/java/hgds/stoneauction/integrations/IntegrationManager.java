package hgds.stoneauction.integrations;

import hgds.stoneauction.StoneAuction;
import hgds.stonecases.api.StoneCasesAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class IntegrationManager {
    
    private final StoneAuction plugin;
    private StoneCasesAPI stoneCasesAPI;
    
    public IntegrationManager(StoneAuction plugin) {
        this.plugin = plugin;
        loadIntegrations();
    }
    
    private void loadIntegrations() {
        // Загрузка StoneCases
        Plugin casesPlugin = Bukkit.getPluginManager().getPlugin("StoneCases");
        if (casesPlugin != null && casesPlugin.isEnabled()) {
            try {
                stoneCasesAPI = ((hgds.stonecases.StoneCases) casesPlugin).getAPI();
                plugin.getLogger().info("StoneCases интегрирован!");
            } catch (Exception e) {
                plugin.getLogger().warning("Не удалось загрузить StoneCases API: " + e.getMessage());
            }
        }
    }
    
    public StoneCasesAPI getStoneCasesAPI() {
        return stoneCasesAPI;
    }

    public boolean withdraw(Player player, double amount) {
        if (amount <= 0) return true;
        String cmd = plugin.getConfig().getString("economy.command_take", "eco take %player% %amount%")
            .replace("%player%", player.getName()).replace("%amount%", String.valueOf((long) amount));
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        return true;
    }

    public void deposit(Player player, double amount) {
        if (amount <= 0) return;
        String cmd = plugin.getConfig().getString("economy.command_give", "eco give %player% %amount%")
            .replace("%player%", player.getName()).replace("%amount%", String.valueOf((long) amount));
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
    }

    public boolean hasEnough(Player player, double amount) {
        return true;
    }
}
