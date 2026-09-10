# DreamBot API analysis and DreamBot Reborn port

## Source inventory

The locally supplied reference `client.jar` identifies itself as a DreamBot
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
- Client state: `Client`, `ClientSettings`, `Skills`, `Skill`, `SkillTracker`, `Combat`, `Prayers`,
  collision maps, camera state, login state, `Camera`, `PlayerSettings`, and
  `Varcs`.
- UI and world helpers: `Widgets`, `Dialogues`, `Tabs`, `Tab`, `Worlds`, `World`,
  `WorldHopper`, `GrandExchange`, `Quests`, `FairyRings`, `Emotes`, live game
  menus, and login-screen controls.
- Magic and prayer: standard, Ancient, Lunar, and Arceuus spellbooks with rune
  costs, experience and maximum-hit metadata; spell selection/casting; prayer
  activation/flicking; quick prayers; and their enums.
- Navigation: collision-aware local paths plus an extensible A* web graph and
  custom web nodes. Long-distance walking falls back to intermediate minimap
  destinations when no custom graph route has been registered.
- Input/utilities: vector-dynamics `VirtualMouse`, destination classes, both
  DreamBot keyboard package facades, `Animations`, `Randoms`, composable
  `Filter<T>` implementations, `Calculations`, and immutable `Query<T>` snapshots.
- Runtime: `AbstractScript`, `TaskScript`, `TreeScript`, manifest discovery,
  on-canvas paints, lifecycle controls, the legacy `ScriptEvent` hierarchy and
  typed listeners, RuneLite-to-compatibility event translation, priority random
  solvers, account-bound scheduling, and stop conditions.
- Compatibility data: bank locations, world types and locations, varbit/varp
  enums, quest books and composable combat/skill/quest/location requirements.

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

## Reproducible API audit

The opt-in audit compares public top-level class paths and inherited public
method signatures without initializing or bundling the reference artifact:

```shell
mvn -Ddreambot.referenceJar=/absolute/path/to/client.jar \
  -Dtest=LegacyApiSurfaceAuditTest test
```

At this revision it reports 283 of 449 public top-level API classes (63.0%).
For classes present in both artifacts, 4,245 of 5,427 reference public methods
have matching normalized signatures (78.2%). The second percentage deliberately
does not count methods of absent classes, so both figures must be read together.

## Compiled-script compatibility

The external script loader accepts owned script JARs compiled against the old
`org.dreambot.api` namespace. It rewrites class-file constant-pool references in
memory and delegates the mapped API types to DreamBot Reborn. The input JAR is
left byte-for-byte unchanged.

Binary method descriptors are stricter than Java source compatibility. For
example, a legacy invocation of `GameObjects.all(): java.util.List` cannot link
directly to a method declared as `GameObjects.all(): Query`, even though `Query`
implements `List`. The loader therefore resolves public target methods and
adapts a descriptor only when the current parameter types accept the legacy
arguments and the current return type is assignable to the legacy return type.
Ambiguous or unsafe adaptations are rejected. Common boxed `Integer[]` entity
and container ID overloads are exposed explicitly because they cannot be
safely changed into primitive `int[]` values by descriptor rewriting alone.

`LegacyScriptLoaderTest` builds an untouched legacy fixture JAR and verifies
annotations, inheritance, lambdas, filters, boxed IDs, collection returns,
failure diagnostics, and input-JAR immutability. With
`-Ddreambot.referenceJar=...`, it also compiles and runs a fixture against the
locally owned historical artifact.

## Intentional remaining boundary

The supplied jar exposes 452 top-level API class files, 449 of which are public.
DreamBot Reborn also has project-specific runtime and UI classes. Class count is
not treated as completion: empty signature stubs would compile while behaving
incorrectly. The remaining bulk consists mostly of DreamBot's proprietary
world-web/transport graph, achievement-diary datasets, client-internal cache and
model wrappers, obsolete social/forum facilities, specialized random solvers,
and duplicate low-level AWT event machinery. DreamBot Reborn's functional web
is intentionally extensible, but it does not pretend to contain DreamBot's
private graph.

The supplied jar is used only to inventory public names and signatures. It is
never loaded, decompiled into DreamBot Reborn, or redistributed. New compatibility
classes are independent RuneLite-backed implementations and must be validated
against current game state before being advertised as functional.
