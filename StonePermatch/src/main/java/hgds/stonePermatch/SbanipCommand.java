package hgds.stonePermatch;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Команда /sbanip "ник" — бан по IP на прокси.
 */
public class SbanipCommand implements SimpleCommand {

    private final ProxyServer server;
    private final BanStorage banStorage;
    private final MessageConfig messages;

    public SbanipCommand(ProxyServer server, BanStorage banStorage, MessageConfig messages) {
        this.server = server;
        this.banStorage = banStorage;
        this.messages = messages;
    }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length == 0) {
            invocation.source().sendMessage(Component.text(messages.getSbanipUsage(), NamedTextColor.RED));
            return;
        }
        String targetNick = String.join(" ", args).trim();
        if (targetNick.isEmpty()) {
            invocation.source().sendMessage(Component.text(messages.getSbanipNoNick(), NamedTextColor.RED));
            return;
        }

        Player target = server.getPlayer(targetNick).orElse(null);
        if (target == null) {
            invocation.source().sendMessage(
                    Component.text(messages.getSbanipNotOnline(targetNick), NamedTextColor.RED)
            );
            return;
        }

        String ip = target.getRemoteAddress().getAddress().getHostAddress();
        banStorage.banIp(ip);
        target.disconnect(BanMessageFormatter.formatSbanipMessage(messages));

        invocation.source().sendMessage(
                Component.text(messages.getSbanipSuccess(targetNick, ip), NamedTextColor.GREEN)
        );
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length == 0 || args.length == 1) {
            String prefix = args.length == 1 ? args[0].toLowerCase() : "";
            return server.getAllPlayers().stream()
                    .map(Player::getUsername)
                    .filter(n -> n.toLowerCase().startsWith(prefix))
                    .sorted()
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("stonepermatch.sbanip");
    }
}
