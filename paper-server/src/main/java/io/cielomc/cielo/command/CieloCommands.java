package io.cielomc.cielo.command;

import net.minecraft.server.MinecraftServer;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.util.permissions.DefaultPermissions;

public final class CieloCommands {

    public static final String PERF_PERMISSION = "cielo.command.perf";

    private CieloCommands() {
    }

    public static void registerCommands(final MinecraftServer server) {
        // OP-only by default; server owners can grant the node through a permission plugin.
        DefaultPermissions.registerPermission(PERF_PERMISSION, "Allows the use of the /perf command", PermissionDefault.OP);
        server.server.getCommandMap().register("perf", "Cielo", new PerfCommand("perf"));
    }
}
