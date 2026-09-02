package com.dreambotreborn.api.script.frameworks.treebranch;

import com.dreambotreborn.api.script.AbstractScript;

/** Hierarchical first-valid-node script framework matching DreamBot's package layout. */
public abstract class TreeScript extends AbstractScript
{
    private final Root root = new Root(this);
    private String currentBranchName;
    private String currentLeafName;

    public final Root addBranches(Leaf... leaves)
    {
        root.addLeaves(leaves);
        return root;
    }

    public final void clear()
    {
        root.clear();
    }

    @Override
    public int onLoop()
    {
        return root.onLoop();
    }

    public Root getRoot()
    {
        return root;
    }

    public String getCurrentBranchName()
    {
        return currentBranchName;
    }

    public void setCurrentBranchName(String currentBranchName)
    {
        this.currentBranchName = currentBranchName;
    }

    public String getCurrentLeafName()
    {
        return currentLeafName;
    }

    public void setCurrentLeafName(String currentLeafName)
    {
        this.currentLeafName = currentLeafName;
    }
}
