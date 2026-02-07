package hgds.stoneauction.categories;

import hgds.stoneauction.StoneAuction;
import hgds.stoneauction.models.AuctionLot;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DonateAuctionCategory extends AuctionCategory {
    
    private final StoneAuction plugin;
    
    public DonateAuctionCategory(StoneAuction plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public AuctionLot createLot(Player seller, Object item, double price, int amount) {
        if (!(item instanceof String)) return null;
        return new AuctionLot(UUID.randomUUID().toString(), seller.getUniqueId(), "donate", item, price, 1);
    }
    
    @Override
    public boolean buyLot(Player buyer, AuctionLot lot, int amount) {
        if (amount < 1) return false;
        double total = lot.getPrice() * amount;
        if (!plugin.getIntegrationManager().hasEnough(buyer, total)) return false;
        plugin.getIntegrationManager().withdraw(buyer, total);
        org.bukkit.OfflinePlayer seller = org.bukkit.Bukkit.getOfflinePlayer(lot.getSellerId());
        if (seller.getPlayer() != null && seller.getPlayer().isOnline()) {
            plugin.getIntegrationManager().deposit(seller.getPlayer(), total);
        }
        lot.setAmount(lot.getAmount() - amount);
        return true;
    }
    
    @Override
    public String getName() {
        return "Донатный аукцион";
    }
}
