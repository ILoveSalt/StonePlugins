package hgds.stoneoplata;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StoneOplata extends JavaPlugin {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private OplataConfig oplataConfig;
    private DeliveryService deliveryService;
    private WebhookServer webhookServer;
    private DonateCheck donateCheck;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        oplataConfig = new OplataConfig(this);
        deliveryService = new DeliveryService(this);
        donateCheck = new DonateCheck(this);
        webhookServer = new WebhookServer(this);
        webhookServer.start();

        StoneOplataCommand cmd = new StoneOplataCommand(this);
        getCommand("stoneoplata").setExecutor(cmd);
        getCommand("stoneoplata").setTabCompleter(cmd);
    }

    @Override
    public void onDisable() {
        if (webhookServer != null) {
            webhookServer.stop();
        }
    }

    public OplataConfig getOplataConfig() {
        return oplataConfig;
    }

    public DeliveryService getDeliveryService() {
        return deliveryService;
    }

    public WebhookServer getWebhookServer() {
        return webhookServer;
    }

    public DonateCheck getDonateCheck() {
        return donateCheck;
    }

    public static String color(String s) {
        if (s == null) return "";
        Matcher matcher = HEX_PATTERN.matcher(s);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("&x");
            for (char c : hex.toCharArray()) {
                replacement.append("&").append(c);
            }
            matcher.appendReplacement(buffer, replacement.toString());
        }
        matcher.appendTail(buffer);
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }
}
