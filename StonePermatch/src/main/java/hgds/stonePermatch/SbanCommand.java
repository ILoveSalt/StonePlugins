package hgds.stonePermatch;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Команда /sban "ник" — бан аккаунта на прокси.
 */
public class SbanCommand implements SimpleCommand {

    private final ProxyServer server;
    private final BanStorage banStorage;
    private final MessageConfig messages;

    public SbanCommand(ProxyServer server, BanStorage banStorage, MessageConfig messages) {
        this.server = server;
        this.banStorage = banStorage;
        this.messages = messages;
    }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length == 0) {
            invocation.source().sendMessage(Component.text(messages.getSbanUsage(), NamedTextColor.RED));
            return;
        }
        String targetNick = String.join(" ", args).trim();
        if (targetNick.isEmpty()) {
            invocation.source().sendMessage(Component.text(messages.getSbanNoNick(), NamedTextColor.RED));
            return;
        }

        banStorage.banUsername(targetNick);

        server.getPlayer(targetNick).ifPresent(player ->
                player.disconnect(BanMessageFormatter.formatSbanMessage(messages))
        );

        invocation.source().sendMessage(
                Component.text(messages.getSbanSuccess(targetNick), NamedTextColor.GREEN)
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
        return invocation.source().hasPermission("stonepermatch.sban");
    }
}
