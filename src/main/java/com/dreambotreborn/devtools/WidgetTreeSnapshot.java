package com.dreambotreborn.devtools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** One immutable capture of all currently attached widget roots. */
public final class WidgetTreeSnapshot
{
    static final WidgetTreeSnapshot EMPTY = new WidgetTreeSnapshot(
        0L, Collections.emptyList(), 0, "waiting for client UI");

    public final long version;
    public final List<WidgetSnapshot> roots;
    public final int widgetCount;
    public final String source;

    WidgetTreeSnapshot(long version, List<WidgetSnapshot> roots, int widgetCount, String source)
    {
        this.version = version;
        this.roots = Collections.unmodifiableList(new ArrayList<>(roots));
        this.widgetCount = widgetCount;
        this.source = source;
    }
}
