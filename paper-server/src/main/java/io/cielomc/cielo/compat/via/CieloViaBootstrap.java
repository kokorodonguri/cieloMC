package io.cielomc.cielo.compat.via;

import com.viaversion.viaversion.ViaManagerImpl;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.platform.ViaPlatformLoader;
import com.viaversion.viaversion.bukkit.platform.BukkitViaInjector;
import com.viaversion.viaversion.commands.ViaCommandHandler;
import java.io.File;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Server;

public final class CieloViaBootstrap {

    private static final Logger LOGGER = Logger.getLogger("Minecraft");
    private static final String DISABLE_PROPERTY = "cielo.disableBuiltInViaVersion";
    private static final String DISABLE_BACKWARDS_PROPERTY = "cielo.disableBuiltInViaBackwards";
    private static boolean initialized;

    private CieloViaBootstrap() {
    }

    public static synchronized void init(final Server server, final File pluginDirectory) {
        if (initialized) {
            return;
        }
        initialized = true;

        if (Boolean.getBoolean(DISABLE_PROPERTY)) {
            LOGGER.info("Cielo built-in ViaVersion disabled by -" + DISABLE_PROPERTY + "=true");
            return;
        }
        if (hasViaVersionPlugin(pluginDirectory)) {
            LOGGER.warning("Cielo built-in ViaVersion skipped because a ViaVersion plugin jar is present.");
            return;
        }

        try {
            CieloViaPlatform platform = new CieloViaPlatform(server);
            CieloViaBackwardsPlatform backwards = createBackwardsPlatform(server, pluginDirectory);
            ViaManagerImpl.initAndLoad(
                platform,
                new BukkitViaInjector(),
                new ViaCommandHandler(false),
                ViaPlatformLoader.NOOP,
                () -> initBackwards(backwards)
            );
            enableBackwards(backwards);
            LOGGER.info("Cielo built-in ViaVersion protocol engine is enabled.");
        } catch (Throwable ex) {
            LOGGER.log(Level.SEVERE, "Could not enable Cielo built-in ViaVersion; continuing without protocol translation", ex);
            try {
                if (Via.getManager() instanceof ViaManagerImpl manager) {
                    manager.destroy();
                }
            } catch (Throwable destroyEx) {
                LOGGER.log(Level.WARNING, "Could not clean up failed Cielo built-in ViaVersion initialization", destroyEx);
            }
        }
    }

    private static boolean hasViaVersionPlugin(final File pluginDirectory) {
        return hasPluginJar(pluginDirectory, "viaversion");
    }

    private static boolean hasViaBackwardsPlugin(final File pluginDirectory) {
        return hasPluginJar(pluginDirectory, "viabackwards");
    }

    private static boolean hasPluginJar(final File pluginDirectory, final String prefix) {
        File[] jars = pluginDirectory.listFiles((dir, name) -> {
            String lower = name.toLowerCase(Locale.ROOT);
            return lower.startsWith(prefix) && lower.endsWith(".jar");
        });
        return jars != null && jars.length > 0;
    }

    private static CieloViaBackwardsPlatform createBackwardsPlatform(final Server server, final File pluginDirectory) {
        if (Boolean.getBoolean(DISABLE_BACKWARDS_PROPERTY)) {
            LOGGER.info("Cielo built-in ViaBackwards disabled by -" + DISABLE_BACKWARDS_PROPERTY + "=true");
            return null;
        }
        if (hasViaBackwardsPlugin(pluginDirectory)) {
            LOGGER.warning("Cielo built-in ViaBackwards skipped because a ViaBackwards plugin jar is present.");
            return null;
        }
        return new CieloViaBackwardsPlatform(server);
    }

    private static void initBackwards(final CieloViaBackwardsPlatform platform) {
        if (platform == null) {
            return;
        }
        platform.init(new File(platform.getDataFolder(), "config.yml"));
    }

    private static void enableBackwards(final CieloViaBackwardsPlatform platform) {
        if (platform == null || platform.disabled()) {
            return;
        }
        platform.enable();
        LOGGER.info("Cielo built-in ViaBackwards protocol engine is enabled.");
    }
}
