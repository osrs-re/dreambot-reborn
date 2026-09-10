# DreamBot Reborn — Agent-Assisted OSRS Reverse Engineering

**DreamBot Reborn is an open-source Java reverse-engineering and API-reconstruction project for Old School RuneScape (OSRS).** Its central goal is to recover the developer experience of a discontinued legacy DreamBot client artifact: package structure, public method signatures, script lifecycle, query semantics, task frameworks, input abstractions, and observable runtime behavior. Those contracts are reimplemented independently on top of RuneLite's published interfaces and injected game client.

The reconstruction is accelerated by **AI coding agents** that inventory Java bytecode, compare namespaces, extract public APIs with `javap`, trace control flow, map legacy types to RuneLite equivalents, implement compatibility layers, and generate regression tests. If you are researching **DreamBot reverse engineering**, **DreamBot deobfuscation**, **Java JAR analysis**, **Java bytecode analysis**, **OSRS client reverse engineering**, **RuneLite internals**, **agent-assisted software archaeology**, or an open-source reference for `GameObjects.find(...)`, `Bank.closest()`, `TaskScript`, `TreeScript`, `ScriptManifest`, and `onPaint`, this repository documents the process as well as the resulting client.

> [!IMPORTANT]
> DreamBot Reborn is an independent community project. It is not affiliated with, endorsed by, or distributed by DreamBot, RuneLite, Jagex, or Old School RuneScape. DreamBot, RuneLite, Jagex, RuneScape, and Old School RuneScape are names or trademarks of their respective owners.

> [!NOTE]
> “Discontinued” in this repository refers to the specific legacy reference artifact supplied for this reconstruction. The reference JAR is not committed, redistributed, loaded at runtime, or required to build DreamBot Reborn. This project does not make a claim about the operating status of any current third-party product or service.

## Contents

- [Overview](#overview)
- [Reverse-engineering mission](#reverse-engineering-mission)
- [Agent-assisted reconstruction](#agent-assisted-reconstruction)
- [Deobfuscation and implementation boundaries](#deobfuscation-and-implementation-boundaries)
- [Features](#features)
- [Quick start](#quick-start)
- [How the OSRS client is embedded](#how-the-osrs-client-is-embedded)
- [DreamBot-style Java API](#dreambot-style-java-api)
- [Writing an OSRS script](#writing-an-osrs-script)
- [TaskScript and TreeScript](#taskscript-and-treescript)
- [Virtual mouse and keyboard input](#virtual-mouse-and-keyboard-input)
- [Script Manager and scheduling](#script-manager-and-scheduling)
- [Developer tools and widget inspector](#developer-tools-and-widget-inspector)
- [Accounts and automatic login](#accounts-and-automatic-login)
- [Settings and preferred worlds](#settings-and-preferred-worlds)
- [Architecture](#architecture)
- [Testing](#testing)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [Security and responsible use](#security-and-responsible-use)
- [License](#license)

## Overview

DreamBot Reborn is a practical exercise in preserving a useful Java API after its original reference project stopped being maintained. The supplied historical JAR contains a large, partly obfuscated application and dependency graph, but its public API still describes a recognizable scripting platform. Rather than treating the artifact as a black box forever, this project turns that legacy surface into an indexed, testable, human-readable compatibility specification.

The runtime is deliberately smaller than a full RuneLite distribution. It does not start RuneLite itself and does not load RuneLite's plugin hub, updater, sidebar, configuration graph, or complete dependency-injection application. Instead, it keeps the pieces required to run the current OSRS client in an AWT/Swing window and rebuilds the recovered script-facing behavior around RuneLite interfaces.

The result is useful for developers researching or building:

- an open-source OSRS client in Java;
- an Old School RuneScape automation or scripting framework;
- a DreamBot-inspired API backed by RuneLite interfaces;
- an OSRS script loader with `@ScriptManifest` discovery;
- a RuneScape virtual mouse and keyboard input system;
- an OSRS widget explorer and developer console;
- priority-based `TaskScript` bots and hierarchical `TreeScript` bots;
- account-bound script schedules and runtime paints;
- a minimal Swing host for RuneLite's injected client.

This repository does not contain the proprietary game client. At startup it reads Jagex's current `jav_config.ws`, loads the configured client class supplied through the Maven dependencies, injects the minimum runtime services, and initializes the game.

## Reverse-engineering mission

The supplied legacy JAR identifies itself as a DreamBot build in its manifest. It is a fat Java archive containing application classes plus third-party dependencies. The relevant `org.dreambot.api` namespace contains **528 class files** and **452 top-level classes**. DreamBot Reborn currently contains **308 API source units**, including **284 matching relative class paths** beneath its own `com.dreambotreborn.api` namespace. The opt-in reflection audit can currently load **283 of 449 public top-level legacy API classes**.

Those numbers are a navigation aid, not a claim of perfect compatibility. A generated empty class can improve a parity percentage without recovering any useful behavior. The project therefore prioritizes observable contracts:

- package names and type relationships used by existing scripts;
- public constructors, methods, overloads, annotations, enums, and return types;
- null, boolean, delay, ordering, and selection semantics;
- script lifecycle and `TaskScript`/`TreeScript` execution rules;
- widget, entity, inventory, bank, input, walking, and paint behavior;
- thread ownership across the OSRS game thread, script workers, input queues, and Swing EDT;
- regression tests that record every reconstructed behavior.

The detailed namespace inventory and current parity boundary are documented in [docs/DREAMBOT_API_PORT.md](docs/DREAMBOT_API_PORT.md). Script bytecode observations are documented separately in [docs/DREAMBOT_SCRIPT_FRAMEWORK.md](docs/DREAMBOT_SCRIPT_FRAMEWORK.md).

## Agent-assisted reconstruction

DreamBot Reborn uses a repeatable multi-agent development workflow. AI coding agents help divide a very large reverse-engineering problem into bounded investigations while the repository, tests, and documentation remain the shared source of truth.

```text
Legacy DreamBot reference JAR
            │
            ▼
  archive and namespace inventory
            │
            ▼
 javap signatures + targeted bytecode inspection
            │
            ▼
 API map + observable behavior specification
            │
            ▼
 independent RuneLite-backed implementation
            │
            ▼
 unit tests + script compatibility + live OSRS smoke tests
```

Agents are used for complementary roles:

1. **Inventory agents** enumerate packages, top-level classes, nested classes, inheritance, annotations, and dependency boundaries.
2. **Signature agents** extract public APIs with `javap -public` and compare them with the reconstructed namespace.
3. **Behavior agents** inspect small, relevant bytecode paths to recover ordering, fallbacks, defaults, lifecycle transitions, and failure behavior.
4. **Mapping agents** connect legacy concepts to supported RuneLite state, widgets, menu actions, events, and geometry.
5. **Implementation agents** write original Java code under `com.dreambotreborn` without making the reference JAR a dependency.
6. **Verification agents** add tests, compile external scripts, check package parity, and identify claims that still require a live-client smoke test.
7. **Documentation agents** turn recovered evidence into API maps, examples, limitations, and reproducible research notes.

AI output is treated as a hypothesis until it compiles and is supported by signatures, bytecode evidence, tests, or observable client behavior. This is especially important for obfuscated identifiers, overloaded methods, thread-sensitive interactions, and OSRS revision changes.

Each agent handoff should leave a compact recovery record:

```text
Reference type:   org.dreambot.api...
Public surface:   constructors, methods, fields, annotations, inheritance
Observed rules:   ordering, defaults, nullability, delays, state transitions
RuneLite mapping: client state, event, widget, menu action, or geometry source
Implementation:   com.dreambotreborn.api...
Verification:     unit test, compatibility compile, or live smoke-test result
Open questions:   behavior that still lacks evidence
```

Keeping that evidence beside the implementation prevents agent guesses from silently becoming compatibility promises. A second agent can review the record, challenge assumptions, and reproduce the result without needing the original conversation.

## Deobfuscation and implementation boundaries

“Deobfuscation” here means converting an opaque legacy binary into an understandable compatibility model. It includes archive inspection, manifest analysis, class-name recovery where metadata permits it, public-signature extraction, bytecode control-flow reading, call-graph tracing, and behavior-oriented testing. It does **not** mean checking generated decompiler output into this repository.

The project follows these boundaries:

- the supplied reference JAR remains local and is never shipped with DreamBot Reborn;
- proprietary implementation source is not copied into the repository;
- public signatures and observable behavior are used to define compatibility targets;
- every runtime implementation is written independently against RuneLite's published API;
- private SDN, licensing, account-service, transport-web, and proprietary dataset internals are not cloned;
- uncertain behavior is documented as incomplete instead of being presented as exact;
- current Jagex/RuneLite client code is consumed only through declared Maven dependencies.

This makes DreamBot Reborn useful not only as an OSRS Java client, but also as a case study in **legacy Java recovery**, **API archaeology**, **binary compatibility research**, **JAR reverse engineering**, **deobfuscation tooling**, and **AI-agent-assisted software reconstruction**.

### Reproduce the local JAR analysis

Point `REFERENCE_JAR` at your own legally obtained legacy artifact. Standard archive utilities and JDK tools are enough for the first analysis pass:

```shell
REFERENCE_JAR=/absolute/path/to/client.jar

# Inspect archive contents and build metadata.
jar tf "$REFERENCE_JAR"
unzip -p "$REFERENCE_JAR" META-INF/MANIFEST.MF

# Inspect a public API without copying its implementation.
javap -classpath "$REFERENCE_JAR" -public org.dreambot.api.script.AbstractScript

# Inspect targeted bytecode when a public signature does not explain behavior.
javap -classpath "$REFERENCE_JAR" -c -p org.dreambot.api.script.impl.TaskScript

# Understand module and library dependencies.
jdeps -summary "$REFERENCE_JAR"
```

Do not commit the reference JAR or bulk decompiler output. Record only the compatibility facts needed for an independent implementation, then encode those facts in documentation and tests.

## Features

| Area | Included functionality |
|---|---|
| Reverse engineering | Legacy JAR inventory, manifest inspection, `javap` signature extraction, targeted bytecode analysis, package mapping, and behavior recovery |
| Agent-assisted recovery | Parallel inventory, signature, behavior, mapping, implementation, verification, and documentation roles with evidence-based handoffs |
| API reconstruction | 284 matching relative API class paths plus independent compatibility and client-support classes under `com.dreambotreborn.api` |
| OSRS client | Current injected Old School RuneScape client hosted in a Java Swing `JFrame` |
| DreamBot-style API | Static methods, immutable wrappers, predicates, filters, queries, entity interaction, containers, skills, worlds, widgets, magic, prayer, trade, shops, and banking |
| Script runtime | `AbstractScript`, `@ScriptManifest`, lifecycle callbacks, pause, resume, stop, skip, runtime timer, arguments, and one active script at a time |
| Script frameworks | Flat priority-based `TaskScript` and hierarchical `TreeScript`, `Root`, `Branch`, and `Leaf` |
| OSRS entities | `GameObjects`, `NPCs`, `Players`, `GroundItems`, projectiles, graphics objects, and tile-based queries |
| Item containers | `Inventory`, `Bank`, `Equipment`, `Shop`, `DepositBox`, `Trade`, and general item-container wrappers |
| Input | Rendered virtual mouse, randomized movement, crosshair, fading trail, synthetic clicks, menu-action verification, and virtual keyboard typing |
| Human input control | Toolbar toggle for physical canvas input while synthetic script input remains available |
| Paint API | Stable `onPaint(Graphics)` and `onPaint(Graphics2D)` rendering over the game buffer |
| Developer tools | Live widget tree, child widgets, search, bounds, click-to-pick, highlights, method inspection, console, and object view |
| Accounts | Local account manager, account selection, logout/login switching, and automatic login before script start |
| Scheduling | Account-bound sequential scripts with duration, skill, and script stop conditions |
| Settings | Continuously saved JSON settings, preferred OSRS world, mouse profile, trail duration, keyboard speed, and typing behavior |
| Random solvers | Login, welcome screen, dismissible NPC, breaks, and recovery hooks |
| Pathfinding | Collision-aware local paths and a customizable A* web built from user-defined nodes |
| External scripts | JAR discovery from `~/.dreambot-reborn/scripts` with isolated classloaders |

## Quick start

### Requirements

- JDK 11 or newer
- Maven 3.8 or newer
- Internet access when starting the OSRS client
- macOS, Linux, or Windows with a graphical desktop environment

The project is currently configured against RuneLite API and injected-client version `1.12.37`. The game client is revision-specific, so that version may need updating after an Old School RuneScape game update.

### Run from source

```shell
git clone https://github.com/osrs-re/dreambot-reborn.git
cd dreambot-reborn
mvn clean test
mvn compile exec:java
```

### Build an executable JAR

```shell
mvn clean package
java -jar target/dreambot-reborn.jar
```

The shaded `target/dreambot-reborn.jar` contains the runtime dependencies needed by the desktop client. Its main class is:

```text
com.dreambotreborn.DreamBotReborn
```

### First launch

On its first start, DreamBot Reborn creates:

```text
~/.dreambot-reborn/
├── accounts.json
├── settings.json
└── scripts/
```

The Jagex client may add its regular cache and preference files beneath the same runtime directory. Build output stays under `target/`; local runtime data and credentials are covered by the repository's `.gitignore` rules.

## How the OSRS client is embedded

Modern RuneLite does not expose the game as a traditional `java.applet.Applet`. The injected `client` is an AWT component implementing `net.runelite.api.Client`. DreamBot Reborn follows the relevant RuneLite startup lifecycle without launching the complete RuneLite desktop application:

1. Download Jagex's current `jav_config.ws`.
2. Apply the configured preferred OSRS world to the codebase and client parameters.
3. Instantiate the configured injected `client` class.
4. Provide callbacks, a scheduled executor, the runtime directory, and required injected members.
5. Bind the public DreamBot Reborn API to `net.runelite.api.Client`.
6. Add the game component to a Swing `JFrame`.
7. Call `client.initialize()`.
8. Refresh immutable game snapshots and dispatch RuneLite events to scripts.

`MinimalCallbacks` keeps the API, input system, widget inspector, login state machine, paint system, and script listener bus synchronized with the live OSRS game client.

## DreamBot-style Java API

The public API lives under `com.dreambotreborn.api`. Its relative package structure and developer-facing contracts are reconstructed from the legacy `org.dreambot.api` surface so older script architecture remains recognizable. The code behind those contracts is original and backed by RuneLite's public interfaces; the reference JAR is not required at build time or runtime.

This separation is the main reverse-engineering seam: legacy signatures describe *what* a script expects, targeted bytecode analysis clarifies ambiguous observable behavior, and the RuneLite-backed implementation decides *how* that behavior works in DreamBot Reborn.

### Find and interact with game objects

```java
import com.dreambotreborn.api.methods.interactive.GameObjects;
import com.dreambotreborn.api.wrappers.interactive.GameObject;

GameObject booth = GameObjects
    .find(object -> "Bank booth".equalsIgnoreCase(object.name))
    .nearest();

if (booth != null)
{
    booth.interact("Bank");
}
```

### Query NPCs, players, and ground items

```java
NPC banker = NPCs.closest(npc -> npc.hasAction("Bank"));
Player local = Players.getLocal();
GroundItem bones = GroundItems.closest(item -> "Bones".equalsIgnoreCase(item.name));
```

### Work with inventory, equipment, and bank containers

```java
Item food = Inventory.find(item -> item.name.equalsIgnoreCase("Lobster")).first();

if (food != null)
{
    food.interact("Eat");
}

if (!Bank.isOpen())
{
    Bank.open();
}

Bank.depositAllExcept("Coins");
Bank.withdraw("Lobster", 10);
```

### Query vocabulary

Most entity and item providers expose a reusable query model:

- `first()` returns the first result or `null`;
- `firstOptional()` returns an `Optional`;
- `nearest()` chooses the nearest entity in a chainable `Query`;
- provider methods such as `GameObjects.closest(...)` and `NPCs.closest(...)` query the nearest match directly;
- `all()` returns an immutable result list;
- `filter(...)` narrows an existing query;
- `count()` returns the result count;
- `stream()` provides a Java Stream;
- `named(...)`, `namedIgnoreCase(...)`, and ID helpers cover common filters.

The OSRS API surface includes game objects, wall objects, decorative objects, ground objects, NPCs, players, ground items, inventory items, bank items, equipment, widgets, menus, skills, worlds, spells, prayers, combat state, animations, collision maps, and walking.

For the package-by-package DreamBot API comparison, see [docs/DREAMBOT_API_PORT.md](docs/DREAMBOT_API_PORT.md).

## Writing an OSRS script

A loadable script extends `AbstractScript`, has a no-argument constructor, and carries a runtime-retained `@ScriptManifest` annotation.

```java
package example.scripts;

import com.dreambotreborn.api.methods.interactive.GameObjects;
import com.dreambotreborn.api.script.AbstractScript;
import com.dreambotreborn.api.script.Category;
import com.dreambotreborn.api.script.ScriptManifest;
import java.awt.Color;
import java.awt.Graphics2D;

@ScriptManifest(
    name = "Simple OSRS Woodcutter",
    author = "Community Developer",
    description = "Finds and chops nearby trees",
    category = Category.WOODCUTTING,
    version = 1.0)
public final class SimpleWoodcutter extends AbstractScript
{
    @Override
    public void onStart()
    {
        log("Starting Simple OSRS Woodcutter");
    }

    @Override
    public int onLoop()
    {
        var tree = GameObjects.closest(object ->
            object.name.equalsIgnoreCase("Tree") && object.hasAction("Chop down"));

        if (tree != null)
        {
            tree.interact("Chop down");
        }
        return 600;
    }

    @Override
    public void onPaint(Graphics2D graphics)
    {
        graphics.setColor(Color.WHITE);
        graphics.drawString("Runtime: " + getScriptTimer().formatTime(), 20, 40);
    }

    @Override
    public void onExit()
    {
        log("Woodcutter stopped");
    }
}
```

Build the script as a JAR, place it in `~/.dreambot-reborn/scripts/`, and press **Refresh** in the Script Manager. Returning a negative delay from `onLoop()` stops the script.

Available lifecycle hooks include:

- `onStart()` and `onStart(String...)`;
- `onLoop()`;
- `onPause()` and `onResume()`;
- `onScheduledStop()`;
- `onPaint(Graphics)` and `onPaint(Graphics2D)`;
- `onExit()`.

## TaskScript and TreeScript

DreamBot Reborn implements two common Java bot-script architectures.

### TaskScript

`TaskScript` evaluates `TaskNode` objects by descending priority. The first node whose `accept()` method returns `true` is executed.

```java
public final class BankingNode extends TaskNode
{
    @Override
    public int priority()
    {
        return 100;
    }

    @Override
    public boolean accept()
    {
        return Inventory.isFull();
    }

    @Override
    public int execute()
    {
        Bank.open();
        return 500;
    }
}
```

Use `addNodes(...)`, `removeNodes(...)`, `getNodes()`, `getLastTaskNode()`, and `setFailLimit(...)` to control the flat task graph.

### TreeScript

`TreeScript` models behavior as a hierarchy of `Root`, `Branch`, and `Leaf` nodes. Each branch selects its first valid child, making it suitable for larger OSRS scripts with nested states such as banking, travel, combat, recovery, and resource gathering.

See [docs/DREAMBOT_SCRIPT_FRAMEWORK.md](docs/DREAMBOT_SCRIPT_FRAMEWORK.md) for framework signatures, selection semantics, and more examples.

## Virtual mouse and keyboard input

DreamBot Reborn uses visible synthetic input instead of silently invoking every action. The virtual mouse:

- moves through a configurable mouse algorithm;
- targets randomized points within entity clickboxes and widget bounds;
- renders a crosshair over the OSRS canvas;
- draws a configurable fading mouse trail;
- supports click, move, hop, entity, tile, minimap, rectangle, polygon, and point destinations;
- verifies requested menu actions before clicking;
- allows synchronous script interactions and asynchronous `CompletableFuture` workflows.

```java
VirtualMouse.moveTo(250, 180)
    .thenRun(VirtualMouse::click);
```

The virtual keyboard supports configurable words per minute, key holds, Shift, Control, Space, Escape, Enter, optional typing corrections, and queued AWT events.

Physical mouse input can be disabled from the bottom toolbar. Starting an account-bound script disables physical OSRS canvas input automatically, while `VirtualMouse` and scripted keyboard input continue working. Moving the physical mouse updates the shared crosshair whenever physical input is enabled.

## Legacy DreamBot script compatibility

Existing compiled scripts that still reference `org.dreambot.api` can be loaded directly. Put the
original script JAR in `~/.dreambot-reborn/scripts/` and press **Refresh**. DreamBot Reborn detects
the old namespace and marks the entry as **Legacy** in the Script Manager.

Compatibility happens entirely in memory:

- classes, descriptors, annotations, generic signatures, lambdas, and method references are
  remapped from `org.dreambot.api` to `com.dreambotreborn.api` as each script class is loaded;
- compatible binary descriptor changes are adapted, including legacy collection-returning calls
  such as `GameObjects.all(): List` targeting the current `GameObjects.all(): Query`;
- API classes accidentally bundled in a fat script JAR are ignored, so they cannot shadow the
  client API;
- support JARs placed in the same scripts directory share the external-script class path;
- the source JAR is never unpacked, patched, or overwritten.

The compatibility layer preserves script bytecode and execution logic; it cannot invent an API
that has not been reconstructed yet. A legacy script that reaches a missing class or incompatible
method stops with a `LegacyScriptCompatibilityException` identifying the unresolved symbol. Native
code, private client internals, third-party libraries absent from the scripts directory, and APIs
whose argument or behavior contract cannot safely be adapted still require a source-level port.

This feature is meant for legally obtained scripts you own or are permitted to run. The historical
reference client JAR itself is neither loaded nor distributed.

Maintainers with a locally owned reference artifact can run the optional end-to-end compatibility
fixture. It compiles a fresh script against that JAR, loads the untouched result through DreamBot
Reborn, and exercises boxed-ID and filter calls:

```shell
mvn -Ddreambot.referenceJar=/absolute/path/to/client.jar \
  -Dtest=LegacyScriptLoaderTest test
```

## Script Manager and scheduling

The bottom script button opens a dedicated Script Manager `JFrame`. It discovers bundled scripts and external script JARs from:

```text
~/.dreambot-reborn/scripts/
```

The Script Manager provides:

- manifest name, author, description, category, version, and source;
- saved-account selection;
- start, pause, resume, stop, and refresh controls;
- current script state and runtime;
- one active OSRS script at a time;
- automatic account verification before startup;
- logout and re-login when another account is active;
- login recovery hooks while a script is running.

The Schedule Manager runs account-bound script entries sequentially. Stop conditions can be based on running time, skill progress, or the script's own completion signal.

## Developer tools and widget inspector

The Developer sidebar contains a live OSRS widget inspector. It displays static widgets, dynamic children, nested child widgets, and login-screen components in a searchable tree.

For every selected widget, the inspector can show:

- packed widget ID, group, child, and index;
- widget type and content type;
- cleaned text and name;
- item ID and quantity;
- available menu actions;
- canvas bounds and scroll state;
- visibility and child count.

Selecting a node highlights its exact bounds over the game. **Pick** mode lets developers hover and select widgets directly on the OSRS canvas; picker clicks are consumed so they do not accidentally reach the game.

Login-screen fields are represented without exposing credential values. Password and authenticator nodes expose only safe focus or redaction metadata.

The right sidebar also includes Script Control, Break Manager, Objects, Console, and Settings views. The bottom lifecycle bar exposes scripts, accounts, play, pause, stop, reload, schedule, settings, developer tools, and physical mouse input.

## Accounts and automatic login

The account manager stores accounts locally in:

```text
~/.dreambot-reborn/accounts.json
```

There is no master-password prompt. The account file is readable JSON intended for local test accounts, which means usernames and passwords are plaintext. Never commit, publish, upload, or share this file.

When starting a script, DreamBot Reborn:

1. requires a saved account selection;
2. checks whether the selected account is already logged in;
3. logs out if a different account is active;
4. fills the OSRS login state on the client thread;
5. submits the login using visible virtual input;
6. starts the script only after account preparation succeeds.

Authenticator challenges must still be completed manually before retrying script startup.

## Settings and preferred worlds

Settings are written immediately and atomically to:

```text
~/.dreambot-reborn/settings.json
```

Configurable options include:

- preferred Old School RuneScape world;
- automatic world application during startup;
- mouse speed and overshoot;
- click-hold timing;
- mouse-trail lifetime;
- keyboard words per minute;
- optional typing mistakes and corrections.

The preferred world is applied to Jagex's codebase host and world parameter before client initialization. **Apply now** can switch the login-screen world without restarting when the client state permits it.

## Architecture

```text
Jagex jav_config.ws
        │
        ▼
RuneLite injected client ──► Swing game canvas
        │
        ▼
MinimalCallbacks / client-thread dispatcher
        │
        ├──► immutable entity, container, skill, world and widget snapshots
        ├──► script listener and RuneLite event bridge
        ├──► virtual mouse and keyboard queues
        ├──► login, welcome, dismiss and break solvers
        └──► overlay, inspector and script paints
                    │
                    ▼
          AbstractScript / TaskScript / TreeScript
```

### Source layout

```text
src/main/java/com/dreambotreborn/
├── DreamBotReborn.java     # application entry point
├── MinimalCallbacks.java   # RuneLite event and paint bridge
├── accounts/               # local accounts and login workflow
├── api/
│   ├── input/              # virtual mouse and keyboard
│   ├── internal/           # snapshots, client thread, interaction targets
│   ├── methods/            # static OSRS method providers
│   ├── randoms/            # login, breaks and other solvers
│   ├── script/             # manifests, runtime, listeners and frameworks
│   ├── utilities/          # sleeps, timers and logging
│   └── wrappers/           # entities, items, widgets, menus and graphics
├── devtools/               # widget inspection and login UI adapter
├── scripts/                # Script Manager, scheduler and demo script
├── settings/               # persistent client and input configuration
└── ui/                     # DreamBot-style desktop interface
```

### Threading model

Live RuneLite state belongs to the game thread. DreamBot Reborn refreshes immutable snapshots there and makes those snapshots available to script threads and Swing UI components. Synthetic input is serialized through dedicated queues. Swing components are created or updated on the Event Dispatch Thread.

Scripts should prefer the provided wrappers and method providers. `unwrap()` exists for advanced integration, but live RuneLite objects must be handled with correct game-thread discipline.

## Testing

Run the complete client test suite:

```shell
mvn clean test
```

Build and test the executable package:

```shell
mvn clean test package
```

Run the optional structural comparison against a locally owned reference JAR:

```shell
mvn -Ddreambot.referenceJar=/absolute/path/to/client.jar \
  -Dtest=LegacyApiSurfaceAuditTest test
```

The reference JAR is read only for class and public-signature inventory. It is
not copied, initialized, packaged, or required at runtime.

The tests cover account persistence and login state, settings, filters, DreamBot API compatibility, virtual mouse behavior, local and web pathfinding, script lifecycle, `TaskScript`, and `TreeScript` semantics. Reverse-engineered rules such as priority ordering, negative loop termination, fallback delays, selection behavior, and synchronous interaction results should always receive focused regression tests.

Compatibility is checked at three different levels:

1. **Structural parity:** expected packages, classes, constructors, annotations, enums, and method signatures exist.
2. **Behavioral parity:** tests reproduce control flow and state transitions observed in the reference artifact.
3. **Runtime viability:** the independent implementation works against current RuneLite state and the live OSRS client.

Passing a structural comparison does not prove behavioral parity, and passing unit tests does not prove a revision-sensitive interaction still works in the live game. Documentation must state which level has actually been verified.

Live OSRS behavior cannot be fully reproduced by unit tests. After a RuneScape update, smoke-test login, widgets, menu actions, object interactions, banking, walking, and any external script before relying on it.

## Troubleshooting

### `error_game_js5connect`

This normally means the embedded OSRS client could not reach the selected world or game asset service. Check internet access, VPN/proxy/firewall settings, the configured preferred world, Jagex service availability, and the configured RuneLite version. Temporarily disable the preferred-world setting to test the default world supplied by `jav_config.ws`.

### Login screen is visible but clicks do not register

Check the physical mouse toggle in the bottom toolbar. When physical input is disabled, only `VirtualMouse` and scripted input are accepted by the canvas. Also ensure no widget-picker mode is consuming the click.

### Physical or virtual cursor flickers

The native cursor should be hidden only above the game canvas. Avoid installing another global custom cursor, and confirm that only one application instance is painting the virtual crosshair.

### Widget tree is empty

Wait until the client has completed a game cycle, press refresh, clear restrictive filters, and verify the Developer panel is connected to the active client. On the login screen, the inspector should show adapted login components even before the normal widget cache is available.

### External OSRS script does not appear

Confirm that:

- the file ends in `.jar` and is under `~/.dreambot-reborn/scripts/`;
- the script extends `AbstractScript`;
- the concrete class has `@ScriptManifest`;
- the class is not abstract;
- it has a no-argument constructor;
- it was compiled against the matching `com.dreambotreborn.api` version, or against the supported
  public `org.dreambot.api` surface and appears with a **Legacy** badge;
- **Refresh** was pressed after copying the JAR.

Open the Script Manager status area when a legacy script is rejected. Missing reconstructed APIs
are reported as compatibility errors; missing non-DreamBot dependencies should be placed in the
same scripts directory as separate JARs or bundled with the script.

### Client breaks after an OSRS update

The injected game client is revision-specific. Update `runelite.version` in `pom.xml`, rebuild with `mvn clean package`, and check RuneLite API changes that affect injected fields or callbacks.

### Side-menu popup immediately disappears

Use the current menu implementation, which keeps popup ownership while the pointer moves between a menu and its submenu. If customizing Swing menus, do not dispose the popup on the parent button's exit event before the submenu receives pointer focus.

## Contributing

Contributions for legacy DreamBot API reconstruction, Java bytecode research, OSRS API compatibility, Java client stability, RuneLite event integration, developer tools, virtual input, pathfinding, tests, documentation, and example scripts are welcome.

A useful contribution should:

1. keep game-thread and Swing-thread boundaries explicit;
2. interact through visible client state rather than hidden state mutation;
3. add or update tests for framework-level behavior;
4. avoid committing account files, credentials, caches, generated JARs, or IDE metadata;
5. document new public API methods;
6. preserve the independent-project and trademark disclaimer;
7. explain any hard-coded OSRS object, widget, varp, or varbit IDs;
8. cite the signature, bytecode observation, runtime observation, or test that supports a reconstructed behavior;
9. keep reference binaries, bulk decompiler output, proprietary datasets, and copied implementation code out of the repository;
10. mark uncertain or untested compatibility explicitly.

Suggested GitHub repository topics include `osrs`, `old-school-runescape`, `runescape`, `dreambot`, `dreambot-api`, `runelite`, `runelite-api`, `java`, `reverse-engineering`, `deobfuscation`, `java-bytecode`, `binary-analysis`, `software-archaeology`, `ai-agents`, `swing`, `automation`, `scripting-api`, `taskscript`, `treescript`, `virtual-mouse`, `widget-inspector`, and `pathfinding`.

## Security and responsible use

- Use only accounts that you own and are authorized to access.
- Treat `accounts.json` as sensitive because it contains plaintext test credentials.
- Never commit `.dreambot-reborn/`, account files, session material, caches, or local settings.
- Do not commit or redistribute the supplied legacy reference JAR or proprietary decompiler output.
- Review third-party script JARs before loading them; they execute Java code in the client process.
- Do not publish proprietary game assets, decompiled proprietary code, credentials, or private datasets.
- Follow applicable law and the terms and rules of any service you access.

This repository is intended for software education, interoperability research, legacy preservation, Java reverse engineering, API design, AI-agent workflow research, UI experimentation, and local testing. Running automation against an online game can carry account and security risks.

## License

No open-source license has been selected in this repository yet. Before publishing, add a `LICENSE` file such as MIT, Apache-2.0, or GPL-3.0 after choosing the terms you want. Without an explicit license, people may view the public source but generally do not receive permission to copy, modify, or redistribute it.

## Related OSRS and Java development terms

DreamBot Reborn covers concepts commonly searched as **DreamBot reverse engineering**, **DreamBot deobfuscation**, **DreamBot API reconstruction**, **legacy DreamBot client**, **Java JAR reverse engineering**, **Java bytecode analysis**, **javap bytecode disassembly**, **Java decompiler research**, **API archaeology**, **binary compatibility**, **agent-assisted development**, **AI coding agents**, **software archaeology**, **OSRS bot**, **Old School RuneScape bot client**, **RuneScape automation**, **DreamBot script**, **DreamBot API**, **DreamBot TaskScript**, **DreamBot TreeScript**, **OSRS scripting API**, **OSRS Java API**, **open-source OSRS client**, **RuneLite injected client**, **RuneLite bot development**, **Java bot framework**, **OSRS script manager**, **RuneScape virtual mouse**, **OSRS widget inspector**, **OSRS pathfinding**, **OSRS banking API**, **GameObjects.find**, **Bank.closest**, **ScriptManifest**, and **Java onPaint overlay**.
