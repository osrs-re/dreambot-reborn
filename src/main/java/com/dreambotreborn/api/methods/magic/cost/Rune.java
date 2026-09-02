package com.dreambotreborn.api.methods.magic.cost;

public class Rune
{
    private final String name;
    private final int amount;
    public Rune(String name, int amount)
    {
        this.name = name == null ? "" : name;
        this.amount = Math.max(0, amount);
    }
    public static Rune[] build(int[] amounts, String[] names)
    {
        int length = Math.min(amounts == null ? 0 : amounts.length, names == null ? 0 : names.length);
        Rune[] result = new Rune[length];
        for (int i = 0; i < length; i++) result[i] = new Rune(names[i], amounts[i]);
        return result;
    }
    public String getName() { return name; }
    public int getAmount() { return amount; }
    @Override public String toString() { return amount + " x " + name; }
}
