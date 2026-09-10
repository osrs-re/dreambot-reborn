package com.dreambotreborn.api.methods.favour;

import com.dreambotreborn.api.DreamBotRebornApi;

/** Historical Kourend favour values retained for legacy-script compatibility. */
public enum House
{
    ARCEUUS(4896), HOSIDIUS(4895), LOVAKENGJ(4898), PISCARILIUS(4899), SHAYZIEN(4894);
    private final int varbit;
    House(int varbit) { this.varbit = varbit; }
    public int getVarbit() { return varbit; }
    public int getValue()
    {
        try { return DreamBotRebornApi.requireClient().getVarbitValue(varbit); }
        catch (RuntimeException unavailable) { return 0; }
    }
    public double getPercent() { return Math.max(0, Math.min(100, getValue() / 10.0)); }
}
