package io.cielomc.cielo;

import io.cielomc.cielo.region.RegionManager;
import io.cielomc.cielo.region.RegionScheduler;

public final class CieloRuntime {

    private static final RegionManager REGION_MANAGER = new RegionManager();
    private static final RegionScheduler REGION_SCHEDULER = new RegionScheduler(REGION_MANAGER);

    private CieloRuntime() {
    }

    public static void init() {
        REGION_SCHEDULER.reconfigure(CieloConfig.get());
    }

    public static void reconfigure() {
        REGION_SCHEDULER.reconfigure(CieloConfig.get());
    }

    public static RegionManager regionManager() {
        return REGION_MANAGER;
    }

    public static RegionScheduler regionScheduler() {
        return REGION_SCHEDULER;
    }
}
