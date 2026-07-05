package io.cielomc.cielo.compat.via;

import com.viaversion.viabackwards.api.ViaBackwardsPlatform;
import java.io.File;
import java.util.logging.Logger;
import org.bukkit.Server;

final class CieloViaBackwardsPlatform implements ViaBackwardsPlatform {

    private final Server server;
    private final File dataFolder = new File("config/cielo/viabackwards");
    private boolean disabled;

    CieloViaBackwardsPlatform(final Server server) {
        this.server = server;
    }

    @Override
    public Logger getLogger() {
        return this.server.getLogger();
    }

    @Override
    public void disable() {
        this.disabled = true;
    }

    @Override
    public File getDataFolder() {
        return this.dataFolder;
    }

    boolean disabled() {
        return this.disabled;
    }
}
