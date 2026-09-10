package com.dreambotreborn.api.data.requirements;

import com.dreambotreborn.api.methods.interactive.Players;
import com.dreambotreborn.api.methods.map.Area;
import com.dreambotreborn.api.methods.map.Tile;

public class LocationRequirement extends Requirement
{
    private Area area;
    public LocationRequirement(Area area) { this.area = area; }
    public LocationRequirement(Tile center, int radius)
    { this(center == null ? null : center.getArea(Math.max(0, radius))); }
    @Override public boolean meetsRequirement()
    { return area != null && Players.getLocal() != null && area.contains(Players.getLocal()); }
    public Area getArea() { return area; }
    public void setArea(Area value) { area = value; }
}
