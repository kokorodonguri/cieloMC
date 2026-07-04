package io.cielomc.cielo.region;

import io.cielomc.cielo.CieloConfig;
import java.util.concurrent.atomic.AtomicReference;

public final class RegionScheduler {

    private final RegionManager regionManager;
    private final AtomicReference<CieloConfig.Values> config = new AtomicReference<>(CieloConfig.Values.defaults());

    public RegionScheduler(final RegionManager regionManager) {
        this.regionManager = regionManager;
    }

    public void reconfigure(final CieloConfig.Values values) {
        this.config.set(values);
    }

    public Status snapshot() {
        CieloConfig.Values values = this.config.get();
        return new Status(
            0,
            values.configuredWorkerCount(),
            0,
            0,
            "dynamic",
            0,
            0,
            0,
            0,
            0,
            this.regionManager.fallbackRegionCount(),
            0,
            0,
            0
        );
    }

    public record Status(
        int activeWorkers,
        int maxWorkers,
        int chunkGenerationWorkers,
        int chunkLoadWorkers,
        String saveWorkers,
        int generationQueue,
        int loadQueue,
        int saveQueue,
        int commitQueue,
        int sendingQueue,
        int fallbackRegions,
        int virtualSubRegions,
        int itemVirtualSubRegions,
        int hostileMobVirtualSubRegions
    ) {
    }
}
