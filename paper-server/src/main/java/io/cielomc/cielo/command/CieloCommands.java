package io.cielomc.cielo.command;

import net.minecraft.server.MinecraftServer;

public final class CieloCommands {

    private CieloCommands() {
    }

    public static void registerCommands(final MinecraftServer server) {
        server.server.getCommandMap().register("perf", "Cielo", new PerfCommand("perf"));
    }
}
