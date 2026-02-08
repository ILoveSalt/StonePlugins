package hgds.stonePermatch;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.List;

/**
 * Форматирует сообщение о бане (без выравнивания).
 * Заголовок (stoneaura) поддерживает цветовые коды через & (например &6, &c, &x&r&r&g&g&b&b для RGB).
 */
public final class BanMessageFormatter {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    /**
     * Сообщение для /sban (бан аккаунта).
     */
    public static Component formatSbanMessage(MessageConfig config) {
        Component header = parseHeader(config.getBanHeader());
        List<String> rest = List.of(
                "",
                config.getBanAccountBanned(),
                config.getBanReason(),
                "",
                config.getBanSupport()
        );
        return buildComponent(header, rest);
    }

    /**
     * Сообщение для /sbanip (бан по IP).
     */
    public static Component formatSbanipMessage(MessageConfig config) {
        Component header = parseHeader(config.getBanHeader());
        List<String> rest = List.of(
                "",
                config.getBanIpBanned(),
                config.getBanReason(),
                "",
                config.getBanSupport()
        );
        return buildComponent(header, rest);
    }

    /** Парсит заголовок: поддерживает & коды (например &6stoneaura или &x&f&f&0&0&0&0stoneaura). */
    private static Component parseHeader(String header) {
        if (header == null || header.isEmpty()) {
            return Component.text("stoneaura", NamedTextColor.RED);
        }
        return LEGACY.deserialize(header);
    }

    private static Component buildComponent(Component header, List<String> rest) {
        List<Component> components = new ArrayList<>();
        components.add(header);
        for (String line : rest) {
            components.add(Component.text(line, NamedTextColor.RED));
        }
        return Component.join(JoinConfiguration.newlines(), components);
    }
}
