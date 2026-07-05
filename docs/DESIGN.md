# CieloMC Design

## Goals

Optimize heavy survival workloads: mob farms, item-heavy pipelines, elytra
exploration, chunk generation, and IO pressure. Performance over strict
safety, but crash-prone integration points keep a main-thread fallback.

## Code layout

All Cielo code lives under `paper-server/src/main/java/io/cielomc/cielo`.
Hooks into Paper internals are single lines marked with `// Cielo` comments:

- `Main`: generates `performance.yml` during `--initSettings`.
- `CraftServer` constructor: `CieloConfig.init()`, `CieloRuntime.init()`,
  `CieloViaBootstrap.init()`, and `/perf` registration.
- `CraftServer.reload()`: reloads `performance.yml`; failures are logged and
  the previous valid configuration is kept.

## Built-in ViaVersion and ViaBackwards

Cielo starts ViaVersion and ViaBackwards protocol engines as internal runtime
features under `io.cielomc.cielo.compat.via`; it does not install or load
plugin jars. `CieloViaBootstrap` initializes `ViaManagerImpl` with a Cielo-owned
`ViaPlatform` and ViaVersion's Paper/Bukkit Netty injector. ViaBackwards is
registered through ViaVersion's enable listener path so its protocol mappings
are added during ViaVersion startup.

The built-in engines store config in `config/cielo/viaversion/` and
`config/cielo/viabackwards/`. They can be disabled with
`-Dcielo.disableBuiltInViaVersion=true` or
`-Dcielo.disableBuiltInViaBackwards=true`, and each skips itself when a matching
plugin jar exists to avoid double injection.

## Configuration lifecycle

`CieloConfig` loads and validates `performance.yml`.

- The file is generated from a commented template only when missing; it is
  never rewritten, so user comments are preserved.
- Validation failure at startup falls back to built-in defaults with a SEVERE
  log. The server must always boot.
- Validation failure on reload keeps the previous valid configuration.
- **Restart-required settings** are detected on reload by diffing the parsed
  values against the live values. The live value is kept and the change is
  reported to the command sender and the log. See
  `CieloConfig.detectRestartRequiredChanges` and
  [PERFORMANCE_CONFIG.md](PERFORMANCE_CONFIG.md) for the list.

This guarantees long-lived runtime structures (region layout, worker pools,
save pipeline) never observe a config change they cannot apply live.

## Region model

- `RegionId`: world key + region coordinates. Chunks map to regions by
  `floorDiv(chunk, performance.region.size)`.
- `RegionManager`: thread-safe (`ConcurrentHashMap.newKeySet()`) tracker of
  regions that fell back to main-thread ticking. Recovery via
  `/perf recover`.
- `RegionScheduler`: foundation only. Holds an `AtomicReference` config
  snapshot so future worker threads read a consistent view. `snapshot()`
  must stay cheap: it only reads counters, never aggregates.

## Scheduler design (planned)

- Common worker pool sized by `worker-cpu-ratio`.
- Score-based priorities instead of fixed priority levels.
- One task per region at a time in the MVP.
- Task-type compatibility rules per region later.
- Starvation bonus for regions that wait too long.
- Queues: Generation, Load, Save, Commit, Sending.

## Diagnostics

All diagnostics go through `/perf` (OP-only, node `cielo.command.perf`).
`/perf status` reads pre-computed snapshots only; heavy aggregation must
happen on scheduler ticks, never at command execution time. Avoid adding
log spam; prefer `/perf` output.
