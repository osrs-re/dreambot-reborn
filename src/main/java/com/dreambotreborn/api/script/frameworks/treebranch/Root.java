package com.dreambotreborn.api.script.frameworks.treebranch;

import java.util.Collections;

/** Always-valid root branch owned by one {@link TreeScript}. */
public class Root extends Branch
{
    private final TreeScript tree;

    public Root(TreeScript tree)
    {
        this.tree = tree;
    }

    @Override
    public TreeScript getTree()
    {
        return tree;
    }

    public final Root addBranches(Branch... branches)
    {
        Collections.addAll(getChildren(), branches);
        for (Leaf child : getChildren())
        {
            child.setParent(this);
        }
        return this;
    }

    @Override
    public boolean isValid()
    {
        return true;
    }
}
