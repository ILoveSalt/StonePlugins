package hgds.stonePermatch;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;

@Plugin(id = "stonepermatch", name = "StonePermatch", version = "1.0-SNAPSHOT")
public class StonePermatch {

    @Inject
    private Logger logger;

    @Inject
    private ProxyServer server;

    @Inject
    @DataDirectory
    private Path dataDirectory;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        try {
            MessageConfig messages = new MessageConfig(dataDirectory);
            BanStorage banStorage = new BanStorage(dataDirectory);

            server.getEventManager().register(this, new PreLoginListener(banStorage, messages));

            server.getCommandManager().register(
                    server.getCommandManager().metaBuilder("sban").plugin(this).build(),
                    new SbanCommand(server, banStorage, messages)
            );
            server.getCommandManager().register(
                    server.getCommandManager().metaBuilder("sbanip").plugin(this).build(),
                    new SbanipCommand(server, banStorage, messages)
            );

            logger.info("StonePermatch: команды /sban и /sbanip зарегистрированы.");
        } catch (IOException e) {
            logger.error("StonePermatch: не удалось загрузить message.yml", e);
            throw new RuntimeException("Failed to load message.yml", e);
        }
    }
}
