package com.dreambotreborn.api.script.event.impl;

import java.util.EventListener;
import com.dreambotreborn.api.script.event.ScriptEvent;
import com.dreambotreborn.api.script.listener.ActionListener;
import com.dreambotreborn.api.wrappers.widgets.MenuRow;

public class ActionEvent extends ScriptEvent
{
    private final int opCode;
    private final int variable;
    private final int xCode;
    private final int yCode;
    private final String action;
    private final String option;
    private final int mouseX;
    private final int mouseY;

    public ActionEvent(int opCode, int variable, int xCode, int yCode, int id,
                       int itemId, String action, String option, int mouseX, int mouseY)
    {
        this.opCode = opCode;
        this.variable = variable;
        this.xCode = xCode;
        this.yCode = yCode;
        this.action = action == null ? "" : action;
        this.option = option == null ? "" : option;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    @Override public void dispatch(EventListener listener)
    {
        if (listener instanceof ActionListener)
            ((ActionListener) listener).onAction(new MenuRow(action, option, variable,
                xCode, yCode, opCode, -1), mouseX, mouseY);
    }
    public int getXCode() { return xCode; }
    public int getYCode() { return yCode; }
    public int getOpCode() { return opCode; }
    public int getVariable() { return variable; }
    public String getAction() { return action; }
    public String getOption() { return option; }
    public int getMouseX() { return mouseX; }
    public int getMouseY() { return mouseY; }
    @Override public String toString() { return "ActionEvent{" + action + ' ' + option + '}'; }
}
