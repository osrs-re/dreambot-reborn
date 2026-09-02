package com.dreambotreborn.api.methods.container.impl;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import com.dreambotreborn.api.wrappers.items.Item;

public class StandardDropPattern implements DropPattern
{
    public static final DropPattern LEFT_TO_RIGHT_THEN_DOWN =
        new StandardDropPattern(sequenceRows());
    public static final DropPattern TOP_TO_BOTTOM_THEN_RIGHT =
        new StandardDropPattern(sequenceColumns());
    private final Map<Integer, Integer> rank = new HashMap<>();
    public StandardDropPattern(int... slots)
    {
        if (slots != null) for (int i = 0; i < slots.length; i++) rank.put(slots[i], i);
    }
    @Override public Comparator<Item> getComparator()
    {
        return Comparator.comparingInt(item -> rank.getOrDefault(item.slot, item.slot + rank.size()));
    }
    private static int[] sequenceRows()
    {
        int[] values = new int[28]; for (int i = 0; i < values.length; i++) values[i] = i; return values;
    }
    private static int[] sequenceColumns()
    {
        int[] values = new int[28]; int cursor = 0;
        for (int column = 0; column < 4; column++)
            for (int row = 0; row < 7; row++) values[cursor++] = row * 4 + column;
        return values;
    }
}
