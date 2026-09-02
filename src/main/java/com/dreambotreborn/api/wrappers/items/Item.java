package com.dreambotreborn.api.wrappers.items;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.runelite.api.ItemComposition;
import net.runelite.api.MenuAction;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.input.VirtualMouse;
import com.dreambotreborn.api.input.mouse.destination.impl.EntityDestination;
import com.dreambotreborn.api.internal.Containers;
import com.dreambotreborn.api.internal.MouseTarget;
import com.dreambotreborn.api.methods.container.impl.ContainerType;
import com.dreambotreborn.api.wrappers.interactive.Entity;
import com.dreambotreborn.api.wrappers.interactive.Interactable;
import com.dreambotreborn.api.wrappers.interactive.Identifiable;
import com.dreambotreborn.api.utilities.Await;

/** Immutable inventory, equipment, or bank item snapshot. */
public class Item implements Interactable, MouseTarget, Identifiable
{
    public final int id;
    public final String name;
    public final int amount;
    public final int slot;
    public final ContainerType container;
    public final List<String> actions;

    private final String[] actionSlots;
    private final Rectangle bounds;
    private final int widgetId;
    private final ItemComposition definition;

    public Item(
        net.runelite.api.Item item,
        int slot,
        ContainerType container,
        ItemComposition definition,
        net.runelite.api.widgets.Widget widget)
    {
        this.id = item.getId();
        this.name = Entity.clean(definition == null ? "" : definition.getName());
        this.amount = item.getQuantity();
        this.slot = slot;
        this.container = container;
        this.definition = definition;
        String[] widgetActions = widget == null ? null : widget.getActions();
        String[] definitionActions = definition == null ? null : definition.getInventoryActions();
        this.actionSlots = Entity.copySlots(hasAny(widgetActions) ? widgetActions : definitionActions);
        this.actions = Entity.compact(actionSlots);
        this.bounds = widget == null || widget.getBounds() == null
            ? null : new Rectangle(widget.getBounds());
        this.widgetId = widget == null ? Containers.widgetId(container) : widget.getId();
    }

    @Override
    public int getId()
    {
        return id;
    }

    @Override
    public String getName()
    {
        return name;
    }

    public int getAmount()
    {
        return amount;
    }

    public int getQuantity()
    {
        return amount;
    }

    public int getSlot()
    {
        return slot;
    }

    public ContainerType getContainerType()
    {
        return container;
    }

    @Override
    public List<String> getActions()
    {
        return actions;
    }

    @Override
    public boolean hasAction(String action)
    {
        return action != null && ("Use".equalsIgnoreCase(action) || actionIndex(action) >= 0);
    }

    public boolean isStackable()
    {
        return definition != null && definition.isStackable();
    }

    public boolean isMembersOnly()
    {
        return definition != null && definition.isMembers();
    }

    public boolean isTradable()
    {
        return definition != null && definition.isTradeable();
    }

    public int getValue()
    {
        return definition == null ? 0 : definition.getPrice();
    }

    public int getHighAlchValue()
    {
        return definition == null ? 0 : definition.getHaPrice();
    }

    public boolean isNoted()
    {
        return definition != null && definition.getNote() != -1;
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
        if ("Use".equalsIgnoreCase(action))
        {
            return enqueueUse();
        }
        return VirtualMouse.interact(this, action);
    }

    private CompletableFuture<Boolean> enqueueUse()
    {
        return VirtualMouse.interact(new UseTarget(this));
    }

    public boolean useOn(Entity target)
    {
        return Await.success(useOnAsync(target));
    }

    public CompletableFuture<Boolean> useOnAsync(Entity target)
    {
        if (target == null)
        {
            return CompletableFuture.completedFuture(false);
        }
        return interactAsync("Use").thenCompose(selected -> selected
            ? VirtualMouse.moveTo(new EntityDestination(target)).thenCompose(moved -> moved
                ? VirtualMouse.click(com.dreambotreborn.api.input.event.impl.mouse.MouseButton.LEFT_CLICK)
                : CompletableFuture.completedFuture(false))
            : CompletableFuture.completedFuture(false));
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
        return bounds == null ? Containers.approximateSlotBounds(container, slot) : bounds;
    }

    @Override
    public InteractionSpec interactionAt(int index)
    {
        MenuAction[] menuActions =
        {
            MenuAction.ITEM_FIRST_OPTION,
            MenuAction.ITEM_SECOND_OPTION,
            MenuAction.ITEM_THIRD_OPTION,
            MenuAction.ITEM_FOURTH_OPTION,
            MenuAction.ITEM_FIFTH_OPTION
        };
        if (index < 0 || index >= menuActions.length || widgetId < 0)
        {
            return null;
        }
        return new InteractionSpec(
            slot,
            widgetId,
            DreamBotRebornApi.requireClient().isWidgetSelected()
                ? MenuAction.WIDGET_USE_ON_ITEM : menuActions[index],
            id,
            actionAt(index),
            name,
            net.runelite.api.WorldView.TOPLEVEL);
    }

    public Rectangle getBounds()
    {
        Shape shape = clickShape(null);
        return shape == null ? null : shape.getBounds();
    }

    public net.runelite.api.Item unwrap()
    {
        return new net.runelite.api.Item(id, amount);
    }

    @Override
    public String toString()
    {
        return "Item{" + "id=" + id + ", name='" + name + '\'' +
            ", amount=" + amount + ", slot=" + slot + ", container=" + container + '}';
    }

    private static boolean hasAny(String[] values)
    {
        if (values != null)
        {
            for (String value : values)
            {
                if (value != null && !value.isEmpty())
                {
                    return true;
                }
            }
        }
        return false;
    }

    private static final class UseTarget implements MouseTarget
    {
        private final Item item;

        private UseTarget(Item item)
        {
            this.item = item;
        }

        @Override
        public int firstActionIndex()
        {
            return 0;
        }

        @Override
        public int actionIndex(String action)
        {
            return "Use".equalsIgnoreCase(action) ? 0 : -1;
        }

        @Override
        public String actionAt(int index)
        {
            return index == 0 ? "Use" : null;
        }

        @Override
        public Shape clickShape(Component component)
        {
            return item.clickShape(component);
        }

        @Override
        public InteractionSpec interactionAt(int index)
        {
            if (index != 0 || item.widgetId < 0)
            {
                return null;
            }
            return new InteractionSpec(item.slot, item.widgetId, MenuAction.ITEM_USE,
                item.id, "Use", item.name, net.runelite.api.WorldView.TOPLEVEL);
        }
    }
}
