package hgds.stoneauction.categories;

import hgds.stoneauction.StoneAuction;
import hgds.stoneauction.models.AuctionLot;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class NormalAuctionCategory extends AuctionCategory {
    
    private final StoneAuction plugin;
    
    public NormalAuctionCategory(StoneAuction plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public AuctionLot createLot(Player seller, Object item, double price, int amount) {
        if (!(item instanceof ItemStack)) return null;
        ItemStack itemStack = ((ItemStack) item).clone();
        int amt = Math.min(amount, itemStack.getAmount());
        if (amt <= 0) return null;
        ItemStack toStore = itemStack.clone();
        toStore.setAmount(amt);
        java.util.HashMap<Integer, ItemStack> left = seller.getInventory().removeItem(toStore);
        if (!left.isEmpty()) {
            for (ItemStack giveBack : left.values()) seller.getInventory().addItem(giveBack);
            return null;
        }
        return new AuctionLot(java.util.UUID.randomUUID().toString(), seller.getUniqueId(), "normal", toStore, price, amt);
    }
    
    @Override
    public boolean buyLot(Player buyer, AuctionLot lot, int amount) {
        if (!(lot.getItem() instanceof ItemStack)) return false;
        int take = Math.min(amount, lot.getAmount());
        if (take <= 0) return false;
        double total = lot.getPrice() * take;
        if (!plugin.getIntegrationManager().hasEnough(buyer, total)) return false;
        plugin.getIntegrationManager().withdraw(buyer, total);
        ItemStack toGive = ((ItemStack) lot.getItem()).clone();
        toGive.setAmount(take);
        buyer.getInventory().addItem(toGive);
        org.bukkit.OfflinePlayer seller = org.bukkit.Bukkit.getOfflinePlayer(lot.getSellerId());
        if (seller.getPlayer() != null && seller.getPlayer().isOnline()) {
            plugin.getIntegrationManager().deposit(seller.getPlayer(), total);
        }
        lot.setAmount(lot.getAmount() - take);
        return true;
    }
    
    @Override
    public String getName() {
        return "Обычный аукцион";
    }
}
