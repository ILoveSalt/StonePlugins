package hgds.stoneoplata;

import org.bukkit.configuration.file.FileConfiguration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class OplataConfig {

    private final StoneOplata plugin;
    private boolean debug;
    private String secretKey;
    private int webhookPort;
    private String webhookPath;
    private boolean testMode;
    private int deliveryDelayTicks;
    private String privilegeCommand;
    private String privilegeForeverCommand;
    private Map<String, String> privilegePeriods;
    private String donateCurrencyCommand;
    private String caseDonateCommand;
    private String caseCoinsCommand;
    private String caseDonateCurrencyCommand;
    private String customBanCommand;

    public OplataConfig(StoneOplata plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        FileConfiguration c = plugin.getConfig();

        debug = c.getBoolean("debug", false);
        secretKey = c.getString("secret-key", "change-me");
        webhookPort = c.getInt("webhook.port", 8765);
        webhookPath = c.getString("webhook.path", "/stoneoplata");
        testMode = c.getBoolean("test-mode", true);
        deliveryDelayTicks = c.getInt("delivery-delay-ticks", 20);

        privilegeCommand = c.getString("delivery.privilege-command", "lp user %player% parent add temp %product_id% %period%");
        privilegeForeverCommand = c.getString("delivery.privilege-forever-command", "lp user %player% parent add %product_id%");
        privilegePeriods = new HashMap<>();
        if (c.contains("delivery.privilege-periods")) {
            for (String k : c.getConfigurationSection("delivery.privilege-periods").getKeys(false)) {
                privilegePeriods.put(k, c.getString("delivery.privilege-periods." + k));
            }
        }
        if (privilegePeriods.isEmpty()) {
            privilegePeriods.put("1", "30d");
            privilegePeriods.put("3", "90d");
            privilegePeriods.put("forever", "forever");
        }

        donateCurrencyCommand = c.getString("delivery.donate-currency-command", "eco give %player% %amount%");
        caseDonateCommand = c.getString("delivery.case-donate-command", "givecase donate %player% %count%");
        caseCoinsCommand = c.getString("delivery.case-coins-command", "givecase coins %player% %count%");
        caseDonateCurrencyCommand = c.getString("delivery.case-donate-currency-command", "givecase donate_currency %player% %count%");
        customBanCommand = c.getString("custom.ban-command", "ban %player% Куплена кастомная привилегия. Обратитесь в тех. поддержку.");
    }

    public boolean isDebug() {
        return debug;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public int getWebhookPort() {
        return webhookPort;
    }

    public String getWebhookPath() {
        return webhookPath;
    }

    public boolean isTestMode() {
        return testMode;
    }

    public int getDeliveryDelayTicks() {
        return deliveryDelayTicks;
    }

    public String getPrivilegeCommand() {
        return privilegeCommand;
    }

    public String getPrivilegeForeverCommand() {
        return privilegeForeverCommand;
    }

    public String getPrivilegePeriod(String period) {
        return privilegePeriods.getOrDefault(period != null ? period : "1", "30d");
    }

    public String getDonateCurrencyCommand() {
        return donateCurrencyCommand;
    }

    public String getCaseDonateCommand() {
        return caseDonateCommand;
    }

    public String getCaseCoinsCommand() {
        return caseCoinsCommand;
    }

    public String getCaseDonateCurrencyCommand() {
        return caseDonateCurrencyCommand;
    }

    public String getCustomBanCommand() {
        return customBanCommand;
    }

    public String msg(String key) {
        String s = plugin.getConfig().getString("messages." + key, "&7" + key);
        return s != null ? s : "";
    }

    /**
     * Проверка подписи HMAC-SHA256: signature = hex(HMAC-SHA256(secret_key, rawBody))
     */
    public boolean verifySignature(String rawBody, String signatureHeader) {
        if (rawBody == null || signatureHeader == null || secretKey == null || secretKey.isEmpty()) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString().equalsIgnoreCase(signatureHeader.trim());
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            return false;
        }
    }
}
