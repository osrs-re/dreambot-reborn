package com.dreambotreborn.api.script;

import com.dreambotreborn.api.methods.MethodProvider;
import com.dreambotreborn.api.script.frameworks.utility.Sleepable;

/** One condition/action pair used by {@code TaskScript}. */
public abstract class TaskNode extends MethodProvider implements Sleepable
{
    public int priority()
    {
        return 1;
    }

    public abstract boolean accept();

    public abstract int execute();
}
