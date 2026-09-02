package com.dreambotreborn.api.script.frameworks.treebranch;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import com.dreambotreborn.api.methods.Calculations;

/** Ordered collection that executes its first valid child. */
public abstract class Branch extends Leaf
{
    private final List<Leaf> children = new LinkedList<>();

    public final Branch addLeaves(Leaf... leaves)
    {
        Collections.addAll(children, leaves);
        children.forEach(leaf -> leaf.setParent(this));
        return this;
    }

    public final void clear()
    {
        children.clear();
    }

    @Override
    public int onLoop()
    {
        for (Leaf leaf : children)
        {
            if (leaf == null || !leaf.isValid() || getTree() == null)
            {
                continue;
            }
            getTree().setCurrentBranchName(getClass().getSimpleName());
            getTree().setCurrentLeafName(leaf.getClass().getSimpleName());
            return leaf.onLoop();
        }
        return (int) Calculations.nextGaussianRandom(350.0D, 250.0D);
    }

    public List<Leaf> getChildren()
    {
        return children;
    }
}
