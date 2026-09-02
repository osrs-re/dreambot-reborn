package com.dreambotreborn.api.methods.input.mouse;

import java.awt.Rectangle;
import com.dreambotreborn.api.input.mouse.algorithm.MouseProfile;

/** Context passed to a mouse movement or interaction. */
public class MouseSettings
{
    public enum Hand
    {
        RIGHT,
        LEFT
    }

    private Rectangle rectDest;
    private boolean walking;
    private String action;
    private String object;
    private boolean multiThread;
    private boolean leftClick = true;
    private boolean rightClick;
    private boolean click = true;
    private boolean overshoot = MouseProfile.isOvershootEnabled();
    private boolean drag;
    private Rectangle bounds;
    private Hand preferredHand = Hand.RIGHT;
    private boolean useMiddleMouseInInteracts;

    public static void resetMouseTimings()
    {
        MouseProfile.setMouseTiming(MouseProfile.getDefaultMouseTiming());
    }

    public static MouseTiming getMouseTiming()
    {
        return MouseProfile.getMouseTiming();
    }

    public static void setMouseTiming(MouseTiming timing)
    {
        MouseProfile.setMouseTiming(timing);
    }

    public static int getSpeed()
    {
        return (int) Math.round(MouseProfile.getSpeed());
    }

    public static void setSpeed(int speed)
    {
        MouseProfile.setSpeed(speed);
    }

    public MouseSettings resetSpeed()
    {
        MouseProfile.setSpeed(MouseProfile.getDEFAULT_SPEED());
        return this;
    }

    public Rectangle getRectDest()
    {
        return rectDest == null ? null : new Rectangle(rectDest);
    }

    public MouseSettings setRectDest(Rectangle rectDest)
    {
        this.rectDest = rectDest == null ? null : new Rectangle(rectDest);
        return this;
    }

    public boolean isWalking()
    {
        return walking;
    }

    public MouseSettings setWalking(boolean walking)
    {
        this.walking = walking;
        return this;
    }

    public MouseSettings setTarget(String action, String object)
    {
        this.action = action;
        this.object = object;
        return this;
    }

    public String getAction()
    {
        return action;
    }

    public MouseSettings setAction(String action)
    {
        this.action = action;
        return this;
    }

    public String getObject()
    {
        return object;
    }

    public MouseSettings setObject(String object)
    {
        this.object = object;
        return this;
    }

    public boolean isMultiThread()
    {
        return multiThread;
    }

    public void setMultiThread(boolean multiThread)
    {
        this.multiThread = multiThread;
    }

    public boolean isLeftClick()
    {
        return leftClick;
    }

    public MouseSettings setLeftClick(boolean leftClick)
    {
        this.leftClick = leftClick;
        return this;
    }

    public boolean isRightClick()
    {
        return rightClick;
    }

    public MouseSettings setRightClick(boolean rightClick)
    {
        this.rightClick = rightClick;
        return this;
    }

    public boolean isClick()
    {
        return click;
    }

    public MouseSettings setClick(boolean click)
    {
        this.click = click;
        return this;
    }

    public boolean isOvershoot()
    {
        return overshoot;
    }

    public MouseSettings setOvershoot(boolean overshoot)
    {
        this.overshoot = overshoot;
        return this;
    }

    public boolean isDrag()
    {
        return drag;
    }

    public MouseSettings setDrag(boolean drag)
    {
        this.drag = drag;
        return this;
    }

    public Rectangle getBounds()
    {
        return bounds == null ? null : new Rectangle(bounds);
    }

    public MouseSettings setBounds(Rectangle bounds)
    {
        this.bounds = bounds == null ? null : new Rectangle(bounds);
        return this;
    }

    public void reset()
    {
        rectDest = null;
        walking = false;
        action = null;
        object = null;
        multiThread = false;
        leftClick = true;
        rightClick = false;
        click = true;
        overshoot = true;
        drag = false;
        bounds = null;
        preferredHand = Hand.RIGHT;
        useMiddleMouseInInteracts = false;
    }

    public Hand getPreferredHand()
    {
        return preferredHand;
    }

    public void setPreferredHand(Hand preferredHand)
    {
        this.preferredHand = preferredHand == null ? Hand.RIGHT : preferredHand;
    }

    public boolean isUseMiddleMouseInInteracts()
    {
        return useMiddleMouseInInteracts;
    }

    public void setUseMiddleMouseInInteracts(boolean value)
    {
        useMiddleMouseInInteracts = value;
    }

    public MouseSettings copy()
    {
        MouseSettings result = new MouseSettings();
        result.rectDest = getRectDest();
        result.walking = walking;
        result.action = action;
        result.object = object;
        result.multiThread = multiThread;
        result.leftClick = leftClick;
        result.rightClick = rightClick;
        result.click = click;
        result.overshoot = overshoot;
        result.drag = drag;
        result.bounds = getBounds();
        result.preferredHand = preferredHand;
        result.useMiddleMouseInInteracts = useMiddleMouseInInteracts;
        return result;
    }
}
