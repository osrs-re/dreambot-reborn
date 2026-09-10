package com.dreambotreborn.api.data.requirements;

import com.dreambotreborn.api.methods.favour.House;

public class FavourRequirement extends Requirement
{
    private int favour;
    private House house;
    public FavourRequirement(int favour, House house) { this.favour = favour; this.house = house; }
    @Override public boolean meetsRequirement()
    { return house != null && house.getPercent() >= favour; }
    public int getFavour() { return favour; }
    public House getHouse() { return house; }
    public void setFavour(int value) { favour = value; }
    public void setHouse(House value) { house = value; }
}
