package com.dreambotreborn.api.methods.grandexchange;

import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.methods.container.impl.ContainerType;
import com.dreambotreborn.api.wrappers.interactive.Identifiable;
import com.dreambotreborn.api.wrappers.items.Item;
import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.GrandExchangeOfferState;

/** Immutable view of one Grand Exchange offer slot. */
public class GrandExchangeItem implements Identifiable
{
    private final GrandExchangeOffer offer;
    private final int slot;

    public GrandExchangeItem(Object offer, int slot)
    {
        if (offer != null && !(offer instanceof GrandExchangeOffer))
            throw new IllegalArgumentException("offer must be a RuneLite GrandExchangeOffer");
        this.offer = (GrandExchangeOffer) offer;
        this.slot = slot;
    }

    public Status getStatus()
    {
        GrandExchangeOfferState state = offer == null ? null : offer.getState();
        if (state == null || state == GrandExchangeOfferState.EMPTY) return Status.EMPTY;
        switch (state)
        {
            case BUYING: return Status.BUY;
            case BOUGHT:
            case CANCELLED_BUY: return Status.BUY_COLLECT;
            case SELLING: return Status.SELL;
            case SOLD:
            case CANCELLED_SELL: return Status.SELL_COLLECT;
            default: return Status.EMPTY;
        }
    }

    public boolean isCanceled()
    {
        return offer != null && (offer.getState() == GrandExchangeOfferState.CANCELLED_BUY
            || offer.getState() == GrandExchangeOfferState.CANCELLED_SELL);
    }

    public boolean isReadyToCollect()
    {
        return getStatus() == Status.BUY_COLLECT || getStatus() == Status.SELL_COLLECT;
    }

    public boolean isBuyOffer()
    {
        return getStatus() == Status.BUY || getStatus() == Status.BUY_COLLECT
            && offer != null && offer.getState() != GrandExchangeOfferState.CANCELLED_SELL;
    }

    public boolean isSellOffer()
    {
        return getStatus() == Status.SELL || getStatus() == Status.SELL_COLLECT
            && offer != null && offer.getState() != GrandExchangeOfferState.CANCELLED_BUY;
    }

    public int getValue() { return getTransferredValue(); }

    public Item getItem()
    {
        int id = getId();
        if (id < 0) return null;
        return new Item(new net.runelite.api.Item(id, Math.max(1, getAmount())), slot,
            ContainerType.INVENTORY, DreamBotRebornApi.requireClient().getItemDefinition(id), null);
    }

    public String getName()
    {
        net.runelite.api.ItemComposition definition = getId() < 0 ? null
            : DreamBotRebornApi.requireClient().getItemDefinition(getId());
        return definition == null || definition.getName() == null ? "" : definition.getName();
    }

    public int getSlot() { return slot; }
    public int getTransferredAmount() { return offer == null ? 0 : offer.getQuantitySold(); }
    public int getTransferredValue() { return offer == null ? 0 : offer.getSpent(); }
    public byte getByte() { return (byte) getStatus().getStatusValue(); }
    @Override public int getId() { return offer == null ? -1 : offer.getItemId(); }
    public int getPrice() { return offer == null ? 0 : offer.getPrice(); }
    public int getAmount() { return offer == null ? 0 : offer.getTotalQuantity(); }
    public GrandExchangeOffer unwrap() { return offer; }
}
