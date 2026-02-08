package hgds.stonePermatch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Хранилище забаненных ников и IP. Сохранение в файлы в папке плагина.
 */
public class BanStorage {

    private final Path dataDirectory;
    private final Set<String> bannedUsernames = ConcurrentHashMap.newKeySet();
    private final Set<String> bannedIps = ConcurrentHashMap.newKeySet();

    private static final String BANNED_PLAYERS_FILE = "banned_players.txt";
    private static final String BANNED_IPS_FILE = "banned_ips.txt";

    public BanStorage(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
        load();
    }

    public void banUsername(String username) {
        bannedUsernames.add(username.toLowerCase());
        saveUsernames();
    }

    public void banIp(String ip) {
        bannedIps.add(normalizeIp(ip));
        saveIps();
    }

    public boolean isUsernameBanned(String username) {
        return bannedUsernames.contains(username.toLowerCase());
    }

    public boolean isIpBanned(String ip) {
        return bannedIps.contains(normalizeIp(ip));
    }

    public Set<String> getBannedUsernames() {
        return Collections.unmodifiableSet(bannedUsernames);
    }

    public Set<String> getBannedIps() {
        return Collections.unmodifiableSet(bannedIps);
    }

    private static String normalizeIp(String ip) {
        if (ip == null) return "";
        int colon = ip.indexOf(':');
        return colon >= 0 ? ip.substring(0, colon) : ip;
    }

    private void load() {
        try {
            Files.createDirectories(dataDirectory);
            Path playersFile = dataDirectory.resolve(BANNED_PLAYERS_FILE);
            Path ipsFile = dataDirectory.resolve(BANNED_IPS_FILE);

            if (Files.exists(playersFile)) {
                try (Stream<String> lines = Files.lines(playersFile)) {
                    lines.map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(String::toLowerCase)
                            .forEach(bannedUsernames::add);
                }
            }
            if (Files.exists(ipsFile)) {
                try (Stream<String> lines = Files.lines(ipsFile)) {
                    lines.map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(BanStorage::normalizeIp)
                            .forEach(bannedIps::add);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load bans", e);
        }
    }

    private void saveUsernames() {
        try {
            Files.write(
                    dataDirectory.resolve(BANNED_PLAYERS_FILE),
                    bannedUsernames.stream().sorted().toList()
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to save banned players", e);
        }
    }

    private void saveIps() {
        try {
            Files.write(
                    dataDirectory.resolve(BANNED_IPS_FILE),
                    bannedIps.stream().sorted().toList()
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to save banned IPs", e);
        }
    }
}
