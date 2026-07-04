package io.cielomc.cielo.region;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class RegionManager {

    private final Set<RegionId> fallbackRegions = ConcurrentHashMap.newKeySet();

    public void markFallback(final RegionId regionId) {
        this.fallbackRegions.add(regionId);
    }

    public boolean recover(final RegionId regionId) {
        return this.fallbackRegions.remove(regionId);
    }

    public int recoverAll() {
        int count = this.fallbackRegions.size();
        this.fallbackRegions.clear();
        return count;
    }

    public int fallbackRegionCount() {
        return this.fallbackRegions.size();
    }

    public Set<RegionId> fallbackRegions() {
        return Collections.unmodifiableSet(this.fallbackRegions);
    }
}
