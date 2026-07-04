# Agent Guidelines for CieloMC

CieloMC is a Paper fork optimized for heavy survival workloads.

## Branch rules

- Work on `dev`. Never commit directly to `main`.
- Keep `main` bootable at all times.

## Change rules

- Keep changes small; one reviewable unit at a time.
- Read the existing code before proposing optimizations; explain what is slow
  and why before changing it.
- Keep Cielo code under `paper-server/src/main/java/io/cielomc/cielo`.
- Touch Paper internals minimally; mark every hook with a `// Cielo` comment.
- Do not break stock Paper startup. Plugin compatibility is not guaranteed in
  the MVP, but booting is.
- Mark unsafe/experimental behavior clearly in names or comments.
- Keep a main-thread fallback for crash-prone parallel paths.

## Off-limits in the MVP

- Villager ticking, POI, trades, beds, job sites, iron golem spawning.

## Config and diagnostics

- `performance.yml` is generated from the template in `CieloConfig` when
  missing and never rewritten (preserves user comments).
- Reload failures must keep the previous valid configuration; startup
  failures fall back to defaults. Never crash on bad config.
- Restart-required settings must be detected on reload, kept at their live
  value, and reported (see `CieloConfig.detectRestartRequiredChanges`).
- All diagnostics go through `/perf` (permission `cielo.command.perf`,
  default OP). `/perf status` must only read cheap snapshots; never run heavy
  aggregation inside a command. Do not add log spam.

## Verification

- Build check: `./gradlew :paper-server:compileJava`
- Full server jar: `./gradlew createPaperclipJar`

## Docs to keep in sync

- `README.md`, `docs/ROADMAP.md`, `docs/DESIGN.md`,
  `docs/PERFORMANCE_CONFIG.md`
