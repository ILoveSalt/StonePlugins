package hgds.stoneoplata;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Команды: /stoneoplata reload, /stoneoplata testpay <игрок> <тип> [product_id|count] [period|amount]
 */
public class StoneOplataCommand implements CommandExecutor, TabCompleter {

    private final StoneOplata plugin;

    public StoneOplataCommand(StoneOplata plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(StoneOplata.color("&e/stoneoplata reload &7— перезагрузить конфиг и вебхук"));
            sender.sendMessage(StoneOplata.color("&e/stoneoplata testpay <игрок> <privilege|donate_currency|case> [id/amount/count] [period] &7— тестовая выдача"));
            return true;
        }

        if ("reload".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("stoneoplata.admin")) {
                sender.sendMessage(StoneOplata.color("&cНет прав."));
                return true;
            }
            plugin.getOplataConfig().reload();
            plugin.getWebhookServer().stop();
            plugin.getWebhookServer().start();
            sender.sendMessage(StoneOplata.color("&a[StoneOplata] Конфиг и вебхук перезагружены."));
            return true;
        }

        if ("testpay".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("stoneoplata.testpay")) {
                sender.sendMessage(StoneOplata.color("&cНет прав. Нужно: stoneoplata.testpay"));
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage(StoneOplata.color("&cИспользование: /stoneoplata testpay <игрок> <privilege|donate_currency|case> [product_id или amount или count] [period для privilege]"));
                return true;
            }
            String playerName = args[1];
            String productType = args[2].toLowerCase();
            WebhookPayload payload = new WebhookPayload();
            payload.setPaymentId("test-" + System.currentTimeMillis());
            payload.setPlayer(playerName);
            payload.setProductType(productType);
            payload.setTest(true);

            switch (productType) {
                case "privilege":
                    payload.setProductId(args.length > 3 ? args[3] : "hero");
                    payload.setPeriod(args.length > 4 ? args[4] : "1");
                    break;
                case "donate_currency":
                    payload.setAmount(args.length > 3 ? parseDouble(args[3], 100.0) : 100.0);
                    break;
                case "case":
                    payload.setCount(args.length > 3 ? parseInt(args[3], 1) : 1);
                    payload.setCaseType(args.length > 4 ? args[4] : "donate");
                    break;
                default:
                    sender.sendMessage(StoneOplata.color("&cТип должен быть: privilege, donate_currency, case"));
                    return true;
            }

            plugin.getDeliveryService().executeDelivery(payload);
            sender.sendMessage(StoneOplata.color(plugin.getOplataConfig().msg("testpay-done")
                    .replace("%player%", playerName)
                    .replace("%product_type%", productType)));
            return true;
        }

        return false;
    }

    private static int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static double parseDouble(String s, double def) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("stoneoplata.admin") && !sender.hasPermission("stoneoplata.testpay")) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            return filter(Arrays.asList("reload", "testpay"), args[0]);
        }
        if (args.length == 2 && "testpay".equalsIgnoreCase(args[0])) {
            List<String> names = new ArrayList<>();
            for (Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                names.add(p.getName());
            }
            return filter(names, args[1]);
        }
        if (args.length == 3 && "testpay".equalsIgnoreCase(args[0])) {
            return filter(Arrays.asList("privilege", "donate_currency", "case"), args[2]);
        }
        if (args.length == 4 && "testpay".equalsIgnoreCase(args[0])) {
            if ("privilege".equalsIgnoreCase(args[2])) {
                return filter(Arrays.asList("hero", "prince", "baron", "king", "imperator", "lord", "overlord", "knyaz", "custom"), args[3]);
            }
            if ("case".equalsIgnoreCase(args[2])) {
                return filter(Arrays.asList("1", "3", "5", "10", "25"), args[3]);
            }
        }
        if (args.length == 5 && "testpay".equalsIgnoreCase(args[0]) && "privilege".equalsIgnoreCase(args[2])) {
            return filter(Arrays.asList("1", "3", "forever"), args[4]);
        }
        if (args.length == 5 && "testpay".equalsIgnoreCase(args[0]) && "case".equalsIgnoreCase(args[2])) {
            return filter(Arrays.asList("donate", "coins", "donate_currency"), args[4]);
        }
        return Collections.emptyList();
    }

    private static List<String> filter(List<String> list, String prefix) {
        if (prefix == null || prefix.isEmpty()) return list;
        String lower = prefix.toLowerCase();
        List<String> out = new ArrayList<>();
        for (String s : list) {
            if (s != null && s.toLowerCase().startsWith(lower)) out.add(s);
        }
        return out;
    }
}
