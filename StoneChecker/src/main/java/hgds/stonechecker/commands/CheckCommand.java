package hgds.stonechecker.commands;

import hgds.stonechecker.StoneChecker;
import hgds.stonechecker.gui.CheckGUI;
import hgds.stonechecker.models.CheckSession;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CheckCommand implements CommandExecutor {
    
    private final StoneChecker plugin;
    
    public CheckCommand(StoneChecker plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cТолько для игроков!");
            return true;
        }
        Player mod = (Player) sender;
        if (!mod.hasPermission("stonechecker.use")) {
            mod.sendMessage(cfg("messages.no_permission"));
            return true;
        }
        
        if (args.length == 0) {
            mod.sendMessage("§c/check <ник> | /check discord|telegram|anydesk <ник> | /check addtime | /check stop | /check cheat <ник>");
            return true;
        }
        
        if (args[0].equalsIgnoreCase("stop")) {
            CheckSession s = plugin.getCheckManager().getByModerator(mod.getUniqueId());
            if (s == null || s.getState() != CheckSession.State.IN_PROGRESS) {
                mod.sendMessage(cfg("commands.stop.wrong_state"));
                return true;
            }
            plugin.getCheckManager().endSession(s, true);
            mod.sendMessage(cfg("commands.stop.success"));
            return true;
        }
        
        if (args[0].equalsIgnoreCase("addtime")) {
            CheckSession s = plugin.getCheckManager().getByModerator(mod.getUniqueId());
            if (s == null) {
                mod.sendMessage(cfg("commands.addtime.wrong_state"));
                return true;
            }
            mod.sendMessage(cfg("commands.addtime.too_much"));
            return true;
        }
        
        if (args[0].equalsIgnoreCase("cheat") && args.length >= 2) {
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                mod.sendMessage("§cИгрок не найден!");
                return true;
            }
            CheckSession s = plugin.getCheckManager().getByTarget(target.getUniqueId());
            if (s == null) {
                mod.sendMessage("§cНет активной проверки на этого игрока.");
                return true;
            }
            CheckGUI.openMain(plugin, mod, s);
            return true;
        }
        
        if (args.length >= 2 && (args[0].equalsIgnoreCase("discord") || args[0].equalsIgnoreCase("telegram") || args[0].equalsIgnoreCase("anydesk"))) {
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                mod.sendMessage("§cИгрок не найден!");
                return true;
            }
            CheckSession.Method method = CheckSession.Method.valueOf(args[0].toUpperCase());
            CheckSession s = plugin.getCheckManager().startCheck(mod, target);
            if (s == null) {
                if (plugin.getCheckManager().isAfk(target)) mod.sendMessage(cfg("afk.message"));
                else mod.sendMessage("§cИгрок уже в проверке или ошибка.");
                return true;
            }
            s.setMethod(method);
            s.setState(CheckSession.State.WAITING_CONTACT);
            String prefix = plugin.getConfig().getString("messages.prefix", "§8[§bStoneChecker§8] ");
            target.sendMessage(prefix + apply("messages.check_started", s, target).replace("&", "§"));
            target.sendMessage(apply("messages.send_id", s, target).replace("&", "§"));
            target.sendMessage(apply("messages.confess_hint", s, target).replace("&", "§"));
            plugin.getCheckManager().runTimer(s, () -> {
                plugin.getCheckManager().endSession(s, true);
                mod.sendMessage("§cВремя вышло. Используйте /check cheat " + target.getName());
            });
            return true;
        }
        
        if (args.length >= 1) {
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                mod.sendMessage("§cИгрок не найден!");
                return true;
            }
            if (plugin.getCheckManager().getByTarget(target.getUniqueId()) != null) {
                mod.sendMessage("§cИгрок уже в проверке.");
                return true;
            }
            if (plugin.getCheckManager().isAfk(target)) {
                mod.sendMessage(cfg("afk.message"));
                return true;
            }
            CheckSession s = plugin.getCheckManager().startCheck(mod, target);
            if (s == null) return true;
            mod.sendMessage("§aПроверка начата. Используйте: /check discord|telegram|anydesk " + target.getName());
            return true;
        }
        
        return true;
    }
    
    private String cfg(String path) {
        return plugin.getConfig().getString(path, path).replace("&", "§");
    }

    private String apply(String path, CheckSession s, Player target) {
        String m = plugin.getConfig().getString(path, path);
        m = m.replace("%stonechecker_method%", s.getMethod() != null ? s.getMethod().name() : "");
        m = m.replace("%stonechecker_moderator%", Bukkit.getOfflinePlayer(s.getModeratorId()).getName());
        m = m.replace("%player%", target.getName());
        return m;
    }
}
