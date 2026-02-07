package hgds.stoneoplata;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * HTTP-сервер для приёма вебхуков от сайта. Проверяет подпись X-Signature (HMAC-SHA256) и передаёт выдачу DeliveryService.
 */
public class WebhookServer {

    private final StoneOplata plugin;
    private final Gson gson = new Gson();
    private HttpServer server;

    public WebhookServer(StoneOplata plugin) {
        this.plugin = plugin;
    }

    public void start() {
        OplataConfig config = plugin.getOplataConfig();
        int port = config.getWebhookPort();
        String path = config.getWebhookPath();
        if (path == null || !path.startsWith("/")) {
            path = "/stoneoplata";
        }

        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext(path, this::handle);
            String checkPath = path.endsWith("/") ? path + "check" : path + "/check";
            server.createContext(checkPath, this::handleCheck);
            server.setExecutor(Executors.newSingleThreadExecutor());
            server.start();
            plugin.getLogger().info("[StoneOplata] Вебхук слушает http://0.0.0.0:" + port + path + " и " + checkPath);
        } catch (Exception e) {
            plugin.getLogger().severe("[StoneOplata] Не удалось запустить вебхук на порту " + port + ": " + e.getMessage());
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
            plugin.getLogger().info("[StoneOplata] Вебхук остановлен.");
        }
    }

    private void handle(HttpExchange exchange) {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        String signature = exchange.getRequestHeaders().getFirst("X-Signature");
        String rawBody;
        try {
            rawBody = readBody(exchange.getRequestBody());
        } catch (Exception e) {
            sendResponse(exchange, 400, "Bad Request");
            return;
        }

        if (rawBody == null || rawBody.isEmpty()) {
            sendResponse(exchange, 400, "Empty body");
            return;
        }

        OplataConfig config = plugin.getOplataConfig();
        if (!config.verifySignature(rawBody, signature != null ? signature : "")) {
            if (config.isDebug()) plugin.getLogger().info("[StoneOplata] Вебхук: неверная подпись.");
            plugin.getLogger().warning("[StoneOplata] Вебхук отклонён: неверная подпись.");
            sendResponse(exchange, 403, "Invalid signature");
            return;
        }
        if (config.isDebug()) plugin.getLogger().info("[StoneOplata] Вебхук: подпись OK, body=" + rawBody);

        WebhookPayload payload;
        try {
            payload = gson.fromJson(rawBody, WebhookPayload.class);
        } catch (JsonSyntaxException e) {
            sendResponse(exchange, 400, "Invalid JSON");
            return;
        }

        if (payload == null || payload.getPlayer() == null) {
            sendResponse(exchange, 400, "Missing player");
            return;
        }

        if (Boolean.TRUE.equals(payload.getTest()) && !config.isTestMode()) {
            sendResponse(exchange, 400, "Test payments disabled");
            return;
        }

        // Проверки доната и ответ — в главном потоке (LuckPerms и Bukkit требуют main thread)
        WebhookPayload p = payload;
        HttpExchange ex = exchange;
        Bukkit.getScheduler().runTask(plugin, () -> processAndRespond(ex, p));
    }

    private void processAndRespond(HttpExchange exchange, WebhookPayload payload) {
        OplataConfig config = plugin.getOplataConfig();
        String productType = payload.getProductType() != null ? payload.getProductType().toLowerCase() : "";
        String productId = payload.getProductId();
        String playerName = payload.getPlayer();

        // Кастомная привилегия: бан навсегда, на сайте — «обратитесь в тех. поддержку»
        if ("privilege".equals(productType) && "custom".equalsIgnoreCase(productId != null ? productId : "")) {
            String banCmd = config.getCustomBanCommand().replace("%player%", playerName);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), banCmd);
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("ok", true);
            json.put("custom_contact_support", true);
            sendJsonResponse(exchange, 200, json);
            return;
        }

        // Проверка доната: если у игрока привилегия выше или та же — ниже купить нельзя
        if ("privilege".equals(productType)) {
            String highest = plugin.getDonateCheck().getHighestPermanentPrivilege(playerName);
            if (!plugin.getDonateCheck().canBuyPrivilege(playerName, productId)) {
                if (config.isDebug()) plugin.getLogger().info("[StoneOplata] Отказ: player=" + playerName + ", productId=" + productId + ", highest=" + highest);
                Map<String, Object> json = new LinkedHashMap<>();
                json.put("ok", true);
                json.put("delivered", false);
                json.put("reason", "already_has_higher_privilege");
                json.put("highest_privilege", highest != null ? highest : "");
                sendJsonResponse(exchange, 200, json);
                return;
            }
        }

        plugin.getDeliveryService().scheduleDelivery(payload);
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("ok", true);
        json.put("delivered", true);
        sendJsonResponse(exchange, 200, json);
    }

    private void sendJsonResponse(HttpExchange exchange, int code, Map<String, Object> json) {
        String body = gson.toJson(json);
        try {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(code, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } catch (Exception ignored) {
        }
    }

    private static String readBody(InputStream is) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    /** GET /stoneoplata/check?player=Nickname — проверка уровня доната (подпись X-Signature от "player=Nickname"). */
    private void handleCheck(HttpExchange exchange) {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        String query = exchange.getRequestURI().getQuery();
        if (query == null || !query.startsWith("player=")) {
            sendResponse(exchange, 400, "Missing player");
            return;
        }
        String playerName;
        try {
            playerName = URLDecoder.decode(query.substring(7).trim(), StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            playerName = query.substring(7).trim();
        }
        if (playerName.isEmpty()) {
            sendResponse(exchange, 400, "Empty player");
            return;
        }
        OplataConfig config = plugin.getOplataConfig();
        String toSign = "player=" + playerName;
        String signature = exchange.getRequestHeaders().getFirst("X-Signature");
        if (!config.verifySignature(toSign, signature != null ? signature : "")) {
            if (config.isDebug()) plugin.getLogger().info("[StoneOplata] Check: неверная подпись для player=" + playerName);
            sendResponse(exchange, 403, "Invalid signature");
            return;
        }
        String highest = plugin.getDonateCheck().getHighestPermanentPrivilege(playerName);
        List<String> order = plugin.getDonateCheck().getPrivilegeOrder();
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("ok", true);
        json.put("highest_privilege", highest != null ? highest : null);
        json.put("privilege_order", order != null ? order : java.util.Collections.emptyList());
        sendJsonResponse(exchange, 200, json);
    }

    private void sendResponse(HttpExchange exchange, int code, String body) {
        try {
            byte[] bytes = (body != null ? body : "").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(code, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } catch (Exception ignored) {
        }
    }
}
