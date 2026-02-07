package hgds.stoneauction.gui;

import hgds.stoneauction.models.AuctionLot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class AuctionHolder implements InventoryHolder {

    private final String type;
    private final String category;
    private final int page;
    private final String lotId;
    private final AuctionLot lot;

    public AuctionHolder(String type, String category, int page, String lotId, AuctionLot lot) {
        this.type = type;
        this.category = category;
        this.page = page;
        this.lotId = lotId;
        this.lot = lot;
    }

    public static AuctionHolder category() {
        return new AuctionHolder("category", null, 0, null, null);
    }

    public static AuctionHolder list(String category, int page) {
        return new AuctionHolder("list", category, page, null, null);
    }

    public static AuctionHolder view(String category, AuctionLot lot) {
        return new AuctionHolder("view", category, 0, lot != null ? lot.getId() : null, lot);
    }

    public static AuctionHolder expired(String category, int page) {
        return new AuctionHolder("expired", category, page, null, null);
    }

    public String getType() { return type; }
    public String getCategory() { return category; }
    public int getPage() { return page; }
    public String getLotId() { return lotId; }
    public AuctionLot getLot() { return lot; }

    @Override
    public Inventory getInventory() { return null; }
}
