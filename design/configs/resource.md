# Resource

Everything a player collects, crafts or owns. Wood, a blade and a sword are
all resources, they just have different components attached.

**Location:** `plugins/IdleMobCore/resources/<id>.yml`, one resource per file.

## Schema

Add only the components a resource needs. A resource with no components is
just a collectable.

```jsonc
{
  "display": {                        // required, how it looks in menus
    "name": Text,                     // required, MiniMessage display name
    "material": Material,             // required, base item e.g. OAK_PLANKS
    "item-model": String              // required, model key, vanilla (minecraft:oak_planks) or custom (idlemob:iron_blade)
  },
  "sort-order": Int,                  // default 0, lower shows first in menus

  "unlock": {                         // optional, locked until BOTH are reached
    "stage": Int,                     // default 1
    "prestige": Int                   // default 0
  },

  "components": {                     // required
    "damaging": {                     // adds DPS for every one owned
      "dps": BigNumber,               // required, DPS per resource owned
      "target": "all" | "mobs" | "bosses"  // default "all"
    },
    "droppable": {                    // drops from mobs and/or bosses
      "mob": Drop,                    // optional
      "boss": Drop                    // optional
    },
    "craftable": {                    // can be crafted from other resources
      "ingredients": Map<ResourceId, Int>,  // required, used per craft
      "output": Int                   // default 1, made per craft
    },
    "tradeable": {                    // can be sold on the resource market for any currency
      "enabled": Boolean              // default true, set false to turn it off without deleting the section
    },
    "blueprint": {                    // can be made by a generator from a blueprint of this resource
      "craft-time": Duration,         // default 1m, how long one craft takes
      "amount": Int                   // default 1, made per craft
    }
  }
}

// Drop
{
  "chance": Percent,                  // required, 0-100 per kill
  "amount": {                         // how many drop per kill
    "min": Int,                       // default 1
    "max": Int                        // default 1
  },
  "stage-bonus": Percent              // default 0, extra amount per stage past the unlock stage
}
```

## Examples

### Raw resource (drops from every mob)

`resources/wood.yml`
```yaml
display:
  name: "<#C69C6D><b>Wood"
  material: OAK_PLANKS
  item-model: minecraft:oak_planks
sort-order: 1

components:
  droppable:
    mob:
      chance: 100
      amount:
        min: 2
        max: 5
      stage-bonus: 10
  tradeable:
    enabled: true
```

### Rare raw resource (locked behind prestige, boss drop)

`resources/nebula.yml`
```yaml
display:
  name: "<gradient:#7F5AF0:#2CB1BC><b>Nebula"
  material: AMETHYST_SHARD
  item-model: idlemob:nebula
sort-order: 8

unlock:
  stage: 40
  prestige: 3

components:
  droppable:
    boss:
      chance: 15
  tradeable:
    enabled: true
```

### Crafted part (no damage, only used in recipes)

`resources/wooden_blade.yml`
```yaml
display:
  name: "<#C69C6D><b>Wooden Blade"
  material: STICK
  item-model: idlemob:wooden_blade

components:
  craftable:
    ingredients:
      wood: 10
```

### Weapon (crafted, adds DPS)

`resources/wooden_sword.yml`
```yaml
display:
  name: "<#C69C6D><b>Wooden Sword"
  material: WOODEN_SWORD
  item-model: minecraft:wooden_sword
sort-order: 1

components:
  craftable:
    ingredients:
      wooden_handle: 1
      wooden_blade: 1
  damaging:
    dps: 15
```

### Boss-only weapon (only damages bosses)

`resources/compound_bow.yml`
```yaml
display:
  name: "<#4CC9F0><b>Compound Bow"
  material: BOW
  item-model: minecraft:bow
sort-order: 12

unlock:
  stage: 25
  prestige: 1

components:
  craftable:
    ingredients:
      bow: 2
      iron: 50
      string: 100
  damaging:
    dps: "12.5K"
    target: bosses
```

### Everything at once (drops, craftable, damaging, tradeable)

`resources/toy_soldier.yml`
```yaml
display:
  name: "<#FF6B6B><b>Toy Soldier"
  material: ARMOR_STAND
  item-model: idlemob:toy_soldier
sort-order: 20

unlock:
  stage: 30
  prestige: 2

components:
  droppable:
    boss:
      chance: 5
      amount:
        min: 1
        max: 2
  craftable:
    ingredients:
      toy_pieces: 25
      magic_core: 2
    output: 1
  damaging:
    dps: "1.2M"
  tradeable:
    enabled: true
```

## Rules the plugin checks on load

- `item-model` must be a valid model key (`namespace:path`, lowercase).
- Every ingredient ID must exist.
- A recipe can't use the resource itself, directly or through other recipes.
- A drop's `amount.min` can't be bigger than its `amount.max`.
- A blueprint's `craft-time` must be longer than 0 and its `amount` at least 1.
- A resource whose file was removed but that players still own is kept in
  the database and shown as "Unknown resource" until it's added back.
- Problems are listed in the console, and a broken file is skipped instead
  of stopping the whole plugin.

## Blueprints

A resource with a `blueprint` component can be made by a generator. The
blueprint's ID is the resource ID, and the item is named after the resource
(e.g. "Wood Blueprint") and looks like the resource itself.

The number of uses isn't set here. Whatever gives out the blueprint (a box,
`/blueprint give`) decides it. Every blueprint item gets a unique ID and its
remaining uses stored on it, so blueprints never stack and can't be duped into
two generators.

A resource without a `blueprint` component has no blueprint.

`resources/wood.yml`
```yaml
display:
  name: "<light_purple><b>Wood"
  material: OAK_PLANKS
  item-model: minecraft:oak_planks
components:
  droppable:
    mob:
      chance: 100
      amount:
        min: 2
        max: 5
  blueprint:
    craft-time: 30s
    amount: 25
```
