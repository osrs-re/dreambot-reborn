package com.dreambotreborn.api.script.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import com.dreambotreborn.api.script.AbstractScript;
import com.dreambotreborn.api.script.TaskNode;

/** Priority-ordered condition/action framework matching DreamBot's TaskScript. */
public abstract class TaskScript extends AbstractScript
{
    private final List<TaskNode> nodes = new ArrayList<>();
    private int failLimit = -1;
    private int failures = -1;
    private TaskNode lastTaskNode;

    public void addNodes(TaskNode... nodes)
    {
        this.nodes.addAll(Arrays.asList(nodes));
    }

    public void removeNodes(TaskNode... nodes)
    {
        if (!this.nodes.isEmpty())
        {
            for (TaskNode node : nodes)
            {
                this.nodes.remove(node);
            }
        }
    }

    public TaskNode[] getNodes()
    {
        return nodes.toArray(new TaskNode[0]);
    }

    @Override
    public int onLoop()
    {
        if (failLimit > 0 && failures > failLimit)
        {
            System.err.println("[Script/warn] TaskScript fail limit exceeded; stopping script");
            return -1;
        }

        TaskNode accepted = nodes.stream()
            .sorted(Comparator.comparingInt(TaskNode::priority).reversed())
            .filter(TaskNode::accept)
            .findFirst()
            .orElse(null);
        if (accepted != null)
        {
            failures = 0;
            lastTaskNode = accepted;
            return accepted.execute();
        }

        failures++;
        return 1_000;
    }

    public TaskNode getLastTaskNode()
    {
        return lastTaskNode;
    }

    public void setFailLimit(int failLimit)
    {
        this.failLimit = failLimit;
    }
}
