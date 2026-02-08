package hgds.stonePermatch;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Загружает сообщения из message.yml (из папки плагина или из ресурса JAR).
 */
public class MessageConfig {

    private final Map<String, Object> config;

    @SuppressWarnings("unchecked")
    public MessageConfig(Path dataDirectory) throws IOException {
        Path file = dataDirectory.resolve("message.yml");
        if (!Files.exists(file)) {
            copyDefault(file);
        }
        try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            config = new Yaml().load(reader);
        }
        if (config == null) {
            throw new IOException("message.yml is empty");
        }
    }

    private void copyDefault(Path target) throws IOException {
        Files.createDirectories(target.getParent());
        try (InputStream in = getClass().getResourceAsStream("/message.yml")) {
            if (in == null) {
                throw new IOException("Ресурс /message.yml не найден в JAR плагина");
            }
            Files.copy(in, target);
        }
    }

    private String get(String path) {
        String[] keys = path.split("\\.");
        Object current = config;
        for (String key : keys) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(key);
            } else {
                return null;
            }
        }
        return current != null ? current.toString() : null;
    }

    private String get(String path, String def) {
        String v = get(path);
        return v != null ? v : def;
    }

    public String getBanHeader() {
        return get("ban.header", "stoneaura");
    }

    public String getBanAccountBanned() {
        return get("ban.account-banned", "Ваш аккаунт был заблокирован на сервере");
    }

    public String getBanIpBanned() {
        return get("ban.ip-banned", "Ваш ip был заблокирован на сервере");
    }

    public String getBanReason() {
        return get("ban.reason", "По решению Администрации");
    }

    public String getBanSupport() {
        return get("ban.support", "Если считаете что бан был ошибочным, напиши в техническую поддержку t.me/hgds_2");
    }

    public String getSbanUsage() {
        return get("commands.sban.usage", "Использование: /sban <ник>");
    }

    public String getSbanNoNick() {
        return get("commands.sban.no-nick", "Укажите ник игрока.");
    }

    public String getSbanSuccess(String nick) {
        return get("commands.sban.success", "Аккаунт %s заблокирован на прокси.").replace("%s", nick);
    }

    public String getSbanipUsage() {
        return get("commands.sbanip.usage", "Использование: /sbanip <ник>");
    }

    public String getSbanipNoNick() {
        return get("commands.sbanip.no-nick", "Укажите ник игрока.");
    }

    public String getSbanipNotOnline(String nick) {
        return get("commands.sbanip.not-online", "Игрок %s не в сети. IP можно забанить только для онлайн-игрока.").replace("%s", nick);
    }

    public String getSbanipSuccess(String nick, String ip) {
        String fmt = get("commands.sbanip.success", "IP игрока %s (%s) заблокирован на прокси.");
        int first = fmt.indexOf("%s");
        if (first >= 0) {
            fmt = fmt.substring(0, first) + nick + fmt.substring(first + 2);
        }
        int second = fmt.indexOf("%s");
        if (second >= 0) {
            fmt = fmt.substring(0, second) + ip + fmt.substring(second + 2);
        }
        return fmt;
    }
}
