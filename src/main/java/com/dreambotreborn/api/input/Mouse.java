package com.dreambotreborn.api.input;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import com.dreambotreborn.api.input.event.impl.mouse.MouseButton;
import com.dreambotreborn.api.input.event.impl.mouse.impl.click.ClickMode;
import com.dreambotreborn.api.input.mouse.algorithm.MouseAlgorithm;
import com.dreambotreborn.api.input.mouse.algorithm.MouseMovementAlgorithm;
import com.dreambotreborn.api.input.mouse.destination.AbstractMouseDestination;
import com.dreambotreborn.api.input.mouse.destination.impl.EntityDestination;
import com.dreambotreborn.api.input.mouse.destination.impl.MiniMapTileDestination;
import com.dreambotreborn.api.input.mouse.destination.impl.PointDestination;
import com.dreambotreborn.api.input.mouse.destination.impl.shape.RectangleDestination;
import com.dreambotreborn.api.methods.input.mouse.CrosshairState;
import com.dreambotreborn.api.methods.input.mouse.MouseSettings;
import com.dreambotreborn.api.methods.input.mouse.MouseTracker;
import com.dreambotreborn.api.methods.interactive.GameObjects;
import com.dreambotreborn.api.methods.interactive.NPCs;
import com.dreambotreborn.api.methods.interactive.Players;
import com.dreambotreborn.api.methods.map.Tile;
import com.dreambotreborn.api.utilities.Await;
import com.dreambotreborn.api.utilities.Sleep;
import com.dreambotreborn.api.utilities.impl.Condition;
import com.dreambotreborn.api.wrappers.interactive.Entity;

/** DreamBot-compatible synchronous facade over the asynchronous software mouse. */
public final class Mouse
{
    private static volatile boolean alwaysHop;
    private static volatile boolean forceDrag;
    private static volatile int lastCrosshairColorId;

    private Mouse()
    {
    }

    public static boolean move(int x, int y) { return Await.success(moveAsync(x, y)); }
    public static boolean move(Point point) { return Await.success(moveAsync(point)); }
    public static boolean move(Rectangle rectangle) { return Await.success(moveAsync(rectangle)); }
    public static boolean move(Entity entity) { return Await.success(moveAsync(entity)); }
    public static boolean move(Tile tile) { return Await.success(moveAsync(tile)); }
    public static boolean move(AbstractMouseDestination<?> destination)
    {
        return Await.success(moveAsync(destination));
    }
    public static boolean move() { return Await.success(moveAsync()); }

    public static CompletableFuture<Boolean> moveAsync(int x, int y)
    {
        return VirtualMouse.moveTo(x, y);
    }

    public static CompletableFuture<Boolean> moveAsync(Point point)
    {
        return point == null ? CompletableFuture.completedFuture(false)
            : VirtualMouse.moveTo(new PointDestination(point));
    }

    public static CompletableFuture<Boolean> moveAsync(Rectangle rectangle)
    {
        return rectangle == null ? CompletableFuture.completedFuture(false)
            : VirtualMouse.moveTo(new RectangleDestination(rectangle));
    }

    public static CompletableFuture<Boolean> moveAsync(Entity entity)
    {
        return entity == null ? CompletableFuture.completedFuture(false)
            : VirtualMouse.moveTo(new EntityDestination(entity));
    }

    public static CompletableFuture<Boolean> moveAsync(Tile tile)
    {
        return tile == null ? CompletableFuture.completedFuture(false)
            : VirtualMouse.moveTo(new MiniMapTileDestination(tile));
    }

    public static CompletableFuture<Boolean> moveAsync(AbstractMouseDestination<?> destination)
    {
        return destination == null ? CompletableFuture.completedFuture(false)
            : VirtualMouse.moveTo(destination);
    }

    public static CompletableFuture<Boolean> moveAsync()
    {
        java.util.concurrent.ThreadLocalRandom random =
            java.util.concurrent.ThreadLocalRandom.current();
        return moveAsync(random.nextInt(0, 765), random.nextInt(0, 503));
    }

    public static boolean click() { return Await.success(clickAsync()); }
    public static boolean click(boolean rightClick) { return Await.success(clickAsync(rightClick)); }
    public static boolean click(MouseButton button) { return Await.success(clickAsync(button)); }
    public static boolean click(ClickMode mode) { return Await.success(clickAsync(mode)); }
    public static boolean click(Point point) { return Await.success(clickAsync(point)); }
    public static boolean click(Point point, boolean rightClick)
    {
        return Await.success(clickAsync(point, rightClick));
    }
    public static boolean click(Rectangle rectangle) { return Await.success(clickAsync(rectangle)); }
    public static boolean click(Rectangle rectangle, boolean rightClick)
    {
        return Await.success(clickAsync(rectangle, rightClick));
    }
    public static boolean click(Entity entity) { return Await.success(clickAsync(entity)); }
    public static boolean click(Entity entity, boolean rightClick)
    {
        return Await.success(clickAsync(entity, rightClick));
    }
    public static boolean click(AbstractMouseDestination<?> destination)
    {
        return Await.success(clickAsync(destination));
    }
    public static boolean click(AbstractMouseDestination<?> destination, MouseButton button)
    {
        return Await.success(clickAsync(destination, button));
    }

    public static CompletableFuture<Boolean> clickAsync()
    {
        return VirtualMouse.click(MouseButton.LEFT_CLICK);
    }

    public static CompletableFuture<Boolean> clickAsync(boolean rightClick)
    {
        return clickAsync(rightClick ? MouseButton.RIGHT_CLICK : MouseButton.LEFT_CLICK);
    }

    public static CompletableFuture<Boolean> clickAsync(MouseButton button)
    {
        return VirtualMouse.click(button);
    }

    public static CompletableFuture<Boolean> clickAsync(ClickMode mode)
    {
        return clickAsync(mode == null ? MouseButton.LEFT_CLICK : mode.getButton());
    }

    public static CompletableFuture<Boolean> clickAsync(Point point)
    {
        return clickAsync(point, false);
    }

    public static CompletableFuture<Boolean> clickAsync(Point point, boolean rightClick)
    {
        return moveAsync(point).thenCompose(moved -> moved
            ? clickAsync(rightClick) : CompletableFuture.completedFuture(false));
    }

    public static CompletableFuture<Boolean> clickAsync(Rectangle rectangle)
    {
        return clickAsync(rectangle, false);
    }

    public static CompletableFuture<Boolean> clickAsync(Rectangle rectangle, boolean rightClick)
    {
        return moveAsync(rectangle).thenCompose(moved -> moved
            ? clickAsync(rightClick) : CompletableFuture.completedFuture(false));
    }

    public static CompletableFuture<Boolean> clickAsync(Entity entity)
    {
        return entity == null ? CompletableFuture.completedFuture(false) : entity.interactAsync();
    }

    public static CompletableFuture<Boolean> clickAsync(Entity entity, boolean rightClick)
    {
        if (!rightClick) return clickAsync(entity);
        return moveAsync(entity).thenCompose(moved -> moved
            ? clickAsync(MouseButton.RIGHT_CLICK) : CompletableFuture.completedFuture(false));
    }

    public static CompletableFuture<Boolean> clickAsync(AbstractMouseDestination<?> destination)
    {
        return clickAsync(destination, MouseButton.LEFT_CLICK);
    }

    public static CompletableFuture<Boolean> clickAsync(
        AbstractMouseDestination<?> destination, MouseButton button)
    {
        return moveAsync(destination).thenCompose(moved -> moved
            ? clickAsync(button) : CompletableFuture.completedFuture(false));
    }

    public static void mouseDownUntil(int buttonId, Condition condition)
    {
        MouseButton button = MouseButton.getForId(buttonId);
        if (!Await.success(VirtualMouse.press(button))) return;
        try
        {
            Sleep.sleepUntil(condition, 30_000L, 20L);
        }
        finally
        {
            Await.success(VirtualMouse.release(button));
        }
    }

    public static Point getPosition() { return VirtualMouse.getPosition(); }
    public static boolean isMoving() { return VirtualMouse.isMoving(); }
    public static void setSpeed(double pixelsPerSecond) { VirtualMouse.setSpeed(pixelsPerSecond); }
    public static double getSpeed() { return VirtualMouse.getSpeed(); }
    public static void setPosition(int x, int y) { VirtualMouse.hopTo(x, y); }
    public static int getX() { return getPosition().x; }
    public static int getY() { return getPosition().y; }
    public static int getIdleTime()
    {
        return (int) Math.max(0L, System.currentTimeMillis() - getLastMouseMoveTime());
    }
    public static Point getLastClicked() { return VirtualMouse.getLastClicked(); }
    public static long getLastClickedTime() { return VirtualMouse.getLastClickedTime(); }
    public static int getLastClickedX() { return getLastClicked().x; }
    public static int getLastClickedY() { return getLastClicked().y; }
    public static MouseSettings getMouseSettings() { return new MouseSettings(); }
    public static CrosshairState getCrosshairState() { return VirtualMouse.getCrosshairState(); }
    public static int getCrosshairColorId() { return getCrosshairState().getId(); }
    public static int getCrosshairColorID() { return getCrosshairColorId(); }
    public static int getLastCrosshairColorId() { return lastCrosshairColorId; }
    public static int getLastCrosshairColorID() { return getLastCrosshairColorId(); }
    public static void setLastCrosshairColorID(int value) { lastCrosshairColorId = value; }

    public static MouseAlgorithm getMouseAlgorithm() { return VirtualMouse.getMouseAlgorithm(); }
    public static MouseAlgorithm getDefaultMouseAlgorithm()
    {
        return VirtualMouse.getDefaultMouseAlgorithm();
    }
    public static MouseMovementAlgorithm getMouseMovementAlgorithm()
    {
        return VirtualMouse.getMouseMovementAlgorithm();
    }
    public static void setMouseAlgorithm(MouseAlgorithm algorithm)
    {
        VirtualMouse.setMouseAlgorithm(algorithm);
    }
    public static void setMouseMovementAlgorithm(MouseMovementAlgorithm algorithm)
    {
        VirtualMouse.setMouseMovementAlgorithm(algorithm);
    }

    public static List<Entity> getEntitiesOnCursor()
    {
        Point point = getPosition();
        List<Entity> result = new ArrayList<>();
        for (Entity entity : GameObjects.all()) addIfContains(result, entity, point);
        for (Entity entity : NPCs.all()) addIfContains(result, entity, point);
        for (Entity entity : Players.all()) addIfContains(result, entity, point);
        return result;
    }

    public static Tile getTileOnCursor()
    {
        List<Entity> entities = getEntitiesOnCursor();
        return entities.isEmpty() ? null : entities.get(0).getTile();
    }

    public static void scrollUntil(boolean down, int rotations, Condition until)
    {
        scroll(down, rotations, until);
    }

    public static boolean scroll(boolean down, int rotations, Condition until)
    {
        return Await.success(scrollAsync(down, rotations, until));
    }

    public static CompletableFuture<Boolean> scrollAsync(
        boolean down, int rotations, Condition until)
    {
        int count = Math.max(1, rotations);
        CompletableFuture<Boolean> result = CompletableFuture.completedFuture(true);
        for (int index = 0; index < count && (until == null || !until.verify()); index++)
        {
            result = result.thenCompose(previous -> previous
                ? VirtualMouse.scroll(down ? 1 : -1)
                : CompletableFuture.completedFuture(false));
        }
        return result;
    }

    public static void scrollDownUntil(int rotations, Condition until)
    {
        scroll(true, rotations, until);
    }
    public static boolean scrollDown(int rotations, Condition until)
    {
        return scroll(true, rotations, until);
    }
    public static void scrollUpUntil(int rotations, Condition until)
    {
        scroll(false, rotations, until);
    }
    public static boolean scrollUp(int rotations, Condition until)
    {
        return scroll(false, rotations, until);
    }

    public static boolean drag(Point point) { return Await.success(dragAsync(point)); }
    public static boolean drag(Rectangle rectangle) { return Await.success(dragAsync(rectangle)); }
    public static boolean drag(Entity entity) { return Await.success(dragAsync(entity)); }
    public static boolean drag(Tile tile)
    {
        return tile != null && Await.success(
            VirtualMouse.dragTo(new MiniMapTileDestination(tile)));
    }
    public static boolean drag(AbstractMouseDestination<?> destination)
    {
        return Await.success(dragAsync(destination));
    }
    public static CompletableFuture<Boolean> dragAsync(Point point)
    {
        return point == null ? CompletableFuture.completedFuture(false)
            : VirtualMouse.dragTo(new PointDestination(point));
    }
    public static CompletableFuture<Boolean> dragAsync(Rectangle rectangle)
    {
        return rectangle == null ? CompletableFuture.completedFuture(false)
            : VirtualMouse.dragTo(new RectangleDestination(rectangle));
    }
    public static CompletableFuture<Boolean> dragAsync(Entity entity)
    {
        return entity == null ? CompletableFuture.completedFuture(false)
            : VirtualMouse.dragTo(new EntityDestination(entity));
    }
    public static CompletableFuture<Boolean> dragAsync(AbstractMouseDestination<?> destination)
    {
        return destination == null ? CompletableFuture.completedFuture(false)
            : VirtualMouse.dragTo(destination);
    }

    public static boolean moveOutsideScreen() { return Await.success(moveOutsideScreenAsync()); }
    public static CompletableFuture<Boolean> moveOutsideScreenAsync()
    {
        return VirtualMouse.moveOutsideScreen();
    }
    public static boolean moveMouseOutsideScreen() { return moveOutsideScreen(); }
    public static boolean moveOutsideScreen(boolean loseFocus)
    {
        boolean moved = moveOutsideScreen();
        return moved && (!loseFocus || loseFocus(0));
    }
    public static boolean loseFocus(int delay)
    {
        if (delay > 0) Sleep.sleep(delay);
        return moveOutsideScreen();
    }
    public static boolean isMouseInScreen() { return VirtualMouse.isMouseInScreen(); }
    public static boolean hop(Point point) { return Await.success(hopAsync(point)); }
    public static boolean hop(int x, int y) { return Await.success(VirtualMouse.hopTo(x, y)); }
    public static CompletableFuture<Boolean> hopAsync(Point point)
    {
        return point == null ? CompletableFuture.completedFuture(false)
            : VirtualMouse.hopTo(point.x, point.y);
    }
    public static Point getPointOutsideScreen() { return new Point(-1, -1); }
    public static double getLastMouseMagnitude() { return VirtualMouse.getLastMouseMagnitude(); }
    public static double getLastMouseDirection() { return VirtualMouse.getLastMouseDirection(); }
    public static long getLastMouseMoveTime() { return VirtualMouse.getLastMouseMoveTime(); }
    public static boolean isAlwaysHop() { return alwaysHop; }
    public static void setAlwaysHop(boolean value) { alwaysHop = value; }
    public static boolean isForceDrag() { return forceDrag; }
    public static void setForceDrag(boolean value) { forceDrag = value; }
    public static boolean isMouseHeldDown() { return VirtualMouse.isMouseHeldDown(); }
    public static boolean isMouseDragging() { return VirtualMouse.isMouseDragging(); }
    public static MouseTracker getTracker() { return VirtualMouse.getMouseTracker(); }
    public static void reset() { VirtualMouse.cancelAll(); }

    private static void addIfContains(List<Entity> result, Entity entity, Point point)
    {
        try
        {
            java.awt.Shape shape = entity.clickShape(null);
            if (shape != null && shape.contains(point.x, point.y)) result.add(entity);
        }
        catch (RuntimeException ignored)
        {
            // A live entity may disappear while its snapshot is inspected.
        }
    }
}
