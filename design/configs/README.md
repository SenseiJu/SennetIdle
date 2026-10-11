# Config Schemas

Mock-ups of every config file the rewrite will use. Each file explains the
options, then shows example configs.

| Config | File(s) on the server | Status |
|---|---|---|
| [Resource](resource.md) | `resources/<id>.yml` | Draft |
| [Main config](config.md) | `config.yml` | Draft |

## Conventions

- All configs are YAML.
- Keys use `kebab-case`.
- Text uses MiniMessage, e.g. `<gold><b>Iron Sword`.
- Big numbers can be written plainly (`2500`) or with a suffix: K, M, B, T, Qa,
  Qi, Sx, Sp, Oc, No, Dc, each 1000x the last (`"2.5K"`, `"1.2M"`, `"3Qa"`).
  They always show with up to 2 decimal places, e.g. `1.25M`.
- An ID is the file name without `.yml`, using lowercase letters, digits and
  underscores only. IDs are saved in the database, so never rename one after
  launch. Change the display name instead.
- All files of a type go in one folder, sub-folders aren't read.
- A broken file is skipped and reported in the console, the rest still load.
- Everything can be reloaded with `/idleadmin reload`, no restart needed.

## Schema types

Schemas are written as a JSON-style block, with the type in place of a value
and `// required` or `// default X` plus a short note on each line.

| Type | Meaning | Example |
|---|---|---|
| `String` | Plain text | `"raw"` |
| `Text` | MiniMessage text | `"<gold><b>Iron Sword"` |
| `Int` | Whole number | `15` |
| `Decimal` | Number with decimals | `1.5` |
| `BigNumber` | Number, plain or with a suffix | `2500`, `"1.2M"` |
| `Percent` | Number from 0 to 100 | `15` |
| `Boolean` | true or false | `true` |
| `Duration` | Time with a unit (s, m, h, d) | `"30s"`, `"2h"` |
| `Material` | Minecraft item name | `OAK_PLANKS` |
| `Location` | world, x, y, z, yaw, pitch | see config.md |
| `ResourceId` | ID of a resource (its file name) | `wood` |
| `Currency` | A currency from the code list | `money`, `gems` |
| `List<X>` | List of X | `[money, gems]` |
| `Map<K, V>` | Keys of type K with values of type V | `{ wood: 10 }` |
| `"a" \| "b"` | One of these exact values | `"bosses"` |
