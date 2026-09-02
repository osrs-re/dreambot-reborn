package com.dreambotreborn.api.script.frameworks.treebranch;

/** Executable end node, and base type for nested branches. */
public abstract class Leaf
{
    private Branch parent;

    public abstract boolean isValid();

    public abstract int onLoop();

    public TreeScript getTree()
    {
        return parent == null ? null : parent.getTree();
    }

    public Branch getParent()
    {
        return parent;
    }

    public void setParent(Branch parent)
    {
        this.parent = parent;
    }
}
