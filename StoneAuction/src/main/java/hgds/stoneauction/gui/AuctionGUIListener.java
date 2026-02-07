package hgds.stoneauction.gui;

import hgds.stoneauction.StoneAuction;
import hgds.stoneauction.models.AuctionLot;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class AuctionGUIListener implements Listener {

    private final StoneAuction plugin;

    public AuctionGUIListener(StoneAuction plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof AuctionHolder)) return;
        e.setCancelled(true);
        AuctionHolder h = (AuctionHolder) e.getInventory().getHolder();
        Player p = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();
        if (slot >= e.getInventory().getSize()) return;

        if ("category".equals(h.getType())) {
            if (slot == 11) plugin.getAuctionManager().openListGUI(p, "normal", 0);
            else if (slot == 13) plugin.getAuctionManager().openListGUI(p, "donate", 0);
            else if (slot == 15) plugin.getAuctionManager().openListGUI(p, "case", 0);
            else if (slot == 31) plugin.getAuctionManager().openExpiredGUI(p, "normal");
        } else if ("list".equals(h.getType())) {
            if (slot == 45) plugin.getAuctionManager().openCategorySelector(p);
            else if (slot >= 0 && slot < 45) {
                AuctionLot lot = plugin.getAuctionManager().getLotAtSlot(h.getCategory(), h.getPage(), slot);
                if (lot != null) plugin.getAuctionManager().openViewGUI(p, lot, h.getCategory());
            } else if (slot == 49 && h.getPage() > 0) {
                plugin.getAuctionManager().openListGUI(p, h.getCategory(), h.getPage() - 1);
            } else if (slot == 53) {
                plugin.getAuctionManager().openListGUI(p, h.getCategory(), h.getPage() + 1);
            }
        } else if ("view".equals(h.getType()) && h.getLot() != null) {
            if (slot == 45) plugin.getAuctionManager().openListGUI(p, h.getCategory(), 0);
            else if (slot == 19) {
                if (plugin.getAuctionManager().buyLot(p, h.getCategory(), h.getLotId(), 1)) {
                    p.sendMessage("§aКуплено 1 шт.");
                    if (h.getLot().getAmount() <= 0) plugin.getAuctionManager().openListGUI(p, h.getCategory(), 0);
                    else plugin.getAuctionManager().openViewGUI(p, plugin.getAuctionManager().findLot(h.getCategory(), h.getLotId()), h.getCategory());
                } else p.sendMessage("§cНедостаточно средств или лот продан.");
            } else if (slot == 23) {
                int amt = h.getLot().getAmount();
                if (plugin.getAuctionManager().buyLot(p, h.getCategory(), h.getLotId(), amt)) {
                    p.sendMessage("§aКуплено всё.");
                    plugin.getAuctionManager().openListGUI(p, h.getCategory(), 0);
                } else p.sendMessage("§cНедостаточно средств.");
            }
        } else if ("expired".equals(h.getType())) {
            if (slot == 45) plugin.getAuctionManager().openCategorySelector(p);
            else if (slot >= 0 && slot < 45) {
                AuctionLot lot = plugin.getAuctionManager().getExpiredLotAtSlot(h.getCategory(), h.getPage(), slot);
                if (lot != null && lot.getSellerId().equals(p.getUniqueId())) {
                    plugin.getAuctionManager().returnExpiredToPlayer(p, h.getCategory(), lot);
                    p.sendMessage("§aПредмет возвращён.");
                    plugin.getAuctionManager().openExpiredGUI(p, h.getCategory());
                }
            }
        }
    }
}
