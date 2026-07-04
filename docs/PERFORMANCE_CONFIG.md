# performance.yml Reference

`performance.yml` is generated with comments on first startup and never
rewritten afterwards, so your comments survive upgrades.

Invalid values never crash the server:

- At startup, an invalid file logs SEVERE and built-in defaults are used.
- On `/perf reload` (or `/reload`), an invalid file keeps the previous valid
  configuration.

## Live-reloadable settings

Applied immediately by `/perf reload`:

- auto-split / merge thresholds (`region.auto-split.*`)
- rescore interval and triggers (`scheduler.rescore-*`)
- `chunk.generation.worker-cpu-ratio`, `generation-load-ratio`
- preload shape/radius/priority (`chunk.preload.*`)
- commit and sending policies (`chunk.commit.*`, `chunk.sending.*`)
- distance tiers and intervals (`entity.hostile-mobs.*`, `entity.items.*`)
- IO and save tuning (`io.*`, `save.io-control`)
- dimension shares (`dimensions.*`)

## Restart-required settings

Detected on reload; the previous value stays live and a warning lists the
deferred paths. The new value applies on the next boot:

| Path | Reason |
|---|---|
| `performance.region.size` | region layout is fixed at boot |
| `performance.region.virtual-sub-region.cell-size-blocks` | sub-region grid is fixed at boot |
| `performance.scheduler.mode` | scheduler implementation choice |
| `performance.chunk.generation.enabled` | worker pool existence |
| `performance.entity.enabled` | parallel tick infrastructure |
| `performance.entity.parallel-types.hostile-mobs` | parallel tick infrastructure |
| `performance.entity.parallel-types.items` | parallel tick infrastructure |
| `performance.save.mode` | save pipeline structure |

## Validation rules

- `region.size`, cell sizes, intervals, distances: positive integers
- thresholds, durations: non-negative
- `worker-cpu-ratio`: 0.01–1.0; worker count = `floor(cpus * ratio)`, min 1
- `generation-load-ratio`: `"G:L"` with both parts positive integers
- `preload.extra-radius-max` >= `extra-radius-min`
- dimension `min-share`/`burst-max`: 0.0–1.0, with `burst-max >= min-share`
- `entity.parallel-types.villagers: true` is rejected (not supported in the MVP)

## Commands

`/perf` requires the `cielo.command.perf` permission (default: OP).

- `/perf status` — TPS, 1m average MSPT, workers, queues, entity/IO/dimension summary
- `/perf reload` — reload with validation; reports restart-required changes
- `/perf workers`, `/perf queues`, `/perf regions`
- `/perf fallback` — list regions running on the main-thread fallback
- `/perf recover <all|region <worldKey>:<x>,<z>>` — clear fallback state
