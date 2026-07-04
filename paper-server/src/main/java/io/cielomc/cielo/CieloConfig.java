package io.cielomc.cielo;

import com.google.common.base.Throwables;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public final class CieloConfig {

    private static final Logger LOGGER = Logger.getLogger("Minecraft");
    private static final String FILE_NAME = "performance.yml";
    private static final String TEMPLATE = """
        # CieloMC performance configuration.
        # Controls region scheduling, chunk generation, entity ticking, IO, and chunk sending.
        performance:
          region:
            # Base region size in chunks.
            # 4 means 4x4 chunks per region.
            size: 4

            auto-split:
              enabled: true

              split:
                entity-threshold: 300
                tick-time-ms: 8
                apply: immediate-at-tick-end

              merge:
                entity-threshold: 120
                tick-time-ms: 3
                stable-duration-seconds: 60

            virtual-sub-region:
              enabled: true
              cell-size-blocks: 4
              detect-only-heavy-regions: true

          scheduler:
            mode: score
            rescore-interval-ticks: 20
            rescore-on-mspt-spike: true
            rescore-on-fast-player-movement: true

          chunk:
            generation:
              enabled: true
              worker-cpu-ratio: 0.5
              generation-load-ratio: "7:3"

            preload:
              enabled: true
              shape: circle
              extra-radius-min: 12
              extra-radius-max: 20
              priority: ungenerated-first

            commit:
              mode: dynamic
              priority: score

            sending:
              priority: nearest-first
              per-player-limit: dynamic
              global-limit: dynamic

          entity:
            enabled: true
            parallel-types:
              hostile-mobs: true
              items: true
              villagers: false

            hostile-mobs:
              region-worker-tick: true
              vanilla-like-near-players: true
              vanilla-like-special-mobs: true

              distance-tiers:
                enabled: true
                near:
                  max-block-distance: 32
                  mode: vanilla-like
                mid:
                  max-block-distance: 96
                  ai-interval: 2
                  pathfinding-interval: 2
                far:
                  min-block-distance: 97
                  ai-interval: 5
                  pathfinding-interval: 5

            items:
              parallel-tick: true
              spatial-index: true
              aggressive-merge-only-when-heavy: true

          io:
            disk-aware: true
            target-storage: sata-ssd

          save:
            mode: immediate-async
            io-control: os-disk-aware

          dimensions:
            mode: overworld-protected-burst
            overworld:
              min-share: 0.40
              burst-max: 0.80
            nether:
              min-share: 0.05
              burst-max: 0.45
            end:
              min-share: 0.05
              burst-max: 0.45
        """;

    private static File configFile;
    private static Values current;

    private CieloConfig() {
    }

    public static void init() {
        CieloConfig.configFile = new File(FILE_NAME);
        CieloConfig.writeDefaultConfigIfMissing();
        ReloadResult result = CieloConfig.reload();
        if (!result.success()) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not load performance.yml: {0}", result.error());
            throw Throwables.propagate(new IllegalStateException(result.error()));
        }
    }

    public static void initSettings() {
        CieloConfig.configFile = new File(FILE_NAME);
        CieloConfig.writeDefaultConfigIfMissing();
    }

    public static Values get() {
        if (CieloConfig.current == null) {
            CieloConfig.current = Values.defaults();
        }
        return CieloConfig.current;
    }

    public static ReloadResult reload() {
        if (CieloConfig.configFile == null) {
            CieloConfig.configFile = new File(FILE_NAME);
        }
        CieloConfig.writeDefaultConfigIfMissing();

        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(CieloConfig.configFile);
            Values parsed = Values.from(config);
            CieloConfig.current = parsed;
            return ReloadResult.success(parsed);
        } catch (IOException ex) {
            return ReloadResult.failure("Could not read performance.yml: " + ex.getMessage());
        } catch (InvalidConfigurationException ex) {
            return ReloadResult.failure("Invalid YAML in performance.yml: " + ex.getMessage());
        } catch (ConfigValidationException ex) {
            return ReloadResult.failure(ex.getMessage());
        }
    }

    private static void writeDefaultConfigIfMissing() {
        if (CieloConfig.configFile.isFile()) {
            return;
        }
        try {
            Files.writeString(CieloConfig.configFile.toPath(), TEMPLATE, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Could not create performance.yml", ex);
            throw Throwables.propagate(ex);
        }
    }

    public record ReloadResult(boolean success, Values values, String error) {

        public static ReloadResult success(final Values values) {
            return new ReloadResult(true, values, null);
        }

        public static ReloadResult failure(final String error) {
            return new ReloadResult(false, null, error);
        }
    }

    public record Values(
        int regionSize,
        boolean autoSplitEnabled,
        int splitEntityThreshold,
        double splitTickTimeMs,
        String splitApply,
        int mergeEntityThreshold,
        double mergeTickTimeMs,
        int mergeStableDurationSeconds,
        boolean virtualSubRegionEnabled,
        int virtualSubRegionCellSizeBlocks,
        boolean virtualSubRegionDetectOnlyHeavyRegions,
        String schedulerMode,
        int schedulerRescoreIntervalTicks,
        boolean rescoreOnMsptSpike,
        boolean rescoreOnFastPlayerMovement,
        boolean chunkGenerationEnabled,
        double workerCpuRatio,
        String generationLoadRatio,
        int generationRatio,
        int loadRatio,
        boolean preloadEnabled,
        String preloadShape,
        int preloadExtraRadiusMin,
        int preloadExtraRadiusMax,
        String preloadPriority,
        String commitMode,
        String commitPriority,
        String sendingPriority,
        String sendingPerPlayerLimit,
        String sendingGlobalLimit,
        boolean entityEnabled,
        boolean hostileMobsParallel,
        boolean itemsParallel,
        boolean villagersParallel,
        boolean hostileMobRegionWorkerTick,
        boolean hostileMobVanillaLikeNearPlayers,
        boolean hostileMobVanillaLikeSpecialMobs,
        boolean distanceTiersEnabled,
        int nearMaxBlockDistance,
        String nearMode,
        int midMaxBlockDistance,
        int midAiInterval,
        int midPathfindingInterval,
        int farMinBlockDistance,
        int farAiInterval,
        int farPathfindingInterval,
        boolean itemParallelTick,
        boolean itemSpatialIndex,
        boolean itemAggressiveMergeOnlyWhenHeavy,
        boolean diskAware,
        String targetStorage,
        String saveMode,
        String saveIoControl,
        String dimensionsMode,
        double overworldMinShare,
        double overworldBurstMax,
        double netherMinShare,
        double netherBurstMax,
        double endMinShare,
        double endBurstMax
    ) {

        public static Values defaults() {
            return new Values(
                4, true, 300, 8.0D, "immediate-at-tick-end", 120, 3.0D, 60,
                true, 4, true, "score", 20, true, true,
                true, 0.5D, "7:3", 7, 3, true, "circle", 12, 20, "ungenerated-first",
                "dynamic", "score", "nearest-first", "dynamic", "dynamic",
                true, true, true, false, true, true, true,
                true, 32, "vanilla-like", 96, 2, 2, 97, 5, 5,
                true, true, true, true, "sata-ssd", "immediate-async", "os-disk-aware",
                "overworld-protected-burst", 0.40D, 0.80D, 0.05D, 0.45D, 0.05D, 0.45D
            );
        }

        static Values from(final YamlConfiguration config) throws ConfigValidationException {
            final String root = "performance.";
            int regionSize = positiveInt(config, root + "region.size", 4);
            boolean villagersParallel = config.getBoolean(root + "entity.parallel-types.villagers", false);
            if (villagersParallel) {
                throw invalid(root + "entity.parallel-types.villagers", "true (villager parallel ticking is not supported in the MVP)");
            }

            String ratio = nonBlankString(config, root + "chunk.generation.generation-load-ratio", "7:3");
            int[] parsedRatio = parseRatio(root + "chunk.generation.generation-load-ratio", ratio);

            int preloadMin = nonNegativeInt(config, root + "chunk.preload.extra-radius-min", 12);
            int preloadMax = nonNegativeInt(config, root + "chunk.preload.extra-radius-max", 20);
            if (preloadMax < preloadMin) {
                throw invalid(root + "chunk.preload.extra-radius-max", Integer.toString(preloadMax));
            }

            double workerCpuRatio = boundedDouble(config, root + "chunk.generation.worker-cpu-ratio", 0.5D, 0.01D, 1.0D);
            double overworldMin = share(config, root + "dimensions.overworld.min-share", 0.40D);
            double overworldBurst = share(config, root + "dimensions.overworld.burst-max", 0.80D);
            double netherMin = share(config, root + "dimensions.nether.min-share", 0.05D);
            double netherBurst = share(config, root + "dimensions.nether.burst-max", 0.45D);
            double endMin = share(config, root + "dimensions.end.min-share", 0.05D);
            double endBurst = share(config, root + "dimensions.end.burst-max", 0.45D);
            validateBurst(root + "dimensions.overworld", overworldMin, overworldBurst);
            validateBurst(root + "dimensions.nether", netherMin, netherBurst);
            validateBurst(root + "dimensions.end", endMin, endBurst);

            return new Values(
                regionSize,
                config.getBoolean(root + "region.auto-split.enabled", true),
                nonNegativeInt(config, root + "region.auto-split.split.entity-threshold", 300),
                nonNegativeDouble(config, root + "region.auto-split.split.tick-time-ms", 8.0D),
                nonBlankString(config, root + "region.auto-split.split.apply", "immediate-at-tick-end"),
                nonNegativeInt(config, root + "region.auto-split.merge.entity-threshold", 120),
                nonNegativeDouble(config, root + "region.auto-split.merge.tick-time-ms", 3.0D),
                nonNegativeInt(config, root + "region.auto-split.merge.stable-duration-seconds", 60),
                config.getBoolean(root + "region.virtual-sub-region.enabled", true),
                positiveInt(config, root + "region.virtual-sub-region.cell-size-blocks", 4),
                config.getBoolean(root + "region.virtual-sub-region.detect-only-heavy-regions", true),
                nonBlankString(config, root + "scheduler.mode", "score"),
                positiveInt(config, root + "scheduler.rescore-interval-ticks", 20),
                config.getBoolean(root + "scheduler.rescore-on-mspt-spike", true),
                config.getBoolean(root + "scheduler.rescore-on-fast-player-movement", true),
                config.getBoolean(root + "chunk.generation.enabled", true),
                workerCpuRatio,
                ratio,
                parsedRatio[0],
                parsedRatio[1],
                config.getBoolean(root + "chunk.preload.enabled", true),
                nonBlankString(config, root + "chunk.preload.shape", "circle"),
                preloadMin,
                preloadMax,
                nonBlankString(config, root + "chunk.preload.priority", "ungenerated-first"),
                nonBlankString(config, root + "chunk.commit.mode", "dynamic"),
                nonBlankString(config, root + "chunk.commit.priority", "score"),
                nonBlankString(config, root + "chunk.sending.priority", "nearest-first"),
                nonBlankString(config, root + "chunk.sending.per-player-limit", "dynamic"),
                nonBlankString(config, root + "chunk.sending.global-limit", "dynamic"),
                config.getBoolean(root + "entity.enabled", true),
                config.getBoolean(root + "entity.parallel-types.hostile-mobs", true),
                config.getBoolean(root + "entity.parallel-types.items", true),
                false,
                config.getBoolean(root + "entity.hostile-mobs.region-worker-tick", true),
                config.getBoolean(root + "entity.hostile-mobs.vanilla-like-near-players", true),
                config.getBoolean(root + "entity.hostile-mobs.vanilla-like-special-mobs", true),
                config.getBoolean(root + "entity.hostile-mobs.distance-tiers.enabled", true),
                positiveInt(config, root + "entity.hostile-mobs.distance-tiers.near.max-block-distance", 32),
                nonBlankString(config, root + "entity.hostile-mobs.distance-tiers.near.mode", "vanilla-like"),
                positiveInt(config, root + "entity.hostile-mobs.distance-tiers.mid.max-block-distance", 96),
                positiveInt(config, root + "entity.hostile-mobs.distance-tiers.mid.ai-interval", 2),
                positiveInt(config, root + "entity.hostile-mobs.distance-tiers.mid.pathfinding-interval", 2),
                positiveInt(config, root + "entity.hostile-mobs.distance-tiers.far.min-block-distance", 97),
                positiveInt(config, root + "entity.hostile-mobs.distance-tiers.far.ai-interval", 5),
                positiveInt(config, root + "entity.hostile-mobs.distance-tiers.far.pathfinding-interval", 5),
                config.getBoolean(root + "entity.items.parallel-tick", true),
                config.getBoolean(root + "entity.items.spatial-index", true),
                config.getBoolean(root + "entity.items.aggressive-merge-only-when-heavy", true),
                config.getBoolean(root + "io.disk-aware", true),
                nonBlankString(config, root + "io.target-storage", "sata-ssd"),
                nonBlankString(config, root + "save.mode", "immediate-async"),
                nonBlankString(config, root + "save.io-control", "os-disk-aware"),
                nonBlankString(config, root + "dimensions.mode", "overworld-protected-burst"),
                overworldMin,
                overworldBurst,
                netherMin,
                netherBurst,
                endMin,
                endBurst
            );
        }

        public int configuredWorkerCount() {
            return Math.max(1, (int) Math.floor(Runtime.getRuntime().availableProcessors() * this.workerCpuRatio));
        }
    }

    private static int positiveInt(final YamlConfiguration config, final String path, final int defaultValue) throws ConfigValidationException {
        int value = config.getInt(path, defaultValue);
        if (value <= 0) {
            throw invalid(path, Integer.toString(value));
        }
        return value;
    }

    private static int nonNegativeInt(final YamlConfiguration config, final String path, final int defaultValue) throws ConfigValidationException {
        int value = config.getInt(path, defaultValue);
        if (value < 0) {
            throw invalid(path, Integer.toString(value));
        }
        return value;
    }

    private static double nonNegativeDouble(final YamlConfiguration config, final String path, final double defaultValue) throws ConfigValidationException {
        double value = config.getDouble(path, defaultValue);
        if (value < 0.0D || Double.isNaN(value)) {
            throw invalid(path, Double.toString(value));
        }
        return value;
    }

    private static double boundedDouble(final YamlConfiguration config, final String path, final double defaultValue, final double min, final double max) throws ConfigValidationException {
        double value = config.getDouble(path, defaultValue);
        if (value < min || value > max || Double.isNaN(value)) {
            throw invalid(path, Double.toString(value));
        }
        return value;
    }

    private static double share(final YamlConfiguration config, final String path, final double defaultValue) throws ConfigValidationException {
        return boundedDouble(config, path, defaultValue, 0.0D, 1.0D);
    }

    private static String nonBlankString(final YamlConfiguration config, final String path, final String defaultValue) throws ConfigValidationException {
        String value = config.getString(path, defaultValue);
        if (value == null || value.isBlank()) {
            throw invalid(path, String.valueOf(value));
        }
        return value;
    }

    private static int[] parseRatio(final String path, final String ratio) throws ConfigValidationException {
        String[] parts = ratio.toLowerCase(Locale.ROOT).split(":");
        if (parts.length != 2) {
            throw invalid(path, ratio);
        }
        try {
            int generation = Integer.parseInt(parts[0].trim());
            int load = Integer.parseInt(parts[1].trim());
            if (generation <= 0 || load <= 0) {
                throw invalid(path, ratio);
            }
            return new int[] {generation, load};
        } catch (NumberFormatException ex) {
            throw invalid(path, ratio);
        }
    }

    private static void validateBurst(final String path, final double min, final double burst) throws ConfigValidationException {
        if (burst < min) {
            throw invalid(path + ".burst-max", Double.toString(burst));
        }
    }

    private static ConfigValidationException invalid(final String path, final String value) {
        return new ConfigValidationException("Invalid setting: " + path + " = " + value);
    }

    private static final class ConfigValidationException extends Exception {
        private ConfigValidationException(final String message) {
            super(message);
        }
    }
}
