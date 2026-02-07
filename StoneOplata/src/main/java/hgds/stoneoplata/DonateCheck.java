package hgds.stoneoplata;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Проверка: если у игрока привилегия выше или та же — привилегии ниже купить нельзя.
 * Использует LuckPerms API через рефлексию (без жёсткой зависимости).
 */
public class DonateCheck {

    private final StoneOplata plugin;
    private final List<String> privilegeOrder;

    public DonateCheck(StoneOplata plugin) {
        this.plugin = plugin;
        this.privilegeOrder = plugin.getConfig().getStringList("donate-check.privilege-order");
    }

    public boolean isCheckEnabled() {
        return plugin.getConfig().getBoolean("donate-check.enabled", true)
                && privilegeOrder != null && !privilegeOrder.isEmpty();
    }

    public List<String> getPrivilegeOrder() {
        return privilegeOrder;
    }

    private boolean isDebug() {
        return plugin.getOplataConfig() != null && plugin.getOplataConfig().isDebug();
    }

    /**
     * Можно ли купить привилегию productId для игрока playerName.
     * false если у игрока уже есть привилегия выше или равная (по списку privilege-order).
     */
    public boolean canBuyPrivilege(String playerName, String productId) {
        if (!isCheckEnabled() || productId == null) return true;
        String highest = getHighestPermanentPrivilege(playerName);
        if (highest == null) {
            if (isDebug()) plugin.getLogger().info("[StoneOplata] DonateCheck: player=" + playerName + ", productId=" + productId + ", highest=none -> canBuy=true");
            return true;
        }
        int productIndex = indexOf(productId);
        int highestIndex = indexOf(highest);
        if (productIndex < 0 || highestIndex < 0) return true;
        boolean canBuy = productIndex > highestIndex;
        if (isDebug()) plugin.getLogger().info("[StoneOplata] DonateCheck: player=" + playerName + ", productId=" + productId + ", highest=" + highest + ", productIndex=" + productIndex + ", highestIndex=" + highestIndex + " -> canBuy=" + canBuy);
        return canBuy;
    }

    /**
     * Самая высокая привилегия (навсегда) у игрока по списку privilege-order.
     * Проверяем от конца списка (custom) к началу (hero).
     */
    public String getHighestPermanentPrivilege(String playerName) {
        if (!isCheckEnabled() || privilegeOrder == null) return null;
        for (int i = privilegeOrder.size() - 1; i >= 0; i--) {
            String group = privilegeOrder.get(i);
            if (hasPermanentGroup(playerName, group)) return group;
        }
        return null;
    }

    private int indexOf(String id) {
        if (id == null || privilegeOrder == null) return -1;
        String lower = id.toLowerCase();
        for (int i = 0; i < privilegeOrder.size(); i++) {
            if (privilegeOrder.get(i).toLowerCase().equals(lower)) return i;
        }
        return -1;
    }

    /**
     * Есть ли у игрока группа groupName навсегда (постоянно, не temp).
     */
    public boolean hasPermanentGroup(String playerName, String groupName) {
        if (playerName == null || groupName == null) return false;
        try {
            Object api = Class.forName("net.luckperms.api.LuckPermsProvider")
                    .getMethod("get").invoke(null);
            Object userManager = api.getClass().getMethod("getUserManager").invoke(api);
            Object user = findUser(userManager, playerName);
            if (user == null) return false;
            return hasPermanentGroupInUser(user, groupName);
        } catch (ClassNotFoundException e) {
            // LuckPerms не установлен
        } catch (Exception e) {
            if (isDebug()) plugin.getLogger().warning("[StoneOplata] DonateCheck: " + e.getMessage());
        }
        return false;
    }

    private Object findUser(Object userManager, String playerName) throws Exception {
        Method getLoaded = userManager.getClass().getMethod("getLoadedUsers");
        Object loaded = getLoaded.invoke(userManager);
        if (loaded instanceof Collection) {
            for (Object u : (Collection<?>) loaded) {
                if (u == null) continue;
                Method getUsername = u.getClass().getMethod("getUsername");
                Object name = getUsername.invoke(u);
                if (name != null && playerName.equalsIgnoreCase(String.valueOf(name)))
                    return u;
            }
        }
        UUID uuid = resolveUuid(playerName);
        if (uuid == null) return null;
        Method loadUser = userManager.getClass().getMethod("loadUser", UUID.class);
        Object future = loadUser.invoke(userManager, uuid);
        if (future == null) return null;
        return future.getClass().getMethod("join").invoke(future);
    }

    private UUID resolveUuid(String playerName) {
        Player online = Bukkit.getPlayerExact(playerName);
        if (online != null) return online.getUniqueId();
        OfflinePlayer off = Bukkit.getOfflinePlayer(playerName);
        return off.getUniqueId();
    }

    private boolean hasPermanentGroupInUser(Object user, String groupName) throws Exception {
        String targetKey = "group." + groupName.toLowerCase();
        Object nodesToCheck = null;
        try {
            Method getQueryOptions = user.getClass().getMethod("getQueryOptions");
            Object queryOptions = getQueryOptions.invoke(user);
            if (queryOptions != null) {
                Method resolveInherited = user.getClass().getMethod("resolveInheritedNodes",
                        Class.forName("net.luckperms.api.query.QueryOptions"));
                nodesToCheck = resolveInherited.invoke(user, queryOptions);
            }
        } catch (Exception ignored) { }
        if (nodesToCheck == null || !(nodesToCheck instanceof Collection))
            nodesToCheck = user.getClass().getMethod("getNodes").invoke(user);
        if (!(nodesToCheck instanceof Collection)) return false;
        for (Object node : (Collection<?>) nodesToCheck) {
            if (node == null) continue;
            String key = getNodeKey(node);
            if (key == null || !key.equalsIgnoreCase(targetKey)) continue;
            if (isNodePermanent(node)) return true;
        }
        return false;
    }

    private String getNodeKey(Object node) {
        try {
            Method getKey = node.getClass().getMethod("getKey");
            Object k = getKey.invoke(node);
            return k != null ? String.valueOf(k) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isNodePermanent(Object node) {
        try {
            try {
                Method hasExpiry = node.getClass().getMethod("hasExpiry");
                Boolean temp = (Boolean) hasExpiry.invoke(node);
                return !Boolean.TRUE.equals(temp);
            } catch (NoSuchMethodException e) { }
            Method getExpiry = node.getClass().getMethod("getExpiry");
            Object expiry = getExpiry.invoke(node);
            if (expiry == null) return true;
            String cn = expiry.getClass().getName();
            if (cn.contains("Optional")) {
                Method isPresent = expiry.getClass().getMethod("isPresent");
                Boolean present = (Boolean) isPresent.invoke(expiry);
                return !Boolean.TRUE.equals(present);
            }
            return false;
        } catch (Exception e) {
            return true;
        }
    }
}
