package hgds.stonebosses;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;

public final class StoneBosses extends JavaPlugin {

    private ConfigManager configManager;
    private BossManager bossManager;
    private GuiManager guiManager;
    private BossListener bossListener;
    private GuiListener guiListener;
    private Object playerPointsApi;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        bossManager = new BossManager(this);
        guiManager = new GuiManager(this);
        bossListener = new BossListener(this);
        guiListener = new GuiListener(this);

        getCommand("bosses").setExecutor(new BossesCommand(this));
        getCommand("bosses").setTabCompleter((org.bukkit.command.TabCompleter) getCommand("bosses").getExecutor());
        getServer().getPluginManager().registerEvents(bossListener, this);
        getServer().getPluginManager().registerEvents(guiListener, this);

        hookPlayerPoints();

        Bukkit.getScheduler().runTaskTimer(this, () -> bossManager.tickBossBar(), 20L, 5L);

        getLogger().info("StoneBosses включен.");
    }

    @Override
    public void onDisable() {
        if (bossManager != null) bossManager.shutdown();
        getLogger().info("StoneBosses выключен.");
    }

    private void hookPlayerPoints() {
        try {
            org.bukkit.plugin.Plugin pp = getServer().getPluginManager().getPlugin("PlayerPoints");
            if (pp != null) {
                Class<?> apiClass = Class.forName("org.black_ixx.playerpoints.PlayerPoints");
                Object pluginInstance = apiClass.cast(pp);
                playerPointsApi = pluginInstance.getClass().getMethod("getAPI").invoke(pluginInstance);
                getLogger().info("PlayerPoints подключен.");
            }
        } catch (Throwable ignored) {
        }
    }

    private void givePlayerPoints(java.util.UUID playerId, int points) {
        if (playerPointsApi == null) return;
        try {
            Class<?> apiClass = playerPointsApi.getClass();
            java.lang.reflect.Method giveByUuid = null;
            try {
                giveByUuid = apiClass.getMethod("give", java.util.UUID.class, int.class);
            } catch (NoSuchMethodException ignored) {
            }
            if (giveByUuid != null) {
                giveByUuid.invoke(playerPointsApi, playerId, points);
                return;
            }
            java.lang.reflect.Method giveByString = apiClass.getMethod("give", String.class, int.class);
            String name = Bukkit.getOfflinePlayer(playerId).getName();
            if (name != null) {
                giveByString.invoke(playerPointsApi, name, points);
            }
        } catch (Throwable t) {
            getLogger().warning("PlayerPoints give: " + t.getMessage());
        }
    }

    public void broadcastDamageMessage() {
        List<Map.Entry<java.util.UUID, Double>> top = bossManager.getTopDamagers(3);
        List<String> lines = configManager.getDamageBossMessages();
        for (String line : lines) {
            String msg = line;
            for (int i = 1; i <= 3; i++) {
                String name = "";
                String damage = "0";
                if (i <= top.size()) {
                    Map.Entry<java.util.UUID, Double> e = top.get(i - 1);
                    Player p = Bukkit.getPlayer(e.getKey());
                    name = p != null ? p.getDisplayName() : Bukkit.getOfflinePlayer(e.getKey()).getName();
                    if (name == null) name = "?";
                    damage = String.format("%.1f", e.getValue());
                }
                msg = msg.replace("%damage_" + i + "_player_name%", name)
                    .replace("%damage_" + i + "_player_damage%", damage);
            }
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', msg));
        }
    }

    public void rewardAndBroadcastKill() {
        List<Map.Entry<java.util.UUID, Double>> top = bossManager.getTopDamagers(3);
        List<String> lines = configManager.getKillBossesMessages();
        int coinsPerDamage = configManager.getCoinsPerDamage();

        for (String line : lines) {
            String msg = line;
            for (int i = 1; i <= 3; i++) {
                String name = "";
                String damage = "0";
                if (i <= top.size()) {
                    Map.Entry<java.util.UUID, Double> e = top.get(i - 1);
                    Player p = Bukkit.getPlayer(e.getKey());
                    name = p != null ? p.getDisplayName() : Bukkit.getOfflinePlayer(e.getKey()).getName();
                    if (name == null) name = "?";
                    damage = String.format("%.1f", e.getValue());
                }
                msg = msg.replace("%damage_" + i + "_player_name%", name)
                    .replace("%damage_" + i + "_player_damage%", damage);
            }
            int amountPoints = 0;
            if (!top.isEmpty()) {
                double dmg = top.get(0).getValue();
                amountPoints = (int) (dmg * coinsPerDamage);
            }
            msg = msg.replace("%amount_damage_point%", String.valueOf(amountPoints));
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', msg));
        }

        if (!top.isEmpty() && playerPointsApi != null) {
            Map.Entry<java.util.UUID, Double> first = top.get(0);
            double dmg = first.getValue();
            int points = (int) (dmg * coinsPerDamage);
            if (points > 0) {
                givePlayerPoints(first.getKey(), points);
            }
        }
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public BossManager getBossManager() {
        return bossManager;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }

    public BossListener getBossListener() {
        return bossListener;
    }
}
