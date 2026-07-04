# CieloMC

CieloMC
A survival-workload optimized Paper fork with a parallel region scheduler.

CieloMC is a high-performance Paper fork optimized for heavy survival servers: mob farms, item-heavy storage pipelines, resource-world exploration, elytra chunk loading, and high entity counts.

CieloMC is experimental.
It is not a drop-in replacement for Paper yet.
The first goal is performance for heavy survival workloads.

CieloMC is based on Paper, but plugin compatibility is not guaranteed.

Plugins that depend heavily on synchronous world, entity, or chunk behavior may not work correctly.

## Features

- Fast chunk generation and loading foundation
- Score-based region scheduler foundation
- Separate generation, load, save, commit, and sending queue model
- Hostile mob and item entity parallel tick roadmap
- Dynamic worker control roadmap
- `/perf status`
- `performance.yml`

## Status

- Experimental
- Based on Paper
- Plugin compatibility is not guaranteed
- Survival workload focused
- Villager, POI, bed, job-site, trade, and iron golem behavior is not optimized in the MVP

## Quick Start

Build:

```bash
./gradlew createPaperclipJar
```

Run:

```bash
java -jar paper-server/build/libs/paper-paperclip-*.jar nogui
```

The initial performance configuration is generated as:

```text
performance.yml
```

Use OP-only performance commands (permission node `cielo.command.perf`, default OP):

```text
/perf status
/perf reload
/perf workers
/perf queues
/perf fallback
/perf regions
/perf recover <all|region <worldKey>:<x>,<z>>
```

## Configuration

`performance.yml` controls Cielo's experimental performance systems.

Important initial settings:

- `performance.region.size`: base region size in chunks. The default is `4`, meaning `4x4` chunks.
- `performance.scheduler.mode`: score-based region scheduling mode.
- `performance.chunk.generation.worker-cpu-ratio`: target worker count as a CPU ratio. The default is `0.5`.
- `performance.chunk.generation.generation-load-ratio`: default generation/load worker split. The default is `7:3`.
- `performance.chunk.preload.extra-radius-min` and `extra-radius-max`: preload radius controls for exploration.
- `performance.entity.parallel-types.hostile-mobs`: hostile mob parallel tick target.
- `performance.entity.parallel-types.items`: item entity parallel tick target.
- `performance.entity.parallel-types.villagers`: always disabled in the MVP.
- `performance.dimensions.*`: dimension worker share policy.

`/perf reload` keeps the previous valid configuration if validation fails.
An invalid `performance.yml` at startup falls back to built-in defaults; the
server always boots.

Restart-required settings are detected on reload: the previous value stays
live and the deferred paths are reported. See
[docs/PERFORMANCE_CONFIG.md](docs/PERFORMANCE_CONFIG.md) for the full
reference.

Live reload target:

- MSPT thresholds
- worker limits
- generation/load ratio
- preload radius
- sending limits
- save control thresholds
- dimension priorities
- debug/status display

Restart-required target (detected and deferred on reload):

- `performance.region.size`
- `performance.region.virtual-sub-region.cell-size-blocks`
- `performance.scheduler.mode`
- `performance.chunk.generation.enabled`
- `performance.entity.enabled`
- `performance.entity.parallel-types.hostile-mobs`
- `performance.entity.parallel-types.items`
- `performance.save.mode`

## Roadmap

- Phase 0: Paper fork environment
- Phase 1: README, `performance.yml`, `/perf status`
- Phase 2: `RegionId`, `RegionManager`, `RegionScheduler` empty implementation
- Phase 3: queue foundation for generation/load/save/commit/sending
- Phase 4: chunk generation worker
- Phase 5: chunk commit control
- Phase 6: chunk sending priority control
- Phase 7: IO monitoring and Save Queue control
- Phase 8: dimension worker allocation
- Phase 9: item entity parallel tick
- Phase 10: hostile mob parallel tick
- Phase 11: region-distributed spawn/despawn
- Phase 12: fallback/recover
- Phase 13: virtual sub-region
- Phase 14: benchmark comparison
- Phase 15: villager optimization review

## For Developers

Initial Cielo code lives under:

```text
paper-server/src/main/java/io/cielomc/cielo
```

Current structure:

- `CieloConfig`: loads and validates `performance.yml` while preserving comments by avoiding unnecessary rewrites.
- `CieloRuntime`: owns Cielo's early runtime singletons.
- `RegionId`: maps chunks to configurable base regions.
- `RegionManager`: tracks fallback regions.
- `RegionScheduler`: empty scheduler foundation with status snapshots.
- `PerfCommand`: `/perf` command (permission `cielo.command.perf`, default OP).

More detail: [docs/DESIGN.md](docs/DESIGN.md),
[docs/ROADMAP.md](docs/ROADMAP.md),
[docs/PERFORMANCE_CONFIG.md](docs/PERFORMANCE_CONFIG.md), and
[AGENTS.md](AGENTS.md) for contribution rules.

Scheduler design:

- Common worker pool foundation
- Score-based priorities instead of fixed priority levels
- One task per region at a time in the MVP
- Future task compatibility rules per task type
- Starvation bonus for regions that wait too long

Queue design:

- Generation Queue
- Load Queue
- Save Queue
- Commit Queue
- Sending Queue

Contribution direction:

- Keep `main` bootable.
- Do normal development on `dev`.
- Keep phases small enough to review and build independently.
- Do not change villager behavior in the MVP.
- Mark unsafe or experimental behavior clearly in names or comments.
- Prefer main-thread fallback for crash-prone integration points.
