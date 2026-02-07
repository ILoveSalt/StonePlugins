package hgds.stonebosses;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class BossListener implements Listener {
    private final StoneBosses plugin;
    private LivingEntity registeredBoss;

    public BossListener(StoneBosses plugin) {
        this.plugin = plugin;
    }

    public void registerBoss(LivingEntity boss) {
        this.registeredBoss = boss;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamageToBoss(EntityDamageByEntityEvent e) {
        if (registeredBoss == null || !e.getEntity().equals(registeredBoss)) return;
        if (!(e.getDamager() instanceof Player)) return;
        Player damager = (Player) e.getDamager();
        double damage = e.getFinalDamage();
        plugin.getBossManager().addDamage(damager.getUniqueId(), damage);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBossHitsPlayer(EntityDamageByEntityEvent e) {
        if (registeredBoss == null || !e.getDamager().equals(registeredBoss)) return;
        if (!(e.getEntity() instanceof Player)) return;
        Player victim = (Player) e.getEntity();
        BossConfig cfg = plugin.getBossManager().getCurrentBossConfig();
        if (cfg != null) {
            cfg.applyEffectDamage(victim);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(EntityDeathEvent e) {
        if (registeredBoss == null || !e.getEntity().equals(registeredBoss)) return;
        e.getDrops().clear();
        e.setDroppedExp(0);
        plugin.getBossManager().onBossRemoved(true);
        registeredBoss = null;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        plugin.getBossManager().addBossBarPlayer(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.getBossManager().removeBossBarPlayer(e.getPlayer());
    }
}
