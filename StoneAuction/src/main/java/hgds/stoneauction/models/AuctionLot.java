package hgds.stoneauction.models;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class AuctionLot {

    private final String id;
    private final UUID sellerId;
    private final String category;
    private final Object item; 
    private final double price;
    private final long createdAt;
    private int amount;

    public AuctionLot(String id, UUID sellerId, String category, Object item, double price, int amount) {
        this(id, sellerId, category, item, price, amount, System.currentTimeMillis());
    }

    public AuctionLot(String id, UUID sellerId, String category, Object item, double price, int amount, long createdAt) {
        this.id = id;
        this.sellerId = sellerId;
        this.category = category;
        this.item = item;
        this.price = price;
        this.amount = amount;
        this.createdAt = createdAt;
    }
    
    public String getId() {
        return id;
    }
    
    public UUID getSellerId() {
        return sellerId;
    }
    
    public String getCategory() {
        return category;
    }
    
    public Object getItem() {
        return item;
    }
    
    public double getPrice() {
        return price;
    }
    
    public int getAmount() {
        return amount;
    }
    
    public void setAmount(int amount) {
        this.amount = amount;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
}
