package com.dreambotreborn.api.methods.input.mouse;

/** Last known RuneScape-style mouse crosshair state. */
public enum CrosshairState
{
    IDLE(0),
    CLICKED(1),
    INTERACTED(2);

    private final int id;

    CrosshairState(int id)
    {
        this.id = id;
    }

    public int getId()
    {
        return id;
    }

    @Override
    public String toString()
    {
        return name().toLowerCase();
    }
}
