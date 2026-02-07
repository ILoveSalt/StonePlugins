package hgds.stonebosses;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BossesCommand implements CommandExecutor, TabCompleter {
    private final StoneBosses plugin;

    public BossesCommand(StoneBosses plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "delay":
                if (!sender.hasPermission("stonebosses.player")) {
                    sender.sendMessage(ChatColor.RED + "Нет доступа.");
                    return true;
                }
                int sec = plugin.getBossManager().getSecondsUntilSpawn();
                int min = sec / 60;
                int h = min / 60;
                min = min % 60;
                sender.sendMessage(ChatColor.GREEN + "Следующий босс через: " + h + " ч " + min + " мин");
                return true;
            case "gui":
                if (!sender.hasPermission("stonebosses.player")) {
                    sender.sendMessage(ChatColor.RED + "Нет доступа.");
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Только для игроков.");
                    return true;
                }
                plugin.getGuiManager().openGui((Player) sender);
                return true;
            case "create":
                if (!sender.hasPermission("stonebosses.admin")) {
                    sender.sendMessage(ChatColor.RED + "Нет доступа.");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Использование: /bosses create \"название босса\" \"моб\"");
                    return true;
                }
                String name = args[1].replace("\"", "");
                String mob = args[2].replace("\"", "");
                String id = name.toLowerCase().replace(" ", "_").replaceAll("[^a-z0-9_]", "");
                if (id.isEmpty()) id = "boss";
                File bossFile = new File(plugin.getConfigManager().getBossesFolder(), id + ".yml");
                if (bossFile.exists()) {
                    sender.sendMessage(ChatColor.RED + "Босс с таким id уже есть: " + id);
                    return true;
                }
                YamlConfiguration cfg = new YamlConfiguration();
                cfg.set("Name", "&c" + name);
                cfg.set("mob", mob.toUpperCase());
                cfg.set("heatlh", 2048);
                cfg.set("armor", 40);
                cfg.set("damage", 20);
                cfg.set("speed", 0.3);
                cfg.set("effect_damage", new ArrayList<String>());
                cfg.set("spawn_children_heatlh", 500);
                cfg.set("children.quantity", 5);
                cfg.set("children.name", "&cПриспешник");
                cfg.set("children.mob", "ZOMBIE");
                cfg.set("children.heatlh", 128);
                cfg.set("children.armor", 5);
                cfg.set("children.damage", 10);
                try {
                    cfg.save(bossFile);
                    plugin.getConfigManager().setActiveBossId(id);
                    sender.sendMessage(ChatColor.GREEN + "Босс создан: " + id + ".yml");
                } catch (Exception e) {
                    sender.sendMessage(ChatColor.RED + "Ошибка: " + e.getMessage());
                }
                return true;
            case "forcestart":
                if (!sender.hasPermission("stonebosses.admin")) {
                    sender.sendMessage(ChatColor.RED + "Нет доступа.");
                    return true;
                }
                plugin.getBossManager().spawnBoss();
                sender.sendMessage(ChatColor.GREEN + "Босс призван.");
                return true;
            case "kill":
                if (!sender.hasPermission("stonebosses.admin")) {
                    sender.sendMessage(ChatColor.RED + "Нет доступа.");
                    return true;
                }
                plugin.getBossManager().killBoss();
                sender.sendMessage(ChatColor.GREEN + "Босс убит.");
                return true;
            case "list":
                if (!sender.hasPermission("stonebosses.admin")) {
                    sender.sendMessage(ChatColor.RED + "Нет доступа.");
                    return true;
                }
                List<String> ids = plugin.getConfigManager().getBossIds();
                sender.sendMessage(ChatColor.GREEN + "Боссы: " + String.join(", ", ids));
                return true;
            case "add":
                if (!sender.hasPermission("stonebosses.admin")) {
                    sender.sendMessage(ChatColor.RED + "Нет доступа.");
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Только для игроков.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Использование: /bosses add <номер_слота>");
                    return true;
                }
                int slotId;
                try {
                    slotId = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Укажите число.");
                    return true;
                }
                ItemStack hand = ((Player) sender).getInventory().getItemInMainHand();
                if (hand.getType().isAir()) {
                    sender.sendMessage(ChatColor.RED + "Возьмите предмет в руку.");
                    return true;
                }
                plugin.getConfigManager().getGuiItemsConfig().set("items." + slotId + ".material", hand.getType().name());
                plugin.getConfigManager().getGuiItemsConfig().set("items." + slotId + ".display_name", hand.hasItemMeta() && hand.getItemMeta() != null && hand.getItemMeta().hasDisplayName() ? hand.getItemMeta().getDisplayName() : hand.getType().name());
                if (hand.getItemMeta() instanceof SkullMeta) {
                    SkullMeta sm = (SkullMeta) hand.getItemMeta();
                    if (sm.hasOwner()) {
                        plugin.getConfigManager().getGuiItemsConfig().set("items." + slotId + ".owner", sm.getOwningPlayer() != null ? sm.getOwningPlayer().getName() : "");
                    }
                }
                plugin.getConfigManager().saveGuiItems();
                sender.sendMessage(ChatColor.GREEN + "Предмет добавлен в gui_items под номером " + slotId);
                return true;
            case "setspawn":
                if (!sender.hasPermission("stonebosses.admin")) {
                    sender.sendMessage(ChatColor.RED + "Нет доступа.");
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Только для игроков.");
                    return true;
                }
                plugin.getConfigManager().setSpawnLocation(((Player) sender).getLocation());
                sender.sendMessage(ChatColor.GREEN + "Точка спавна боссов установлена.");
                return true;
            default:
                sendUsage(sender);
                return true;
        }
    }

    private void sendUsage(CommandSender sender) {
        if (sender.hasPermission("stonebosses.admin")) {
            sender.sendMessage(ChatColor.GRAY + "/bosses delay - когда заспавнится босс");
            sender.sendMessage(ChatColor.GRAY + "/bosses gui - GUI призыва");
            sender.sendMessage(ChatColor.GRAY + "/bosses create \"название\" \"моб\" - создать босса");
            sender.sendMessage(ChatColor.GRAY + "/bosses forcestart - призвать босса");
            sender.sendMessage(ChatColor.GRAY + "/bosses kill - убить босса");
            sender.sendMessage(ChatColor.GRAY + "/bosses list - список боссов");
            sender.sendMessage(ChatColor.GRAY + "/bosses add <число> - добавить предмет в GUI");
            sender.sendMessage(ChatColor.GRAY + "/bosses setspawn - установить точку спавна");
        } else {
            sender.sendMessage(ChatColor.GRAY + "/bosses delay - когда заспавнится босс");
            sender.sendMessage(ChatColor.GRAY + "/bosses gui - GUI призыва");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            String p = args[0].toLowerCase();
            if (sender.hasPermission("stonebosses.player")) {
                if ("delay".startsWith(p)) out.add("delay");
                if ("gui".startsWith(p)) out.add("gui");
            }
            if (sender.hasPermission("stonebosses.admin")) {
                if ("create".startsWith(p)) out.add("create");
                if ("forcestart".startsWith(p)) out.add("forcestart");
                if ("kill".startsWith(p)) out.add("kill");
                if ("list".startsWith(p)) out.add("list");
                if ("add".startsWith(p)) out.add("add");
                if ("setspawn".startsWith(p)) out.add("setspawn");
            }
            return out.stream().sorted().collect(Collectors.toList());
        }
        return out;
    }
}
