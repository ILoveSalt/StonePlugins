package hgds.stonePermatch;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import net.kyori.adventure.text.Component;

import java.net.InetSocketAddress;

/**
 * При подключении игрока проверяет бан по нику и по IP, при совпадении отклоняет вход с сообщением.
 */
public class PreLoginListener {

    private final BanStorage banStorage;
    private final MessageConfig messages;

    public PreLoginListener(BanStorage banStorage, MessageConfig messages) {
        this.banStorage = banStorage;
        this.messages = messages;
    }

    @Subscribe
    public void onPreLogin(PreLoginEvent event) {
        String username = event.getUsername();
        InetSocketAddress address = event.getConnection().getRemoteAddress();
        String ip = address != null ? address.getAddress().getHostAddress() : "";

        if (banStorage.isUsernameBanned(username)) {
            Component message = BanMessageFormatter.formatSbanMessage(messages);
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(message));
            return;
        }
        if (banStorage.isIpBanned(ip)) {
            Component message = BanMessageFormatter.formatSbanipMessage(messages);
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(message));
        }
    }
}
