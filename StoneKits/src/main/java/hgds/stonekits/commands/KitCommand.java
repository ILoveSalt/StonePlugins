package hgds.stonekits.commands;

import hgds.stonekits.StoneKits;
import hgds.stonekits.gui.KitGUI;
import hgds.stonekits.models.Kit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KitCommand implements CommandExecutor, TabCompleter {

    private final StoneKits plugin;

    public KitCommand(StoneKits plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§e/kit <набор> §7— получить набор");
            sender.sendMessage("§e/kit preview <набор> §7— предпросмотр");
            if (sender.hasPermission("stonekits.admin")) {
                sender.sendMessage("§e/kit create <набор> §7— создать");
                sender.sendMessage("§e/kit delete <набор> §7— удалить");
                sender.sendMessage("§e/kit edit <набор> §7— редактировать");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("preview")) {
            if (!(sender instanceof Player)) { sender.sendMessage("§cТолько для игроков!"); return true; }
            if (args.length < 2) { sender.sendMessage("§c/kit preview <набор>"); return true; }
            Kit kit = plugin.getKitManager().getKit(args[1]);
            if (kit == null) {
                sender.sendMessage("§cНабор §f" + args[1] + " §cне найден.");
                return true;
            }
            KitGUI.openPreview((Player) sender, kit);
            return true;
        }

        if (args[0].equalsIgnoreCase("create")) {
            if (!sender.hasPermission("stonekits.admin")) { sender.sendMessage("§cНет прав."); return true; }
            if (args.length < 2) { sender.sendMessage("§c/kit create <набор>"); return true; }
            String name = args[1];
            if (!name.matches("[a-zA-Z0-9_]+")) {
                sender.sendMessage("§cНазвание набора может содержать только буквы, цифры и нижнее подчёркивание.");
                return true;
            }
            if (plugin.getKitManager().createKit(name)) {
                plugin.getKitManager().saveKits();
                sender.sendMessage("§aНабор §f" + name + " §aуспешно создан!");
            } else {
                sender.sendMessage("§cНабор с таким названием уже существует.");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("delete")) {
            if (!sender.hasPermission("stonekits.admin")) { sender.sendMessage("§cНет прав."); return true; }
            if (args.length < 2) { sender.sendMessage("§c/kit delete <набор>"); return true; }
            if (plugin.getKitManager().deleteKit(args[1])) {
                plugin.getKitManager().saveKits();
                sender.sendMessage("§aНабор §f" + args[1] + " §aуспешно удалён!");
            } else {
                sender.sendMessage("§cНабор §f" + args[1] + " §cне найден.");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("edit")) {
            if (!(sender instanceof Player)) { sender.sendMessage("§cТолько для игроков!"); return true; }
            if (!sender.hasPermission("stonekits.admin")) { sender.sendMessage("§cНет прав."); return true; }
            if (args.length < 2) { sender.sendMessage("§c/kit edit <набор>"); return true; }
            Kit kit = plugin.getKitManager().getKit(args[1]);
            if (kit == null) {
                sender.sendMessage("§cНабор §f" + args[1] + " §cне найден.");
                return true;
            }
            KitGUI.openEdit((Player) sender, kit);
            return true;
        }

        if (!(sender instanceof Player)) { sender.sendMessage("§cТолько для игроков!"); return true; }
        Player player = (Player) sender;
        Kit kit = plugin.getKitManager().getKit(args[0]);
        if (kit == null) {
            player.sendMessage("§cНабор §f" + args[0] + " §cне найден.");
            return true;
        }
        if (!plugin.getKitManager().giveKit(player, kit)) {
            if (!player.hasPermission(kit.getPermission())) player.sendMessage("§cУ вас нет доступа к этому набору.");
            else if (plugin.getKitManager().getCooldownLeft(player, kit) > 0)
                player.sendMessage("§cВы сможете получить этот набор через §e" + plugin.getKitManager().getCooldownLeft(player, kit) + "§c сек.");
            else if (kit.getMaxUses() >= 0) player.sendMessage("§cВы исчерпали количество использований этого набора.");
            else player.sendMessage("§cНедостаточно места в инвентаре.");
            return true;
        }
        player.sendMessage("§aВы получили набор §f" + kit.getDisplayName() + "§a!");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            String a = args[0].toLowerCase();
            List<String> names = plugin.getKitManager().getAllKits().stream().map(Kit::getId).collect(Collectors.toList());
            names.add("preview");
            if (sender.hasPermission("stonekits.admin")) { names.add("create"); names.add("delete"); names.add("edit"); }
            for (String n : names) if (n.toLowerCase().startsWith(a)) out.add(n);
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("preview") || args[0].equalsIgnoreCase("edit") || args[0].equalsIgnoreCase("delete"))) {
            String a = args[1].toLowerCase();
            for (Kit kit : plugin.getKitManager().getAllKits()) if (kit.getId().toLowerCase().startsWith(a)) out.add(kit.getId());
        }
        return out;
    }
}
