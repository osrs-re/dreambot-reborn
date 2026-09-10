package com.dreambotreborn.api.methods.ignore;

import com.dreambotreborn.api.methods.friend.NameProvider;

public class Ignored extends NameProvider
{
    Ignored(net.runelite.api.Ignore ignored)
    {
        super(ignored == null ? "" : ignored.getName(), ignored == null ? "" : ignored.getPrevName());
    }
    public static boolean hasIgnored() { return IgnoredProvider.getSize() > 0; }
}
