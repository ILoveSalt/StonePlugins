package hgds.stoneauction.categories;

import hgds.stoneauction.StoneAuction;
import hgds.stoneauction.models.AuctionLot;
import hgds.stoneauction.integrations.IntegrationManager;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CaseAuctionCategory extends AuctionCategory {
    
    private final StoneAuction plugin;
    
    public CaseAuctionCategory(StoneAuction plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public AuctionLot createLot(Player seller, Object item, double price, int amount) {
        if (!(item instanceof String)) return null;
        String caseId = (String) item;
        if (plugin.getIntegrationManager().getStoneCasesAPI() == null) return null;
        if (!plugin.getIntegrationManager().getStoneCasesAPI().takeKeys(seller, caseId, amount)) return null;
        return new AuctionLot(UUID.randomUUID().toString(), seller.getUniqueId(), "case", caseId, price, amount);
    }
    
    @Override
    public boolean buyLot(Player buyer, AuctionLot lot, int amount) {
        if (!(lot.getItem() instanceof String)) return false;
        int take = Math.min(amount, lot.getAmount());
        if (take <= 0) return false;
        IntegrationManager intm = plugin.getIntegrationManager();
        if (intm.getStoneCasesAPI() == null) return false;
        double total = lot.getPrice() * take;
        if (!intm.hasEnough(buyer, total)) return false;
        intm.withdraw(buyer, total);
        intm.getStoneCasesAPI().giveCase(buyer, (String) lot.getItem(), take);
        org.bukkit.OfflinePlayer seller = org.bukkit.Bukkit.getOfflinePlayer(lot.getSellerId());
        if (seller.getPlayer() != null && seller.getPlayer().isOnline()) {
            intm.deposit(seller.getPlayer(), total);
        }
        lot.setAmount(lot.getAmount() - take);
        return true;
    }
    
    @Override
    public String getName() {
        return "Кейсовый аукцион";
    }
}
