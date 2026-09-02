package com.dreambotreborn.devtools;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Immutable, Swing-safe view of a live RuneScape widget. */
public final class WidgetSnapshot
{
    public final String key;
    public final String relation;
    public final int id;
    public final int groupId;
    public final int childId;
    public final int index;
    public final int type;
    public final String typeName;
    public final int contentType;
    public final String name;
    public final String text;
    public final Rectangle bounds;
    public final boolean hidden;
    public final boolean selfHidden;
    public final int itemId;
    public final int itemQuantity;
    public final int scrollX;
    public final int scrollY;
    public final int scrollWidth;
    public final int scrollHeight;
    public final List<String> actions;
    public final List<WidgetSnapshot> children;

    WidgetSnapshot(
        String key,
        String relation,
        int id,
        int index,
        int type,
        String typeName,
        int contentType,
        String name,
        String text,
        Rectangle bounds,
        boolean hidden,
        boolean selfHidden,
        int itemId,
        int itemQuantity,
        int scrollX,
        int scrollY,
        int scrollWidth,
        int scrollHeight,
        List<String> actions,
        List<WidgetSnapshot> children)
    {
        this.key = key;
        this.relation = relation;
        this.id = id;
        this.groupId = id >>> 16;
        this.childId = id & 0xffff;
        this.index = index;
        this.type = type;
        this.typeName = typeName;
        this.contentType = contentType;
        this.name = name;
        this.text = text;
        this.bounds = bounds == null ? null : new Rectangle(bounds);
        this.hidden = hidden;
        this.selfHidden = selfHidden;
        this.itemId = itemId;
        this.itemQuantity = itemQuantity;
        this.scrollX = scrollX;
        this.scrollY = scrollY;
        this.scrollWidth = scrollWidth;
        this.scrollHeight = scrollHeight;
        this.actions = Collections.unmodifiableList(new ArrayList<>(actions));
        this.children = Collections.unmodifiableList(new ArrayList<>(children));
    }

    static WidgetSnapshot loginElement(
        String key,
        String relation,
        String typeName,
        String name,
        String state,
        Rectangle bounds,
        List<String> actions,
        List<WidgetSnapshot> children)
    {
        return new WidgetSnapshot(
            key, relation, -1, -1, -1, typeName, 0, name, state, bounds,
            false, false, -1, 0, 0, 0, 0, 0, actions, children);
    }

    public boolean isLoginElement()
    {
        return id == -1;
    }

    public String identifier()
    {
        if (isLoginElement())
        {
            return name;
        }
        return groupId + ":" + childId + (index >= 0 ? " [" + index + "]" : "");
    }

    /** Compact name used by the inspector tree. */
    public String displayName()
    {
        if (isLoginElement())
        {
            return name + " — " + typeName + (text.isEmpty() ? "" : " (" + text + ")");
        }

        StringBuilder label = new StringBuilder();
        label.append(groupId).append(':').append(childId);
        if (index >= 0)
        {
            label.append(" [").append(index).append(']');
        }
        label.append(' ').append(typeName);

        String description = !plainText(name).isEmpty() ? plainText(name) : plainText(text);
        if (!description.isEmpty())
        {
            label.append(" — ").append(abbreviate(description, 34));
        }
        if (hidden)
        {
            label.append(" (hidden)");
        }
        return label.toString();
    }

    /** Lower-case searchable content for filtering the tree. */
    String searchText()
    {
        return (displayName() + ' ' + relation + ' ' + name + ' ' + text + ' ' + actions)
            .toLowerCase(Locale.ROOT);
    }

    private static String plainText(String value)
    {
        if (value == null)
        {
            return "";
        }
        return value.replaceAll("<[^>]*>", "")
            .replace('\n', ' ')
            .replace('\r', ' ')
            .trim();
    }

    private static String abbreviate(String value, int length)
    {
        return value.length() <= length ? value : value.substring(0, length - 1) + '\u2026';
    }
}
