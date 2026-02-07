package hgds.stoneauction.categories;

import hgds.stoneauction.models.AuctionLot;
import org.bukkit.entity.Player;

public abstract class AuctionCategory {

    public abstract AuctionLot createLot(Player seller, Object item, double price, int amount);

    public abstract boolean buyLot(Player buyer, AuctionLot lot, int amount);

    public abstract String getName();
}
