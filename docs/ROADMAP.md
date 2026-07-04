# CieloMC Roadmap

CieloMC is built in small, independently buildable phases. Each phase must keep
a stock Paper server bootable.

## Phases

- **Phase 0**: Paper fork environment — done
- **Phase 1**: README, `performance.yml`, `/perf status` — done
- **Phase 2**: `RegionId`, `RegionManager`, `RegionScheduler` empty implementation — done
- **Phase 3**: queue foundation for generation/load/save/commit/sending
- **Phase 4**: chunk generation worker
- **Phase 5**: chunk commit control
- **Phase 6**: chunk sending priority control
- **Phase 7**: IO monitoring and Save Queue control
- **Phase 8**: dimension worker allocation
- **Phase 9**: item entity parallel tick
- **Phase 10**: hostile mob parallel tick
- **Phase 11**: region-distributed spawn/despawn
- **Phase 12**: fallback/recover
- **Phase 13**: virtual sub-region
- **Phase 14**: benchmark comparison
- **Phase 15**: villager optimization review

## Out of scope for the MVP

- Villager ticking, POI, trades, beds, job sites, iron golem spawning
- Paper plugin compatibility guarantees
- Any change that risks breaking stock Paper startup
