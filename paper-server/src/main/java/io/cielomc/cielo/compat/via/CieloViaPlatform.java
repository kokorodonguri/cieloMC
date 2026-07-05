package io.cielomc.cielo.compat.via;

import com.viaversion.viaversion.api.ViaAPI;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.platform.ViaPlatform;
import com.viaversion.viaversion.bukkit.platform.BukkitViaAPI;
import com.viaversion.viaversion.bukkit.platform.BukkitViaConfig;
import com.viaversion.viaversion.libs.gson.JsonObject;
import java.io.File;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;

final class CieloViaPlatform implements ViaPlatform<Player> {

    private final Server server;
    private final File dataFolder = new File("config/cielo/viaversion");
    private final BukkitViaConfig config;
    private final ViaAPI<Player> api;

    CieloViaPlatform(final Server server) {
        this.server = server;
        this.config = new BukkitViaConfig(this.dataFolder, this.getLogger());
        this.api = new BukkitViaAPI();
    }

    @Override
    public Logger getLogger() {
        return this.server.getLogger();
    }

    @Override
    public String getPlatformName() {
        return "CieloMC";
    }

    @Override
    public String getPlatformVersion() {
        return this.server.getVersion();
    }

    @Override
    public String getPluginVersion() {
        return "5.10.0";
    }

    @Override
    public ViaAPI<Player> getApi() {
        return this.api;
    }

    @Override
    public BukkitViaConfig getConf() {
        return this.config;
    }

    @Override
    public File getDataFolder() {
        return this.dataFolder;
    }

    @Override
    public void sendMessage(final UserConnection connection, final String message) {
        Player player = this.player(connection);
        if (player != null) {
            player.sendMessage(message);
        }
    }

    @Override
    public boolean kickPlayer(final UserConnection connection, final String message) {
        Player player = this.player(connection);
        if (player == null) {
            return false;
        }
        player.kickPlayer(message);
        return true;
    }

    @Override
    public JsonObject getDump() {
        JsonObject dump = new JsonObject();
        dump.addProperty("embedded", true);
        dump.addProperty("platform", "CieloMC");
        return dump;
    }

    @Override
    public boolean hasPlugin(final String name) {
        return Bukkit.getPluginManager().getPlugin(name) != null;
    }

    private Player player(final UserConnection connection) {
        UUID uuid = connection.getProtocolInfo().getUuid();
        return uuid == null ? null : Bukkit.getPlayer(uuid);
    }
}
