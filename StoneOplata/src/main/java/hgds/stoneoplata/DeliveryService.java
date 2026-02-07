package hgds.stoneoplata;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Выполняет выдачу покупки: подставляет плейсхолдеры и выполняет команды от консоли.
 */
public class DeliveryService {

    private final StoneOplata plugin;
    private final OplataConfig config;

    public DeliveryService(StoneOplata plugin) {
        this.plugin = plugin;
        this.config = plugin.getOplataConfig();
    }

    /**
     * Запланировать выдачу через N тиков (чтобы выполнить в главном потоке).
     */
    public void scheduleDelivery(WebhookPayload payload) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> executeDelivery(payload), config.getDeliveryDelayTicks());
    }

    /**
     * Выполнить выдачу сразу (для тестовой оплаты).
     */
    public void executeDelivery(WebhookPayload payload) {
        String playerName = payload.getPlayer();
        if (playerName == null || playerName.isEmpty()) {
            plugin.getLogger().warning("[StoneOplata] Пустой player в вебхуке, payment_id=" + payload.getPaymentId());
            return;
        }

        String productType = payload.getProductType();
        if (productType == null) productType = "";

        switch (productType.toLowerCase()) {
            case "privilege":
                String period = payload.getPeriod() != null ? payload.getPeriod() : "1";
                if ("forever".equalsIgnoreCase(period)) {
                    runCommand(replace(config.getPrivilegeForeverCommand(), payload, null, null, null));
                } else {
                    runCommand(replace(config.getPrivilegeCommand(), payload,
                            config.getPrivilegePeriod(period)));
                }
                break;
            case "donate_currency":
                String amount = payload.getAmount() != null ? String.valueOf(payload.getAmount().intValue()) : "0";
                runCommand(replace(config.getDonateCurrencyCommand(), payload, amount, null, null));
                break;
            case "case":
                String count = payload.getCount() != null ? String.valueOf(payload.getCount()) : "1";
                String caseType = payload.getCaseType() != null ? payload.getCaseType() : "donate";
                String cmd;
                switch (caseType.toLowerCase()) {
                    case "coins":
                        cmd = config.getCaseCoinsCommand();
                        break;
                    case "donate_currency":
                        cmd = config.getCaseDonateCurrencyCommand();
                        break;
                    default:
                        cmd = config.getCaseDonateCommand();
                        break;
                }
                runCommand(replace(cmd, payload, null, count, caseType));
                break;
            default:
                plugin.getLogger().warning("[StoneOplata] Неизвестный product_type: " + productType + ", payment_id=" + payload.getPaymentId());
                return;
        }

        plugin.getLogger().info("[StoneOplata] Выдача выполнена: " + playerName + " — " + productType + (payload.getTest() != null && payload.getTest() ? " (тест)" : ""));

        Player p = Bukkit.getPlayerExact(playerName);
        if (p != null && p.isOnline()) {
            String msg = config.msg("delivery-success")
                    .replace("%product%", productType + (payload.getProductId() != null ? " " + payload.getProductId() : ""))
                    .replace("%payment_id%", payload.getPaymentId() != null ? payload.getPaymentId() : "");
            p.sendMessage(StoneOplata.color(msg));
        }
    }

    private String replace(String template, WebhookPayload payload, String periodValue) {
        return replace(template, payload, null, null, periodValue);
    }

    private String replace(String template, WebhookPayload payload, String amountOrNull, String countOrNull, String caseTypeOrPeriod) {
        String player = payload.getPlayer() != null ? payload.getPlayer() : "";
        String productId = payload.getProductId() != null ? payload.getProductId() : "";
        String period = payload.getPeriod() != null ? payload.getPeriod() : "1";
        String amount = amountOrNull != null ? amountOrNull : (payload.getAmount() != null ? String.valueOf(payload.getAmount().intValue()) : "0");
        String count = countOrNull != null ? countOrNull : (payload.getCount() != null ? String.valueOf(payload.getCount()) : "1");
        String caseType = payload.getCaseType() != null ? payload.getCaseType() : "donate";
        String paymentId = payload.getPaymentId() != null ? payload.getPaymentId() : "";

        String out = template
                .replace("%player%", player)
                .replace("%product_id%", productId)
                .replace("%payment_id%", paymentId)
                .replace("%count%", count)
                .replace("%case_type%", caseType);
        if (caseTypeOrPeriod != null) {
            out = out.replace("%period%", caseTypeOrPeriod);
        } else {
            out = out.replace("%period%", period);
        }
        return out.replace("%amount%", amount);
    }

    private void runCommand(String command) {
        if (command == null || command.isEmpty()) return;
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }
}
