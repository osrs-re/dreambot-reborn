package com.dreambotreborn.api.methods.cs2;

import com.dreambotreborn.api.wrappers.widgets.WidgetChild;

/** Mutable, client-independent view of a RuneScript/CS2 invocation. */
public class RuneScriptEvent
{
    private Object event;
    private Object[] arguments = new Object[0];
    private int var;
    private WidgetChild dragTarget;
    private WidgetChild widget;
    private int type;
    private boolean mouseInput;
    private int mouseX;
    private int mouseY;
    private String opbase = "";
    private boolean consumed;
    private int varcIndex = -1;
    private Integer varcIntVal;
    private String varcStringVal;

    public RuneScriptEvent() { }
    public RuneScriptEvent(Object event) { this.event = event; }
    public void createEvent(Object... arguments)
    { this.arguments = arguments == null ? new Object[0] : arguments.clone(); }
    public Object[] getArguments() { return arguments.clone(); }
    public void setVar(int value) { var = value; }
    public int getVar() { return var; }
    public void setDragTarget(WidgetChild value) { dragTarget = value; }
    public WidgetChild getDragTarget() { return dragTarget; }
    public void setWidget(WidgetChild value) { widget = value; }
    public WidgetChild getWidget() { return widget; }
    public void setType(int value) { type = value; }
    public int getType() { return type; }
    public void setMouseInput(boolean value) { mouseInput = value; }
    public boolean getMouseInput() { return mouseInput; }
    public void setMouseX(int value) { mouseX = value; }
    public int getMouseX() { return mouseX; }
    public void setMouseY(int value) { mouseY = value; }
    public int getMouseY() { return mouseY; }
    public void setOpbase(String value) { opbase = value == null ? "" : value; }
    public String getOpbase() { return opbase; }
    public Object getEvent() { return event; }
    public boolean isConsumed() { return consumed; }
    public int getVarcIndex() { return varcIndex; }
    public Integer getVarcIntVal() { return varcIntVal; }
    public String getVarcStringVal() { return varcStringVal; }
    public void setEvent(Object value) { event = value; }
    public void setConsumed(boolean value) { consumed = value; }
    public void setVarcIndex(int value) { varcIndex = value; }
    public void setVarcIntVal(Integer value) { varcIntVal = value; }
    public void setVarcStringVal(String value) { varcStringVal = value; }
}
