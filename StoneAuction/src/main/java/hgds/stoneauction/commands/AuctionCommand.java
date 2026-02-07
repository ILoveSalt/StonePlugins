package hgds.stoneauction.commands;

import hgds.stoneauction.StoneAuction;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AuctionCommand implements CommandExecutor {

    private final StoneAuction plugin;

    public AuctionCommand(StoneAuction plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cТолько для игроков!");
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("stoneauction.use")) {
            player.sendMessage("§cНет прав!");
            return true;
        }
        String category = getCategoryFromCommand(label);

        if (args.length == 0) {
            plugin.getAuctionManager().openCategorySelector(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("sell")) {
            if (category.equals("normal")) {
                if (args.length < 2) {
                    player.sendMessage("§cИспользуйте: /ah sell <цена>");
                    return true;
                }
                ItemStack hand = player.getInventory().getItemInMainHand();
                if (hand.getType() == Material.AIR) {
                    player.sendMessage("§cВозьмите предмет в руку!");
                    return true;
                }
                try {
                    double price = Double.parseDouble(args[1]);
                    if (price <= 0) throw new NumberFormatException("");
                    if (plugin.getAuctionManager().createLot(player, "normal", hand, price, 1)) {
                        player.sendMessage("§aЛот выставлен!");
                    } else {
                        player.sendMessage("§cНе удалось выставить лот.");
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage("§cУкажите корректную цену.");
                }
            } else if (category.equals("case")) {
                if (args.length < 3) {
                    player.sendMessage("§cИспользуйте: /cah sell <ид_кейса> <цена>");
                    return true;
                }
                String caseId = args[1];
                try {
                    double price = Double.parseDouble(args[2]);
                    if (price <= 0) throw new NumberFormatException("");
                    if (plugin.getAuctionManager().createLot(player, "case", caseId, price, 1)) {
                        player.sendMessage("§aЛот выставлен!");
                    } else {
                        player.sendMessage("§cНедостаточно ключей или кейс не найден.");
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage("§cУкажите корректную цену.");
                }
            } else if (category.equals("donate")) {
                if (args.length < 3) {
                    player.sendMessage("§cИспользуйте: /dah sell <ранг> <цена>");
                    return true;
                }
                String rank = args[1];
                try {
                    double price = Double.parseDouble(args[2]);
                    if (price <= 0) throw new NumberFormatException("");
                    if (plugin.getAuctionManager().createLot(player, "donate", rank, price, 1)) {
                        player.sendMessage("§aЛот выставлен!");
                    } else {
                        player.sendMessage("§cОшибка.");
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage("§cУкажите корректную цену.");
                }
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("expired")) {
            plugin.getAuctionManager().openExpiredGUI(player, category);
            return true;
        }

        return false;
    }

    private String getCategoryFromCommand(String command) {
        if (command.equalsIgnoreCase("ah")) return "normal";
        if (command.equalsIgnoreCase("dah")) return "donate";
        if (command.equalsIgnoreCase("cah")) return "case";
        return "normal";
    }
}
