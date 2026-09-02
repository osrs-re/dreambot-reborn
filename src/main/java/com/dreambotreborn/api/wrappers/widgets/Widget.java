package com.dreambotreborn.api.wrappers.widgets;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.runelite.api.MenuAction;
import com.dreambotreborn.api.input.VirtualMouse;
import com.dreambotreborn.api.internal.MouseTarget;
import com.dreambotreborn.api.wrappers.interactive.Entity;
import com.dreambotreborn.api.wrappers.interactive.Interactable;
import com.dreambotreborn.api.utilities.Await;

/** Immutable snapshot of a RuneLite widget. */
public class Widget implements Interactable, MouseTarget
{
    public final int id;
    public final int groupId;
    public final int childId;
    public final int parentId;
    public final int index;
    public final int type;
    public final String name;
    public final String text;
    public final List<String> actions;
    public final Rectangle bounds;
    public final boolean visible;
    public final int itemId;
    public final int itemQuantity;

    private final net.runelite.api.widgets.Widget widget;
    private final String[] actionSlots;

    public Widget(net.runelite.api.widgets.Widget widget)
    {
        this.widget = widget;
        this.id = widget.getId();
        this.groupId = id >>> 16;
        this.childId = id & 0xffff;
        this.parentId = widget.getParentId();
        this.index = widget.getIndex();
        this.type = widget.getType();
        this.name = Entity.clean(widget.getName());
        this.text = Entity.clean(widget.getText());
        this.actionSlots = Entity.copySlots(widget.getActions());
        this.actions = Entity.compact(actionSlots);
        this.bounds = widget.getBounds() == null ? null : new Rectangle(widget.getBounds());
        this.visible = !widget.isHidden() && bounds != null && bounds.width > 0 && bounds.height > 0;
        this.itemId = widget.getItemId();
        this.itemQuantity = widget.getItemQuantity();
    }

    @Override
    public int getId()
    {
        return id;
    }

    public int getGroupId()
    {
        return groupId;
    }

    public int getChildId()
    {
        return childId;
    }

    public int getParentId()
    {
        return parentId;
    }

    public int getIndex()
    {
        return index;
    }

    public int getType()
    {
        return type;
    }

    @Override
    public String getName()
    {
        return name;
    }

    public String getText()
    {
        return text;
    }

    @Override
    public List<String> getActions()
    {
        return actions;
    }

    public Rectangle getBounds()
    {
        return bounds == null ? null : new Rectangle(bounds);
    }

    public boolean isVisible()
    {
        return visible;
    }

    public int getItemId()
    {
        return itemId;
    }

    public int getItemQuantity()
    {
        return itemQuantity;
    }

    @Override
    public boolean hasAction(String action)
    {
        return action != null && actionIndex(action) >= 0;
    }

    @Override
    public boolean interact()
    {
        return Await.success(interactAsync());
    }

    @Override
    public boolean interact(String action)
    {
        return Await.success(interactAsync(action));
    }

    @Override
    public CompletableFuture<Boolean> interactAsync()
    {
        return VirtualMouse.interact(this);
    }

    @Override
    public CompletableFuture<Boolean> interactAsync(String action)
    {
        return VirtualMouse.interact(this, action);
    }

    /** Performs an ordinary visible left click even when the widget has no menu actions. */
    public boolean click()
    {
        return Await.success(clickAsync());
    }

    public CompletableFuture<Boolean> clickAsync()
    {
        if (bounds == null)
        {
            return CompletableFuture.completedFuture(false);
        }
        int x = bounds.x + bounds.width / 2;
        int y = bounds.y + bounds.height / 2;
        return VirtualMouse.moveTo(x, y).thenApply(moved ->
        {
            if (moved)
            {
                VirtualMouse.click();
            }
            return moved;
        });
    }

    @Override
    public int firstActionIndex()
    {
        for (int i = 0; i < actionSlots.length; i++)
        {
            if (actionSlots[i] != null)
            {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int actionIndex(String action)
    {
        if (action != null)
        {
            for (int i = 0; i < actionSlots.length; i++)
            {
                if (actionSlots[i] != null && actionSlots[i].equalsIgnoreCase(action))
                {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public String actionAt(int index)
    {
        return index < 0 || index >= actionSlots.length ? null : actionSlots[index];
    }

    @Override
    public Shape clickShape(Component component)
    {
        return bounds;
    }

    @Override
    public InteractionSpec interactionAt(int actionIndex)
    {
        if (actionIndex < 0 || actionAt(actionIndex) == null)
        {
            return null;
        }
        return new InteractionSpec(
            index,
            id,
            MenuAction.CC_OP,
            actionIndex + 1,
            actionAt(actionIndex),
            name.isEmpty() ? text : name,
            net.runelite.api.WorldView.TOPLEVEL);
    }

    public net.runelite.api.widgets.Widget unwrap()
    {
        return widget;
    }

    @Override
    public String toString()
    {
        return "Widget{" + "id=" + id + ", groupId=" + groupId +
            ", childId=" + childId + ", text='" + text + '\'' + ", visible=" + visible + '}';
    }
}
