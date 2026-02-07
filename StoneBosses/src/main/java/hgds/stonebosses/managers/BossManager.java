package hgds.stonebosses.managers;

import hgds.stonebosses.StoneBosses;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class BossManager {
    
    private final StoneBosses plugin;
    private Entity currentBoss;
    
    public BossManager(StoneBosses plugin) {
        this.plugin = plugin;
    }
    
    public void spawnBoss(String bossId, int delay) {
        // Логика спавна босса
    }
    
    public void startEvent(String bossId, int delay) {
        // Запуск ивента босса
    }
    
    public Entity getCurrentBoss() {
        return currentBoss;
    }
}
