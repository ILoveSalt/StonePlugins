package hgds.stonechecker.commands;

import hgds.stonechecker.StoneChecker;
import hgds.stonechecker.models.CheckSession;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ContactCommand implements CommandExecutor {
    
    private final StoneChecker plugin;
    
    public ContactCommand(StoneChecker plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        if (args.length == 0) {
            player.sendMessage("§c/contact <сообщение>");
            return true;
        }
        CheckSession s = plugin.getCheckManager().getByTarget(player.getUniqueId());
        if (s == null) {
            player.sendMessage("§cУ вас нет активной проверки.");
            return true;
        }
        String msg = String.join(" ", args);
        if ("Я с читами".equalsIgnoreCase(msg.trim())) {
            plugin.getCheckManager().endSession(s, true);
            player.sendMessage(plugin.getConfig().getString("messages.confess_success", "§cВы признались...").replace("&", "§"));
            Player mod = Bukkit.getPlayer(s.getModeratorId());
            if (mod != null) mod.sendMessage("§cИгрок " + player.getName() + " признался. Используйте /check cheat " + player.getName());
            return true;
        }
        s.setContactInfo(msg);
        s.setState(CheckSession.State.IN_PROGRESS);
        if (s.getTimer() != null) s.getTimer().cancel();
        
        String toMod = plugin.getConfig().getString("messages.contact_to_mod", "&b[CONTACT] %player%: %message%").replace("%player%", player.getName()).replace("%message%", msg).replace("&", "§");
        String toPlayer = plugin.getConfig().getString("messages.contact_to_player", "&b[CONTACT] %mod%: %message%").replace("%stonechecker_moderator%", Bukkit.getOfflinePlayer(s.getModeratorId()).getName()).replace("%message%", msg).replace("&", "§");
        Player mod = Bukkit.getPlayer(s.getModeratorId());
        if (mod != null && mod.isOnline()) mod.sendMessage(toMod);
        player.sendMessage("§aСообщение отправлено проверяющему.");
        return true;
    }
}
