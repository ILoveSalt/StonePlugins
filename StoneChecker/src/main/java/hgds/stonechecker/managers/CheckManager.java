package hgds.stonechecker.managers;

import hgds.stonechecker.StoneChecker;
import hgds.stonechecker.models.CheckSession;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CheckManager {
    
    private final StoneChecker plugin;
    private final Map<UUID, CheckSession> byTarget = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastActivity = new ConcurrentHashMap<>();
    private static final long AFK_THRESHOLD_MS = 5 * 60 * 1000;
    private static final long CHECK_TIME_LIMIT_MS = 10 * 60 * 1000;
    
    public CheckManager(StoneChecker plugin) {
        this.plugin = plugin;
    }
    
    public void recordActivity(Player player) {
        lastActivity.put(player.getUniqueId(), System.currentTimeMillis());
    }
    
    public boolean isAfk(Player player) {
        if (!plugin.getConfig().getBoolean("afk.enabled", true)) return false;
        Long last = lastActivity.get(player.getUniqueId());
        if (last == null) return false;
        return (System.currentTimeMillis() - last) > AFK_THRESHOLD_MS;
    }
    
    public CheckSession getByTarget(UUID targetId) { return byTarget.get(targetId); }
    public CheckSession getByModerator(UUID modId) {
        for (CheckSession s : byTarget.values()) {
            if (s.getModeratorId().equals(modId)) return s;
        }
        return null;
    }
    
    public CheckSession startCheck(Player moderator, Player target) {
        if (byTarget.containsKey(target.getUniqueId())) return null;
        if (plugin.getConfig().getBoolean("afk.block_check", true) && isAfk(target)) return null;
        
        CheckSession session = new CheckSession(target, moderator);
        byTarget.put(target.getUniqueId(), session);
        if (plugin.getConfig().getBoolean("settings.freeze_on_check", true)) {
            session.setFreeze(true);
        }
        return session;
    }
    
    public void endSession(CheckSession session, boolean unfreeze) {
        if (session == null) return;
        byTarget.remove(session.getTargetId());
        if (session.getTimer() != null) session.getTimer().cancel();
        if (unfreeze) {
            Player t = Bukkit.getPlayer(session.getTargetId());
            if (t != null && t.isOnline()) session.setFreeze(false);
        }
    }
    
    public void runTimer(CheckSession session, Runnable onExpire) {
        if (session.getTimer() != null) session.getTimer().cancel();
        long limitMs = CHECK_TIME_LIMIT_MS;
        String cfg = plugin.getConfig().getString("settings.check_time_limit", "10m");
        if (cfg != null && cfg.endsWith("m")) {
            try { limitMs = Long.parseLong(cfg.replace("m", "")) * 60 * 1000; } catch (Exception ignored) {}
        }
        long delay = (limitMs - session.getExtraTimeMs()) / 50;
        if (delay <= 0) delay = 1;
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            session.getTimer().cancel();
            onExpire.run();
        }, delay);
        session.setTimer(task);
    }
    
    public String getPlaceholder(String key, CheckSession session) {
        if (session == null) return "";
        switch (key) {
            case "reason": return session.getBanReason() != null ? session.getBanReason() : "";
            case "duration": return session.getBanDuration() != null ? session.getBanDuration() + " дней" : "";
            case "moderator": return Bukkit.getOfflinePlayer(session.getModeratorId()).getName();
            case "method": return session.getMethod() != null ? session.getMethod().name() : "";
            default: return "";
        }
    }
}
