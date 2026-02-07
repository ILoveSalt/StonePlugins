package hgds.stoneauction.categories;

import hgds.stoneauction.models.AuctionLot;
import org.bukkit.entity.Player;

public abstract class AuctionCategory {

    /** Создать лот. amount — количество (для стака/кейсов). */
    public abstract AuctionLot createLot(Player seller, Object item, double price, int amount);

    /** Купить amount единиц. Возвращает true при успехе (деньги списаны, товар выдан). */
    public abstract boolean buyLot(Player buyer, AuctionLot lot, int amount);

    public abstract String getName();
}
