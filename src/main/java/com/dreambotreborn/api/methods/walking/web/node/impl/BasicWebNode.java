package com.dreambotreborn.api.methods.walking.web.node.impl;

import com.dreambotreborn.api.methods.walking.web.node.AbstractWebNode;
import com.dreambotreborn.api.methods.walking.web.node.WebNodeType;
import com.dreambotreborn.api.utilities.impl.Condition;

public class BasicWebNode extends AbstractWebNode
{
    private double randomizationFactor;
    private Condition valid;
    public BasicWebNode(int x, int y) { this(x, y, 0); }
    public BasicWebNode(int x, int y, int z) { super(x, y, z); }
    @Override public WebNodeType getType() { return WebNodeType.BASIC_NODE; }
    @Override public boolean isValid() { return valid == null || valid.verify(); }
    public boolean execute(AbstractWebNode next) { return execute(); }
    public double getRandomizationFactor() { return randomizationFactor; }
    public void setRandomizationFactor(double value) { randomizationFactor = Math.max(0, value); }
    public Condition getValid() { return valid; }
    public void setValid(Condition value) { valid = value; }
}
