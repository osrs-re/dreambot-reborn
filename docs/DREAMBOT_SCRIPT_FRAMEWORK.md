# DreamBot-style script framework analysis

This implementation was derived from the public signatures and bytecode of the
discontinued `/Users/user/deob-db/client.jar`. The DreamBot Reborn classes use the same
relative package structure under `com.dreambotreborn` and reproduce the observable
TaskScript and tree-branch behavior without loading that jar at runtime.

## Package and signature map

| Source jar | DreamBot Reborn | Public role |
| --- | --- | --- |
| `org.dreambot.api.script.ScriptManifest` | `com.dreambotreborn.api.script.ScriptManifest` | Runtime type annotation with category, name, description, author, version, image, and `_key` |
| `org.dreambot.api.script.AbstractScript` | `com.dreambotreborn.api.script.AbstractScript` | Lifecycle, loop, timer, paint, state, and stop API |
| `org.dreambot.api.script.TaskNode` | `com.dreambotreborn.api.script.TaskNode` | `priority()`, `accept()`, and `execute()` |
| `org.dreambot.api.script.impl.TaskScript` | `com.dreambotreborn.api.script.impl.TaskScript` | Priority-based flat task framework |
| `org.dreambot.api.script.frameworks.treebranch.TreeScript` | matching DreamBot Reborn package | Root-owned hierarchical framework |
| `Root`, `Branch`, `Leaf` in `frameworks.treebranch` | matching DreamBot Reborn package | Tree structure and first-valid-child traversal |
| `frameworks.utility.Sleepable` | matching DreamBot Reborn package | Six default sleep methods |
| `frameworks.utility.Loggable` | matching DreamBot Reborn package | Object and formatted log overloads |

`Category` contains the same 24 values found in the jar. `Unobfuscated` is also
present with runtime retention and type/method/field targets. Store, premium
account, and SDN internals are deliberately not runtime dependencies; harmless
SDN metadata getters return local-script defaults. Random solvers, listener
dispatch, schedules, and solver lifecycle callbacks are implemented locally.

## Lifecycle behavior

The manager owns one daemon script thread. Starting a script creates it through
its no-argument constructor, starts its timer, and calls `onStart(String...)`;
the default implementation delegates to `onStart()`. It then repeatedly calls
`onLoop()` and treats the returned integer as milliseconds to wait. As in the
source bytecode, a negative result stops the script. A failure in user code is
reported without taking down the game render thread, and `onExit()` runs once
when the managed script thread ends.

Pause and resume update the manager state, pause or resume the script timer, and
call `onPause()` or `onResume()`. Script paints are isolated on a copied
`Graphics2D`, so a script cannot permanently change the transform, stroke,
colour, or clip used by later overlays.

## TaskScript behavior

The bytecode shows these exact rules:

1. Nodes are copied, sorted by descending `priority()`, and evaluated in that
   order on each loop.
2. The first node whose `accept()` returns `true` becomes `getLastTaskNode()` and
   its `execute()` result is returned.
3. The default priority is `1`.
4. With no accepted node, the internal failure counter increments and the loop
   returns `1000`.
5. `setFailLimit(n)` stops by returning `-1` once the counter is greater than
   the positive limit. The source counter begins at `-1` and resets to `0` after
   a successful node; DreamBot Reborn preserves that boundary behavior.

```java
@ScriptManifest(
    name = "Task example", author = "Example Author",
    category = Category.UTILITY, version = 1.0)
public final class TaskExample extends TaskScript
{
    @Override
    public void onStart()
    {
        setFailLimit(10);
        addNodes(new TaskNode()
        {
            @Override public int priority() { return 10; }
            @Override public boolean accept() { return Inventory.isFull(); }
            @Override public int execute()
            {
                // Bank items here.
                return 600;
            }
        });
    }
}
```

## TreeScript behavior

`TreeScript` constructs one `Root(this)`. `addBranches(Leaf...)` delegates to
the root's `addLeaves`, while `Root.addBranches(Branch...)` is the narrower
convenience overload found in the jar. Adding children sets their parent; a leaf
finds its tree recursively through that parent. `Root.isValid()` always returns
`true`.

Every `Branch.onLoop()` walks insertion order and executes the first non-null,
valid child when the branch belongs to a tree. Immediately before execution it
sets the tree's current branch name to the executing branch class's simple name
and the current leaf name to the selected child's simple name. If no child is
valid, it returns a Gaussian idle delay with mean `350`, deviation `250`, and
the same one-deviation rejection boundary as the source (`100 < delay < 600`).

```java
@ScriptManifest(
    name = "Tree example", author = "Example Author",
    category = Category.UTILITY, version = 1.0)
public final class TreeExample extends TreeScript
{
    @Override
    public void onStart()
    {
        addBranches(new BankingBranch().addLeaves(
            new OpenBankLeaf(),
            new DepositLeaf()));
    }
}

final class BankingBranch extends Branch
{
    @Override public boolean isValid() { return Inventory.isFull(); }
}

final class OpenBankLeaf extends Leaf
{
    @Override public boolean isValid() { return !Bank.isOpen(); }
    @Override public int onLoop()
    {
        Bank.closest();
        return 600;
    }
}
```

The complete framework behavior is covered by `ScriptFrameworkTest`: priority
selection, the source fail-limit boundary, tree traversal and names, discovery,
start/paint/pause/resume/stop, and exit are all exercised.
