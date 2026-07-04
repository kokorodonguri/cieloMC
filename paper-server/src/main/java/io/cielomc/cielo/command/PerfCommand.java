package io.cielomc.cielo.command;

import ca.spottedleaf.moonrise.common.time.TickData;
import io.cielomc.cielo.CieloConfig;
import io.cielomc.cielo.CieloRuntime;
import io.cielomc.cielo.region.RegionId;
import io.cielomc.cielo.region.RegionScheduler;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public final class PerfCommand extends Command {

    private static final ThreadLocal<DecimalFormat> ONE_DECIMAL = ThreadLocal.withInitial(() -> new DecimalFormat("########0.0"));

    public PerfCommand(final String name) {
        super(name);
        this.description = "Cielo performance scheduler status and controls";
        this.usageMessage = "/perf <status|reload|workers|queues|fallback|regions>";
        this.setPermission("cielo.command.perf");
    }

    @Override
    public boolean execute(final CommandSender sender, final String commandLabel, final String[] args) {
        if (!this.testPermission(sender)) {
            return true;
        }

        if (args.length == 0 || "status".equalsIgnoreCase(args[0])) {
            this.sendStatus(sender);
            return true;
        }

        switch (args[0].toLowerCase(java.util.Locale.ROOT)) {
            case "reload" -> this.reload(sender);
            case "workers" -> this.sendWorkers(sender);
            case "queues" -> this.sendQueues(sender);
            case "fallback" -> this.sendFallback(sender);
            case "regions" -> this.sendRegions(sender);
            case "recover" -> this.recover(sender, args);
            default -> sender.sendMessage("Usage: " + this.usageMessage);
        }
        return true;
    }

    @Override
    public List<String> tabComplete(final CommandSender sender, final String alias, final String[] args, final Location location) {
        if (!sender.hasPermission("cielo.command.perf")) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            return List.of("status", "reload", "workers", "queues", "fallback", "regions", "recover");
        }
        if (args.length == 2 && "fallback".equalsIgnoreCase(args[0])) {
            return List.of("list");
        }
        if (args.length == 2 && "recover".equalsIgnoreCase(args[0])) {
            return List.of("region", "all");
        }
        return Collections.emptyList();
    }

    private void reload(final CommandSender sender) {
        CieloConfig.ReloadResult result = CieloConfig.reload();
        if (!result.success()) {
            sender.sendMessage("/perf reload failed");
            sender.sendMessage("Keeping previous valid configuration.");
            sender.sendMessage(result.error());
            return;
        }
        CieloRuntime.reconfigure();
        sender.sendMessage("/perf reload complete");
    }

    private void sendStatus(final CommandSender sender) {
        CieloConfig.Values config = CieloConfig.get();
        RegionScheduler.Status status = CieloRuntime.regionScheduler().snapshot();
        sender.sendMessage("TPS: " + ONE_DECIMAL.get().format(Bukkit.getTPS()[0]));
        sender.sendMessage("MSPT avg 1m: " + ONE_DECIMAL.get().format(this.averageMspt1m()) + "ms");
        sender.sendMessage("");
        this.sendWorkers(sender, status);
        sender.sendMessage("");
        this.sendQueues(sender, status);
        sender.sendMessage("");
        sender.sendMessage("Entity:");
        sender.sendMessage("  hostile mobs parallel: " + enabled(config.entityEnabled() && config.hostileMobsParallel()));
        sender.sendMessage("  items parallel: " + enabled(config.entityEnabled() && config.itemsParallel()));
        sender.sendMessage("  villagers parallel: disabled");
        sender.sendMessage("  fallback regions: " + status.fallbackRegions());
        sender.sendMessage("  virtual sub-regions: " + status.virtualSubRegions());
        sender.sendMessage("");
        sender.sendMessage("IO:");
        sender.sendMessage("  disk status: unmonitored");
        sender.sendMessage("  avg save latency: n/a");
        sender.sendMessage("");
        sender.sendMessage("Dimension:");
        sender.sendMessage("  overworld: " + percent(config.overworldMinShare()) + " min / " + percent(config.overworldBurstMax()) + " burst");
        sender.sendMessage("  nether: " + percent(config.netherMinShare()) + " min / " + percent(config.netherBurstMax()) + " burst");
        sender.sendMessage("  end: " + percent(config.endMinShare()) + " min / " + percent(config.endBurstMax()) + " burst");
    }

    private void sendWorkers(final CommandSender sender) {
        this.sendWorkers(sender, CieloRuntime.regionScheduler().snapshot());
    }

    private void sendWorkers(final CommandSender sender, final RegionScheduler.Status status) {
        sender.sendMessage("Workers:");
        sender.sendMessage("  active: " + status.activeWorkers() + " / " + status.maxWorkers());
        sender.sendMessage("  chunk generation: " + status.chunkGenerationWorkers());
        sender.sendMessage("  chunk load: " + status.chunkLoadWorkers());
        sender.sendMessage("  save: " + status.saveWorkers());
    }

    private void sendQueues(final CommandSender sender) {
        this.sendQueues(sender, CieloRuntime.regionScheduler().snapshot());
    }

    private void sendQueues(final CommandSender sender, final RegionScheduler.Status status) {
        sender.sendMessage("Queues:");
        sender.sendMessage("  generation: " + status.generationQueue());
        sender.sendMessage("  load: " + status.loadQueue());
        sender.sendMessage("  save: " + status.saveQueue());
        sender.sendMessage("  commit: " + status.commitQueue());
        sender.sendMessage("  sending: " + status.sendingQueue());
    }

    private void sendFallback(final CommandSender sender) {
        sender.sendMessage("Fallback regions:");
        if (CieloRuntime.regionManager().fallbackRegions().isEmpty()) {
            sender.sendMessage("  none");
            return;
        }
        for (RegionId regionId : CieloRuntime.regionManager().fallbackRegions()) {
            sender.sendMessage("  " + regionId.asCompactString());
        }
    }

    private void sendRegions(final CommandSender sender) {
        CieloConfig.Values config = CieloConfig.get();
        RegionScheduler.Status status = CieloRuntime.regionScheduler().snapshot();
        sender.sendMessage("Regions:");
        sender.sendMessage("  size: " + config.regionSize() + "x" + config.regionSize() + " chunks");
        sender.sendMessage("  scheduler mode: " + config.schedulerMode());
        sender.sendMessage("  fallback regions: " + status.fallbackRegions());
        sender.sendMessage("  active virtual sub-regions: " + status.virtualSubRegions());
        sender.sendMessage("    item: " + status.itemVirtualSubRegions());
        sender.sendMessage("    hostile-mob: " + status.hostileMobVirtualSubRegions());
    }

    private void recover(final CommandSender sender, final String[] args) {
        if (args.length == 2 && "all".equalsIgnoreCase(args[1])) {
            int recovered = CieloRuntime.regionManager().recoverAll();
            sender.sendMessage("Recovered fallback regions: " + recovered);
            return;
        }
        if (args.length == 3 && "region".equalsIgnoreCase(args[1])) {
            RegionId.parseCompactString(args[2]).ifPresentOrElse(
                regionId -> sender.sendMessage(CieloRuntime.regionManager().recover(regionId)
                    ? "Recovered fallback region: " + regionId.asCompactString()
                    : "Region is not in fallback: " + regionId.asCompactString()),
                () -> sender.sendMessage("Invalid region id. Expected <worldKey>:<regionX>,<regionZ>")
            );
            return;
        }
        sender.sendMessage("Usage: /perf recover <all|region <regionId>>");
    }

    private double averageMspt1m() {
        MinecraftServer server = MinecraftServer.getServer();
        TickData.TickReportData reportData = server.tickTimes1m.generateTickReport(null, System.nanoTime(), server.tickRateManager().nanosecondsPerTick());
        return reportData == null ? 0.0D : reportData.timePerTickData().segmentAll().average() * 1.0E-6D;
    }

    private static String enabled(final boolean enabled) {
        return enabled ? "enabled" : "disabled";
    }

    private static String percent(final double share) {
        return ONE_DECIMAL.get().format(share * 100.0D) + "%";
    }
}
