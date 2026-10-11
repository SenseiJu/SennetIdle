# Main Config

The server-wide settings: the database connection, saving, where the fight
happens, and the global game numbers like CPS cap, boss timer and market tax.
Game content (resources, blueprints, heroes, boxes, prestige levels,
abilities) has its own files and is never set here. Currencies are defined in
code, not in config.

**Location:** `plugins/IdleMobCore/config.yml`, one file. It's created with all
defaults on first start.

Durations are written as a number with a unit: `s` (seconds), `m` (minutes),
`h` (hours), `d` (days), e.g. `30s`, `10m`, `2h`, `1h30m`.

Locations are written as a block:
```yaml
world: idle
x: 0.5
y: 64
z: 12.5
yaw: 180    # optional, default 0
pitch: 0    # optional, default 0
```

## Schema

Saving on quit and on shutdown always happens, and currency and tradeable
things (tradeable resources, boxes, blueprints) always save instantly.
`auto-save-interval` only controls the regular save of everything else.

Mob health = `base-mob-health` x `health-growth` ^ stage. Boss health = that
stage's mob health x `boss-health-multiplier`. Ranks and prestige perks add on
top of the `player-attack`, `offline-rewards` and `heroes` values.

```jsonc
{
  "database": {                       // required, MariaDB connection (restart to change)
    "host": String,                   // default "localhost"
    "port": Int,                      // default 3306
    "database": String,               // default "idlemob", created if missing
    "username": String,               // required
    "password": String,               // required
    "pool": {
      "maximum-pool-size": Int,       // default 10, most connections open at once
      "minimum-idle": Int             // default 10, kept open while quiet, can't be above the max
    },
    "properties": Map<String, String> // optional, extra MariaDB options e.g. useSsl: "true"
  },

  "auto-save-interval": Duration,     // default "30s", regular save of online players' progress

  "fight-zone": {                     // required, where players fight their mob
    "mob-location": Location,         // required, where each player's personal mob stands
    "region": String                  // required, WorldGuard region ID in the mob-location's world.
                                      // Inside it players get the combat inventory and can't see each other
  },

  "player-attack": {
    "base-dps": Decimal,              // default 10, damage per second before resources, heroes and boosters
    "base-cps-limit": Int,            // default 10, clicks per second that deal damage, before rank and prestige bonuses
    "click-damage-percent": Percent   // default 15, damage per click as % of DPS
  },

  "stages": {
    "waves-per-stage": Int,           // default 15, the last wave is the boss
    "base-boss-time-limit": Duration, // default "30s", before bonuses, then back to the start of the stage
    "base-mob-health": BigNumber,     // default 100 (placeholder), health before growth
    "health-growth": Decimal,         // default 1.5, health multiplier per stage
    "boss-health-multiplier": Decimal, // default 8, boss health vs a normal mob of that stage
    "mob-types": List<EntityType>,    // default zombie, skeleton, spider and more, picked at random each wave
    "boss-types": List<EntityType>    // default [IRON_GOLEM, WARDEN], picked at random each boss
  },

  "offline-rewards": {
    "required-offline-time": Duration, // default "10m", how long a player must be offline before they can collect rewards
    "base-max-time": Duration,        // default "2h", most offline time a player can bank, ranks, vouchers and upgrades add to it
    "offline-damage-percent": Percent, // default 25 (placeholder), % of the player's DPS used to simulate kills
                                      // on wave 1 of their current stage while they were offline
    "recharge": {                     // offline time given back while playing
      "percent": Percent,             // default 1, % of max time given back...
      "every": Duration               // default "2m", ...per this long online
    }
  },

  "generators": {
    "slots": List<Location>           // default [], display location per generator, in unlock order.
                                      // The list length is the most generators a player can unlock
  },

  "heroes": {
    "slots": List<Location>,          // default [], where each hero NPC stands, in unlock order.
                                      // The list length is the most hero slots a player can have
    "xp-slots": Int                   // default 1, active heroes earning XP at once
  },

  "market": {
    "tax-percent": Percent            // default 5 (placeholder), taken from the seller on every
                                      // resource market and auction house sale, removed from the game
  },

  "leaderboards": {
    "update-interval": Duration       // default "5m", refresh of leaderboards and their placeholders
  }
}
```

## Examples

### Minimal (only the required keys)

```yaml
database:
  username: idlemob
  password: "change-me"

fight-zone:
  mob-location: { world: idle, x: 0.5, y: 64, z: 12.5, yaw: 180 }
  region: fight-zone
```

### Full default (every key with its default)

```yaml
database:
  host: localhost
  port: 3306
  database: idlemob
  username: idlemob
  password: "change-me"
  pool:
    maximum-pool-size: 10
    minimum-idle: 10
  properties: {}

auto-save-interval: 30s

fight-zone:
  mob-location: { world: idle, x: 0.5, y: 64, z: 12.5, yaw: 180, pitch: 0 }
  region: fight-zone

player-attack:
  base-dps: 10
  base-cps-limit: 10
  click-damage-percent: 15

stages:
  waves-per-stage: 15
  base-boss-time-limit: 30s
  base-mob-health: 100
  health-growth: 1.5
  boss-health-multiplier: 8
  mob-types: [ZOMBIE, SKELETON, SPIDER, CREEPER, BLAZE, ENDERMAN, PILLAGER, VINDICATOR, EVOKER, ZOMBIFIED_PIGLIN]
  boss-types: [IRON_GOLEM, WARDEN]

offline-rewards:
  required-offline-time: 10m
  base-max-time: 2h
  offline-damage-percent: 25
  recharge:
    percent: 1
    every: 2m

generators:
  slots: []

heroes:
  slots: []
  xp-slots: 1

market:
  tax-percent: 5

leaderboards:
  update-interval: 5m
```

### Production (remote database, tuned values)

```yaml
database:
  host: db.internal.example.net
  port: 3306
  database: idlemob_live
  username: idlemob_live
  password: "a-long-random-password"
  pool:
    maximum-pool-size: 20
    minimum-idle: 20
  properties:
    useSsl: "true"
    serverSslCert: "/etc/ssl/db-ca.pem"

auto-save-interval: 1m

fight-zone:
  mob-location: { world: idle, x: 0.5, y: 64, z: 12.5, yaw: 180 }
  region: idle-arena

player-attack:
  base-cps-limit: 6
  click-damage-percent: 12

stages:
  waves-per-stage: 20
  base-boss-time-limit: 45s
  base-mob-health: 250
  health-growth: 1.45
  boss-health-multiplier: 10

offline-rewards:
  required-offline-time: 15m
  base-max-time: 3h
  offline-damage-percent: 20

generators:
  slots:
    - { world: idle, x: -6.5, y: 64, z: 2.5, yaw: 90 }
    - { world: idle, x: -6.5, y: 64, z: 6.5, yaw: 90 }
    - { world: idle, x: -6.5, y: 64, z: 10.5, yaw: 90 }
    - { world: idle, x: 7.5, y: 64, z: 2.5, yaw: -90 }
    - { world: idle, x: 7.5, y: 64, z: 6.5, yaw: -90 }
    - { world: idle, x: 7.5, y: 64, z: 10.5, yaw: -90 }

market:
  tax-percent: 7.5

leaderboards:
  update-interval: 2m
```

## Rules the plugin checks on load

- If the database can't be reached on startup, the plugin stops and the
  server doesn't let players join. Players are never let in without saving.
- `database` changes need a restart. Everything else reloads with
  `/idleadmin reload`.
- Every world named in a location must exist.
- `region` must be a WorldGuard region in the same world as `mob-location`,
  and `mob-location` must be inside it.
- Numbers must be above zero, and percents between `0` and `100`.
  `health-growth` and `boss-health-multiplier` must be at least `1`.
- `minimum-idle` can't be higher than `maximum-pool-size`.
- `xp-slots` can't be higher than the number of hero `slots`.
- If `generators.slots` is empty, generators are turned off and a warning is
  shown in the console.
- Removing generator slots on reload never deletes a player's generator or
  its blueprint, it just isn't shown until the slot is added back.
- A broken or missing value is listed in the console and its default is used,
  except for required keys.
