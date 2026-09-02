# DreamBot API analysis and DreamBot Reborn port

## Source inventory

The supplied `/Users/user/deob-db/client.jar` identifies itself as a DreamBot
build in `META-INF/MANIFEST.MF`. It is a fat jar: in addition to the client it
contains Guava, Gson, JOGL, JCEF, OkHttp, Log4j, Tink, and other dependencies.
The relevant namespace contains 528 class files under `org/dreambot/api`, of
which 452 are top-level classes.

Public signatures were inventoried with `javap -public`; implementation bytecode
was not copied. DreamBot Reborn provides independent RuneLite-backed implementations and
does not load or ship the supplied jar.

## Package mapping

The original namespace is mirrored below DreamBot Reborn's own root:

| DreamBot package | DreamBot Reborn package |
| --- | --- |
| `org.dreambot.api.methods.interactive` | `com.dreambotreborn.api.methods.interactive` |
| `org.dreambot.api.methods.container.impl` | `com.dreambotreborn.api.methods.container.impl` |
| `org.dreambot.api.methods.container.impl.bank` | `com.dreambotreborn.api.methods.container.impl.bank` |
| `org.dreambot.api.methods.container.impl.equipment` | `com.dreambotreborn.api.methods.container.impl.equipment` |
| `org.dreambot.api.methods.depositbox` | `com.dreambotreborn.api.methods.depositbox` |
| `org.dreambot.api.methods.trade` | `com.dreambotreborn.api.methods.trade` |
| `org.dreambot.api.methods.magic` | `com.dreambotreborn.api.methods.magic` |
| `org.dreambot.api.methods.prayer` | `com.dreambotreborn.api.methods.prayer` |
| `org.dreambot.api.methods.login` | `com.dreambotreborn.api.methods.login` |
| `org.dreambot.api.methods.item` | `com.dreambotreborn.api.methods.item` |
| `org.dreambot.api.methods.widget` | `com.dreambotreborn.api.methods.widget` |
| `org.dreambot.api.methods.skills` | `com.dreambotreborn.api.methods.skills` |
| `org.dreambot.api.methods.walking.impl` | `com.dreambotreborn.api.methods.walking.impl` |
| `org.dreambot.api.wrappers.interactive` | `com.dreambotreborn.api.wrappers.interactive` |
| `org.dreambot.api.wrappers.items` | `com.dreambotreborn.api.wrappers.items` |
| `org.dreambot.api.wrappers.widgets` | `com.dreambotreborn.api.wrappers.widgets` |

## Implemented surface

- Scene snapshots and closest/filter queries: `GameObjects`, `NPCs`, `Players`,
  `GroundItems`, `GraphicsObjects`, and `Projectiles`.
- Wrappers: `Entity`, `Character`, `GameObject`, `NPC`, `Player`, `GroundItem`,
  `Item`, `Widget`, `WidgetChild`, `Menu`, `MenuRow`, and message wrappers.
- Containers: `Inventory`, `Equipment`, `EquipmentSlot`, and `Bank`, including
  DreamBot-style synchronous operations, drop patterns, bank tabs, quantities,
  placeholders and scroll helpers. `Shop`, `DepositBox`, `Trade`, and the
  general item-container facade use the same live RuneLite state.
- Client state: `Client`, `Skills`, `Skill`, `SkillTracker`, `Combat`, `Prayers`,
  collision maps, camera state, login state, `Camera`, `PlayerSettings`, and
  `Varcs`.
- UI and world helpers: `Widgets`, `Dialogues`, `Tabs`, `Tab`, `Worlds`, `World`,
  `WorldHopper`, `Emotes`, live game menus, and login-screen controls.
- Magic and prayer: the standard spellbook, spell selection/casting, prayer
  activation/flicking, quick prayers, and their enums.
- Navigation: collision-aware local paths plus an extensible A* web graph and
  custom web nodes. Long-distance walking falls back to intermediate minimap
  destinations when no custom graph route has been registered.
- Input/utilities: vector-dynamics `VirtualMouse`, destination classes, both
  DreamBot keyboard package facades, `Animations`, `Randoms`, composable
  `Filter<T>` implementations, `Calculations`, and immutable `Query<T>` snapshots.
- Runtime: `AbstractScript`, `TaskScript`, `TreeScript`, manifest discovery,
  on-canvas paints, lifecycle controls, typed listeners, priority random
  solvers, account-bound scheduling, and stop conditions.

The common interaction surface now follows DreamBot's immediate `boolean`
contract. It blocks a script/background thread while the visible virtual mouse
moves and confirms the click. Each operation also has an explicit `*Async`
companion returning `CompletableFuture<Boolean>` for UI and event-driven code.
Calling a synchronous operation from the game client thread never waits on that
same thread; it queues the remaining work and reports whether it was accepted.

## Examples

```java
import com.dreambotreborn.api.methods.container.impl.Inventory;
import com.dreambotreborn.api.methods.container.impl.bank.Bank;
import com.dreambotreborn.api.methods.interactive.GameObjects;
import com.dreambotreborn.api.methods.interactive.NPCs;

Bank.closest();
boolean opened = Bank.open();
Bank.openAsync().thenAccept(result -> System.out.println("Bank click sent: " + result));

GameObjects.closest("Bank booth").interact("Bank");
NPCs.find(npc -> npc.name.equals("Goblin") && npc.distance() < 8).first();
Inventory.find(item -> item.name.equals("Lobster")).first();
```

Queries return `null` when no matching wrapper exists. Synchronous interactions
return `false`, and asynchronous interactions resolve to `false`, when the
target has disappeared, is off-screen, has no matching action, or the game does
not accept the generated menu entry.

## Intentional remaining boundary

The supplied jar exposes 452 top-level API classes; DreamBot Reborn currently mirrors 179
of those exact relative class paths and also has DreamBot Reborn-specific runtime/UI
classes. Class count is not treated as completion: empty signature stubs would
compile while behaving incorrectly. The remaining bulk consists mostly of
DreamBot's proprietary world-web/transport data, quest and achievement-diary
datasets, client-internal cache nodes, JCEF/SDN/license services, and duplicate
low-level AWT event classes. DreamBot Reborn's functional web is intentionally
extensible, but it does not pretend to contain DreamBot's private graph.

The supplied jar is used only to inventory public names and signatures. It is
never loaded, decompiled into DreamBot Reborn, or redistributed. New compatibility
classes are independent RuneLite-backed implementations and must be validated
against current game state before being advertised as functional.
