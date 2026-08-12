# OverEnchant

Paper plugin (Minecraft 1.26.2) that lets an anvil push enchantments past their
vanilla max level.

Vanilla already turns "same enchant, same level" into level+1 on an anvil
(V + V = VI) - it just refuses to go past the enchantment's own max level. This
plugin removes that ceiling, per enchantment, up to a configurable cap:

```
Efficiency V + V  -> VI
VI + VI           -> VII
...
XIV + XIV         -> XV  (configurable max-effect: instant mine on any block)
```

## Config

See [`src/main/resources/config.yml`](src/main/resources/config.yml). Every
entry under `enchantments:` sets a `cap` (highest level obtainable via anvil)
and an optional `max-effect` (currently only `instamine`, used on Efficiency).
Levels above vanilla's own max cost extra XP, configurable via
`anvil.xp-cost-per-overlevel`.

If overenchanted merges show "Too Expensive!" for survival players, raise
`settings.anvil-max-repair-cost` in `spigot.yml` (or set it to `-1`).

## Commands

- `/overenchant reload` (`overenchant.admin`, default op) - reloads `config.yml`.

## Build

```
./gradlew jar
```

Produces `build/libs/OverEnchant-1.0.<n>.jar` (auto-incrementing build number).
