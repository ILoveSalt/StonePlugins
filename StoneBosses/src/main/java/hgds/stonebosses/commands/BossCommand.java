package hgds.stonebosses.commands;

import hgds.stonebosses.StoneBosses;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BossCommand implements CommandExecutor {
    
    private final StoneBosses plugin;
    
    public BossCommand(StoneBosses plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cИспользуйте: /stonebosses <start|reload>");
            return true;
        }
        
        if (args[0].equalsIgnoreCase("start")) {
            if (!sender.hasPermission("stonebosses.admin")) {
                sender.sendMessage("§cУ вас нет прав!");
                return true;
            }
            // Логика запуска босса
            return true;
        }
        
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("stonebosses.admin")) {
                sender.sendMessage("§cУ вас нет прав!");
                return true;
            }
            plugin.reloadConfig();
            sender.sendMessage("§aКонфиг перезагружен!");
            return true;
        }
        
        return false;
    }
}
