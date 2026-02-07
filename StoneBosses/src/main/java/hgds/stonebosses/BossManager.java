package hgds.stonebosses;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.stream.Collectors;

public class BossManager {
    private final StoneBosses plugin;
    private final ConfigManager configManager;
    private LivingEntity currentBoss;
    private BossConfig currentBossConfig;
    private BossBar bossBar;
    private long nextSpawnTime;
    private BukkitTask spawnTask;
    private BukkitTask damageMessageTask;
    private final Map<UUID, Double> damageByPlayer = new HashMap<>();

    public BossManager(StoneBosses plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.nextSpawnTime = System.currentTimeMillis() + configManager.getBossesSpawnDelay() * 1000L;
        startSpawnTimer();
        startDamageMessageTimer();
    }

    public void addDamage(UUID playerId, double damage) {
        damageByPlayer.merge(playerId, damage, Double::sum);
    }

    public Map<UUID, Double> getDamageByPlayer() {
        return new HashMap<>(damageByPlayer);
    }

    public List<Map.Entry<UUID, Double>> getTopDamagers(int n) {
        return damageByPlayer.entrySet().stream()
            .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
            .limit(n)
            .collect(Collectors.toList());
    }

    public void startSpawnTimer() {
        cancelSpawnTimer();
        int delaySec = configManager.getBossesSpawnDelay();
        spawnTask = Bukkit.getScheduler().runTaskLater(plugin, this::spawnBoss, delaySec * 20L);
        nextSpawnTime = System.currentTimeMillis() + delaySec * 1000L;
    }

    public void cancelSpawnTimer() {
        if (spawnTask != null) {
            spawnTask.cancel();
            spawnTask = null;
        }
    }

    private void startDamageMessageTimer() {
        if (damageMessageTask != null) damageMessageTask.cancel();
        int interval = configManager.getDamageMessageInterval() * 20;
        damageMessageTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (currentBoss != null && currentBoss.isValid()) {
                plugin.broadcastDamageMessage();
            }
        }, interval, interval);
    }

    public void spawnBoss() {
        if (currentBoss != null && currentBoss.isValid()) return;
        String activeId = configManager.getActiveBossId();
        if (activeId == null) {
            List<String> ids = configManager.getBossIds();
            if (ids.isEmpty()) {
                plugin.getLogger().warning("Нет конфигов боссов в папке bosses/");
                rescheduleSpawn();
                return;
            }
            activeId = ids.get(0);
            configManager.setActiveBossId(activeId);
        }
        BossConfig cfg = configManager.getBossConfig(activeId);
        if (cfg == null) {
            rescheduleSpawn();
            return;
        }
        currentBossConfig = cfg;
        damageByPlayer.clear();
        currentBoss = cfg.spawnBoss(configManager.getSpawnLocation());
        if (currentBoss == null) {
            rescheduleSpawn();
            return;
        }
        plugin.getBossListener().registerBoss(currentBoss);
        if (bossBar != null) {
            bossBar.removeAll();
        }
        bossBar = Bukkit.createBossBar(cfg.getName(), BarColor.RED, BarStyle.SOLID);
        for (Player p : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(p);
        }
        for (String line : configManager.getSpawnBossesMessages()) {
            String msg = line.replace("%name_boss%", cfg.getName());
            Bukkit.broadcastMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', msg));
        }
        nextSpawnTime = System.currentTimeMillis() + configManager.getBossesSpawnDelay() * 1000L;
        cancelSpawnTimer();
    }

    public void killBoss() {
        if (currentBoss != null && currentBoss.isValid()) {
            currentBoss.remove();
        }
        onBossRemoved(false);
    }

    public void onBossRemoved(boolean wasKilled) {
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }
        currentBoss = null;
        if (currentBossConfig != null && wasKilled) {
            plugin.rewardAndBroadcastKill();
        } else if (currentBossConfig != null && !wasKilled) {
            for (String line : configManager.getNoKillBossesMessages()) {
                Bukkit.broadcastMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', line));
            }
        }
        currentBossConfig = null;
        damageByPlayer.clear();
        rescheduleSpawn();
    }

    public void tickBossBar() {
        if (bossBar != null && currentBoss != null && currentBoss.isValid()) {
            double health = currentBoss.getHealth();
            double maxHealth = currentBoss.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
            bossBar.setProgress(Math.max(0, health / maxHealth));
            if (currentBossConfig != null && currentBossConfig.shouldSpawnChildren(health)) {
                currentBossConfig.spawnChildren(currentBoss.getLocation());
            }
        }
    }

    private void rescheduleSpawn() {
        startSpawnTimer();
    }

    public long getNextSpawnTime() {
        return nextSpawnTime;
    }

    public int getSecondsUntilSpawn() {
        long diff = nextSpawnTime - System.currentTimeMillis();
        return (int) Math.max(0, diff / 1000);
    }

    public LivingEntity getCurrentBoss() {
        return currentBoss;
    }

    public BossConfig getCurrentBossConfig() {
        return currentBossConfig;
    }

    public BossBar getBossBar() {
        return bossBar;
    }

    public void addBossBarPlayer(Player p) {
        if (bossBar != null) bossBar.addPlayer(p);
    }

    public void removeBossBarPlayer(Player p) {
        if (bossBar != null) bossBar.removePlayer(p);
    }

    public void shutdown() {
        cancelSpawnTimer();
        if (damageMessageTask != null) {
            damageMessageTask.cancel();
        }
        if (currentBoss != null && currentBoss.isValid()) {
            currentBoss.remove();
        }
        if (bossBar != null) bossBar.removeAll();
    }
}
