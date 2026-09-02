package com.dreambotreborn.api.input;

import java.awt.BasicStroke;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.Menu;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import com.dreambotreborn.api.internal.MouseTarget;
import com.dreambotreborn.api.wrappers.interactive.GameObject;
import com.dreambotreborn.api.input.event.impl.mouse.MouseButton;
import com.dreambotreborn.api.input.mouse.algorithm.MouseAlgorithm;
import com.dreambotreborn.api.input.mouse.algorithm.MouseMovementAlgorithm;
import com.dreambotreborn.api.input.mouse.algorithm.MouseProfile;
import com.dreambotreborn.api.input.mouse.algorithm.StandardMouseAlgorithm;
import com.dreambotreborn.api.input.mouse.destination.AbstractMouseDestination;
import com.dreambotreborn.api.input.mouse.destination.impl.PointDestination;
import com.dreambotreborn.api.input.mouse.destination.impl.shape.ShapeDestination;
import com.dreambotreborn.api.methods.input.mouse.CrosshairState;
import com.dreambotreborn.api.methods.input.mouse.MouseSettings;
import com.dreambotreborn.api.methods.input.mouse.MouseTracker;

/** A software mouse that is rendered over the game and can interact with objects. */
public final class VirtualMouse
{
    private static final ConcurrentLinkedQueue<Command> COMMANDS = new ConcurrentLinkedQueue<>();
    private static final ConcurrentLinkedDeque<TrailPoint> TRAIL = new ConcurrentLinkedDeque<>();
    private static final int MAX_TRAIL_POINTS = 96;
    private static final ThreadLocal<Integer> SYNTHETIC_EVENT_DEPTH = ThreadLocal.withInitial(() -> 0);
    private static final MouseMovementAlgorithm DEFAULT_ALGORITHM = new StandardMouseAlgorithm();
    private static final MouseTracker TRACKER = new MouseTracker();

    private static volatile Client client;
    private static volatile Component component;
    private static volatile Command activeCommand;
    private static volatile TargetMenu pendingTargetMenu;
    private static volatile int x;
    private static volatile int y;
    private static volatile MouseAlgorithm mouseAlgorithm = DEFAULT_ALGORITHM;
    private static volatile MouseMovementAlgorithm movementAlgorithm = DEFAULT_ALGORITHM;
    private static volatile boolean visible = true;
    private static volatile boolean physicalInputEnabled = true;
    private static volatile boolean inScreen = true;
    private static volatile boolean dragging;
    private static volatile long trailLifetimeNanos = 600_000_000L;
    private static volatile long pressedUntilNanos;
    private static volatile java.awt.Point lastClicked = new java.awt.Point(-1, -1);
    private static volatile long lastClickedTime;
    private static volatile long lastMouseMoveTime;
    private static volatile double lastMouseMagnitude;
    private static volatile double lastMouseDirection;
    private static volatile CrosshairState crosshairState = CrosshairState.IDLE;
    private static volatile long commandGeneration;
    private static long lastTickNanos;

    private VirtualMouse()
    {
    }

    /** Called once by the launcher before the game client starts. */
    public static synchronized void initialize(Client value, Component gameComponent)
    {
        client = Objects.requireNonNull(value, "client");
        component = Objects.requireNonNull(gameComponent, "gameComponent");
        x = Constants.GAME_FIXED_SIZE.width / 2;
        y = Constants.GAME_FIXED_SIZE.height / 2;
        activeCommand = null;
        pendingTargetMenu = null;
        COMMANDS.clear();
        TRAIL.clear();
        TRACKER.clear();
        MouseButton.reset();
        inScreen = true;
        dragging = false;
        crosshairState = CrosshairState.IDLE;
        movementAlgorithm.reset();
        lastTickNanos = 0L;
        hideNativeCursor(value, gameComponent);
    }

    public static CompletableFuture<Boolean> interact(GameObject object)
    {
        return interact((MouseTarget) object);
    }

    public static CompletableFuture<Boolean> interact(MouseTarget object)
    {
        Objects.requireNonNull(object, "object");
        int actionIndex = object.firstActionIndex();
        if (actionIndex < 0)
        {
            return CompletableFuture.completedFuture(false);
        }
        return enqueueInteraction(object, actionIndex);
    }

    public static CompletableFuture<Boolean> interact(GameObject object, String action)
    {
        return interact((MouseTarget) object, action);
    }

    public static CompletableFuture<Boolean> interact(MouseTarget object, String action)
    {
        Objects.requireNonNull(object, "object");
        Objects.requireNonNull(action, "action");
        int actionIndex = object.actionIndex(action);
        if (actionIndex < 0)
        {
            return CompletableFuture.completedFuture(false);
        }
        return enqueueInteraction(object, actionIndex);
    }

    private static CompletableFuture<Boolean> enqueueInteraction(MouseTarget object, int actionIndex)
    {
        requireInitialized();
        InteractionCommand command = new InteractionCommand(object, actionIndex);
        COMMANDS.add(command);
        return command.future();
    }

    /** Moves the software cursor to a canvas coordinate. */
    public static CompletableFuture<Boolean> moveTo(int targetX, int targetY)
    {
        return moveTo(new PointDestination(new java.awt.Point(targetX, targetY)));
    }

    /** Moves to a live destination whose shape may change while the cursor travels. */
    public static CompletableFuture<Boolean> moveTo(AbstractMouseDestination<?> destination)
    {
        requireInitialized();
        Objects.requireNonNull(destination, "destination");
        MoveCommand command = new MoveCommand(destination);
        COMMANDS.add(command);
        return command.future();
    }

    public static CompletableFuture<Boolean> hopTo(int targetX, int targetY)
    {
        Component currentComponent = inputComponent(requireInitialized());
        int maxX = Math.max(0, currentComponent.getWidth() - 1);
        int maxY = Math.max(0, currentComponent.getHeight() - 1);
        int boundedX = Math.max(0, Math.min(targetX, maxX));
        int boundedY = Math.max(0, Math.min(targetY, maxY));
        setPosition(boundedX, boundedY, true);
        dispatchMove(currentComponent, boundedX, boundedY);
        return CompletableFuture.completedFuture(true);
    }

    public static CompletableFuture<Boolean> moveOutsideScreen()
    {
        Component currentComponent = inputComponent(requireInitialized());
        int edgeX = x < currentComponent.getWidth() / 2 ? 0 : Math.max(0, currentComponent.getWidth() - 1);
        int edgeY = Math.max(0, Math.min(y, currentComponent.getHeight() - 1));
        return moveTo(edgeX, edgeY).thenApply(moved ->
        {
            if (moved)
            {
                EventQueue.invokeLater(() -> dispatchSynthetic(currentComponent, new MouseEvent(
                    currentComponent, MouseEvent.MOUSE_EXITED, System.currentTimeMillis(),
                    0, edgeX, edgeY, 0, false, MouseEvent.NOBUTTON)));
                inScreen = false;
            }
            return moved;
        });
    }

    /** Sends a normal left click through the game component at the cursor position. */
    public static void click()
    {
        click(MouseButton.LEFT_CLICK);
    }

    public static CompletableFuture<Boolean> click(MouseButton button)
    {
        return dispatchClick(inputComponent(requireInitialized()), x, y,
            button == null ? MouseButton.LEFT_CLICK : button);
    }

    public static CompletableFuture<Boolean> press(MouseButton button)
    {
        MouseButton selected = button == null ? MouseButton.LEFT_CLICK : button;
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        Component currentComponent = inputComponent(requireInitialized());
        int mouseX = x;
        int mouseY = y;
        EventQueue.invokeLater(() ->
        {
            MouseButton.markPressed(selected, true);
            dispatchSynthetic(currentComponent, new MouseEvent(currentComponent,
                MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), selected.getMask(),
                mouseX, mouseY, 1, false, selected.getId()));
            result.complete(true);
        });
        return result;
    }

    public static CompletableFuture<Boolean> release(MouseButton button)
    {
        MouseButton selected = button == null ? MouseButton.LEFT_CLICK : button;
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        Component currentComponent = inputComponent(requireInitialized());
        int mouseX = x;
        int mouseY = y;
        EventQueue.invokeLater(() ->
        {
            dispatchSynthetic(currentComponent, new MouseEvent(currentComponent,
                MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0,
                mouseX, mouseY, 1, false, selected.getId()));
            MouseButton.markPressed(selected, false);
            result.complete(true);
        });
        return result;
    }

    public static CompletableFuture<Boolean> dragTo(AbstractMouseDestination<?> destination)
    {
        dragging = true;
        return press(MouseButton.LEFT_CLICK)
            .thenCompose(pressed -> pressed ? moveTo(destination)
                : CompletableFuture.completedFuture(false))
            .thenCompose(moved -> release(MouseButton.LEFT_CLICK).thenApply(released -> moved && released))
            .whenComplete((ignored, error) -> dragging = false);
    }

    public static CompletableFuture<Boolean> scroll(int rotations)
    {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        Component currentComponent = inputComponent(requireInitialized());
        int mouseX = x;
        int mouseY = y;
        EventQueue.invokeLater(() ->
        {
            dispatchSynthetic(currentComponent, new MouseWheelEvent(
                currentComponent, MouseEvent.MOUSE_WHEEL, System.currentTimeMillis(), 0,
                mouseX, mouseY, 0, false, MouseWheelEvent.WHEEL_UNIT_SCROLL,
                Math.max(1, Math.abs(rotations)), rotations));
            result.complete(true);
        });
        return result;
    }

    public static java.awt.Point getPosition()
    {
        return new java.awt.Point(x, y);
    }

    /** Keeps the software cursor synchronized with physical mouse movement. */
    public static void observeRealMouse(MouseEvent event)
    {
        if (event != null)
        {
            setPosition(event.getX(), event.getY(), true);
        }
    }

    /**
     * Filters an event arriving through RuneLite's callback. Synthetic virtual
     * mouse events always pass; physical events can be disabled from the UI.
     */
    public static MouseEvent filterMouseEvent(MouseEvent event, boolean updatePosition)
    {
        if (event == null)
        {
            return null;
        }

        if (!isSyntheticEvent() && !physicalInputEnabled)
        {
            event.consume();
            return event;
        }

        if (updatePosition && !isSyntheticEvent())
        {
            observeRealMouse(event);
        }
        return event;
    }

    public static void setPhysicalInputEnabled(boolean enabled)
    {
        physicalInputEnabled = enabled;
    }

    public static boolean isPhysicalInputEnabled()
    {
        return physicalInputEnabled;
    }

    /** True while an AWT event dispatched by this software mouse is being handled. */
    public static boolean isSyntheticInputEvent()
    {
        return isSyntheticEvent();
    }

    public static boolean isMoving()
    {
        return activeCommand != null || !COMMANDS.isEmpty();
    }

    public static void setVisible(boolean value)
    {
        visible = value;
    }

    public static boolean isVisible()
    {
        return visible;
    }

    public static void setSpeed(double pixelsPerSecond)
    {
        if (!Double.isFinite(pixelsPerSecond) || pixelsPerSecond <= 0.0)
        {
            throw new IllegalArgumentException("pixelsPerSecond must be positive");
        }
        MouseProfile.setSpeed(pixelsPerSecond);
    }

    public static double getSpeed() { return MouseProfile.getSpeed(); }

    public static MouseAlgorithm getMouseAlgorithm() { return mouseAlgorithm; }
    public static MouseAlgorithm getDefaultMouseAlgorithm() { return DEFAULT_ALGORITHM; }
    public static MouseMovementAlgorithm getMouseMovementAlgorithm() { return movementAlgorithm; }

    public static synchronized void setMouseAlgorithm(MouseAlgorithm algorithm)
    {
        mouseAlgorithm = Objects.requireNonNull(algorithm, "algorithm");
        if (algorithm instanceof MouseMovementAlgorithm)
        {
            setMouseMovementAlgorithm((MouseMovementAlgorithm) algorithm);
        }
    }

    public static synchronized void setMouseMovementAlgorithm(MouseMovementAlgorithm algorithm)
    {
        movementAlgorithm.reset();
        movementAlgorithm = Objects.requireNonNull(algorithm, "algorithm");
        mouseAlgorithm = algorithm;
    }

    public static MouseTracker getMouseTracker() { return TRACKER; }
    public static java.awt.Point getLastClicked() { return new java.awt.Point(lastClicked); }
    public static long getLastClickedTime() { return lastClickedTime; }
    public static double getLastMouseMagnitude() { return lastMouseMagnitude; }
    public static double getLastMouseDirection() { return lastMouseDirection; }
    public static long getLastMouseMoveTime() { return lastMouseMoveTime; }
    public static boolean isMouseInScreen() { return inScreen; }
    public static boolean isMouseDragging() { return dragging; }
    public static boolean isMouseHeldDown() { return !MouseButton.getPressed().isEmpty(); }
    public static CrosshairState getCrosshairState() { return crosshairState; }

    /** Cancels movement/click work left behind by a paused or stopped script. */
    public static synchronized void cancelAll()
    {
        commandGeneration++;
        Command queued;
        while ((queued = COMMANDS.poll()) != null) queued.complete(false);
        if (activeCommand != null) activeCommand.complete(false);
        if (pendingTargetMenu != null) pendingTargetMenu.command.complete(false);
        activeCommand = null;
        pendingTargetMenu = null;
        movementAlgorithm.reset();
        dragging = false;
        MouseButton.reset();
    }

    public static void setTrailDuration(long milliseconds)
    {
        if (milliseconds < 0L)
        {
            throw new IllegalArgumentException("milliseconds cannot be negative");
        }
        trailLifetimeNanos = milliseconds * 1_000_000L;
        if (milliseconds == 0L)
        {
            TRAIL.clear();
        }
    }

    public static void clearTrail()
    {
        TRAIL.clear();
    }

    /** Advances queued movement. Called by the launcher on the game thread. */
    public static void tick()
    {
        Component currentComponent = component;
        if (client == null || currentComponent == null)
        {
            return;
        }

        long now = System.nanoTime();
        double elapsedSeconds = lastTickNanos == 0L
            ? 0.02
            : Math.min((now - lastTickNanos) / 1_000_000_000.0, 0.1);
        lastTickNanos = now;

        TargetMenu pendingMenu = pendingTargetMenu;
        if (pendingMenu != null)
        {
            if (now >= pendingMenu.expiresAtNanos)
            {
                pendingMenu.command.complete(false);
                pendingTargetMenu = null;
            }
            else
            {
                return;
            }
        }

        Command command = activeCommand;
        if (command == null)
        {
            command = COMMANDS.poll();
            activeCommand = command;
        }
        if (command == null)
        {
            return;
        }
        if (command.expired(now))
        {
            command.complete(false);
            movementAlgorithm.reset();
            activeCommand = null;
            return;
        }

        java.awt.Point target;
        try
        {
            target = command.target(currentComponent);
        }
        catch (RuntimeException ex)
        {
            command.fail(ex);
            activeCommand = null;
            return;
        }

        if (target == null)
        {
            command.complete(false);
            activeCommand = null;
            return;
        }

        if (!command.movementStarted)
        {
            movementAlgorithm.begin(new java.awt.Point(x, y), target, command.settings());
            command.movementStarted = true;
        }

        if (movementAlgorithm.isComplete(new java.awt.Point(x, y), target)
            && command.arrived(new java.awt.Point(x, y), target))
        {
            setPosition(target.x, target.y, true);
            dispatchMove(inputComponent(currentComponent), target.x, target.y);
            try
            {
                command.arrive();
                if (command.completeOnArrival())
                {
                    command.complete(true);
                }
            }
            catch (RuntimeException ex)
            {
                command.fail(ex);
            }
            movementAlgorithm.reset();
            activeCommand = null;
            return;
        }

        java.awt.Point next = movementAlgorithm.next(new java.awt.Point(x, y), target, elapsedSeconds);
        int nextX = next.x;
        int nextY = next.y;
        if (nextX == x && nextY == y)
        {
            double distance = Math.hypot(target.x - x, target.y - y);
            if (distance > 0.0)
            {
                nextX = x + (int) Math.signum(target.x - x);
                nextY = y + (int) Math.signum(target.y - y);
            }
        }
        setPosition(nextX, nextY, true);
        dispatchMove(inputComponent(currentComponent), nextX, nextY);
    }

    /** Receives RuneLite callback events needed to install the pending click action. */
    public static void onEvent(Object event)
    {
        TargetMenu target = pendingTargetMenu;
        if (target == null)
        {
            return;
        }

        if (event instanceof MenuEntryAdded)
        {
            Menu menu = client.getMenu();
            MenuEntry entry = menu.createMenuEntry(-1)
                .setOption(target.option)
                .setTarget(target.target)
                .setIdentifier(target.identifier)
                .setType(target.action)
                .setParam0(target.param0)
                .setParam1(target.param1)
                .setItemId(-1)
                .setWorldViewId(target.worldViewId)
                .setForceLeftClick(false);
            menu.setMenuEntries(new MenuEntry[] {entry});
        }
        else if (event instanceof MenuOptionClicked)
        {
            MenuEntry clicked = ((MenuOptionClicked) event).getMenuEntry();
            boolean matches = target.matches(clicked);
            target.command.complete(matches);
            if (matches)
            {
                crosshairState = CrosshairState.INTERACTED;
            }
            pendingTargetMenu = null;
        }
    }

    /** Draws the software cursor after the game buffer has been painted. */
    public static void draw(Graphics graphics)
    {
        if (!visible || graphics == null)
        {
            return;
        }

        Graphics2D g = (Graphics2D) graphics.create();
        try
        {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            long now = System.nanoTime();
            drawTrail(g, now);
            drawCrosshair(g, x, y, now < pressedUntilNanos);
        }
        finally
        {
            g.dispose();
        }
    }

    private static void drawTrail(Graphics2D g, long now)
    {
        long lifetime = trailLifetimeNanos;
        if (lifetime <= 0L)
        {
            return;
        }

        TrailPoint oldest;
        while ((oldest = TRAIL.peekFirst()) != null && now - oldest.createdAtNanos >= lifetime)
        {
            TRAIL.pollFirst();
        }

        for (TrailPoint point : TRAIL)
        {
            double remaining = 1.0 - (double) (now - point.createdAtNanos) / lifetime;
            if (remaining <= 0.0)
            {
                continue;
            }
            int alpha = (int) Math.round(150.0 * remaining);
            int radius = remaining > 0.55 ? 3 : 2;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha / 255f));
            g.setColor(new Color(93, 210, 255));
            g.fillOval(point.x - radius, point.y - radius, radius * 2, radius * 2);
        }
        g.setComposite(AlphaComposite.SrcOver);
    }

    private static void drawCrosshair(Graphics2D g, int centerX, int centerY, boolean pressed)
    {
        Color foreground = pressed ? new Color(255, 190, 60) : new Color(225, 240, 246);

        g.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(0, 0, 0, 190));
        drawCrosshairLines(g, centerX, centerY);
        g.drawOval(centerX - 4, centerY - 4, 8, 8);

        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(foreground);
        drawCrosshairLines(g, centerX, centerY);
        g.drawOval(centerX - 4, centerY - 4, 8, 8);
        g.fillOval(centerX - 1, centerY - 1, 3, 3);
    }

    private static void drawCrosshairLines(Graphics2D g, int centerX, int centerY)
    {
        g.drawLine(centerX - 11, centerY, centerX - 6, centerY);
        g.drawLine(centerX + 6, centerY, centerX + 11, centerY);
        g.drawLine(centerX, centerY - 11, centerX, centerY - 6);
        g.drawLine(centerX, centerY + 6, centerX, centerY + 11);
    }

    private static void setPosition(int newX, int newY, boolean addTrail)
    {
        Component currentComponent = component;
        if (currentComponent != null)
        {
            int maxX = Math.max(0, currentComponent.getWidth() - 1);
            int maxY = Math.max(0, currentComponent.getHeight() - 1);
            newX = Math.max(0, Math.min(newX, maxX));
            newY = Math.max(0, Math.min(newY, maxY));
        }

        int oldX = x;
        int oldY = y;
        if (oldX == newX && oldY == newY)
        {
            return;
        }

        x = newX;
        y = newY;
        long now = System.currentTimeMillis();
        lastMouseMagnitude = Math.hypot(newX - oldX, newY - oldY);
        lastMouseDirection = Math.atan2(newY - oldY, newX - oldX);
        lastMouseMoveTime = now;
        inScreen = true;
        TRACKER.record(newX, newY);
        if (addTrail && trailLifetimeNanos > 0L)
        {
            TRAIL.addLast(new TrailPoint(newX, newY, System.nanoTime()));
            while (TRAIL.size() > MAX_TRAIL_POINTS)
            {
                TRAIL.pollFirst();
            }
        }
    }

    private static void hideNativeCursor(Client currentClient, Component gameComponent)
    {
        if (GraphicsEnvironment.isHeadless())
        {
            return;
        }

        try
        {
            BufferedImage transparent = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Cursor hidden = Toolkit.getDefaultToolkit().createCustomCursor(
                transparent, new java.awt.Point(0, 0), "DreamBot Reborn hidden cursor");
            gameComponent.setCursor(hidden);
            if (currentClient.getCanvas() != null)
            {
                currentClient.getCanvas().setCursor(hidden);
            }
        }
        catch (RuntimeException ignored)
        {
            // A software cursor still works on platforms that reject custom cursors.
        }
    }

    private static Component requireInitialized()
    {
        Component currentComponent = component;
        if (client == null || currentComponent == null)
        {
            throw new IllegalStateException("VirtualMouse has not been initialized");
        }
        return currentComponent;
    }

    /** The injected client is a Panel, but RuneScape's input listeners live on its Canvas. */
    private static Component inputComponent(Component fallback)
    {
        Client currentClient = client;
        if (currentClient != null && currentClient.getCanvas() != null)
        {
            return currentClient.getCanvas();
        }
        return fallback;
    }

    private static java.awt.Point clickPoint(Shape clickbox, Component currentComponent)
    {
        if (clickbox == null)
        {
            return null;
        }

        Rectangle bounds = clickbox.getBounds();
        int centerX = bounds.x + bounds.width / 2;
        int centerY = bounds.y + bounds.height / 2;

        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int attempt = 0; attempt < 40 && bounds.width > 0 && bounds.height > 0; attempt++)
        {
            int candidateX = random.nextInt(bounds.x, bounds.x + bounds.width + 1);
            int candidateY = random.nextInt(bounds.y, bounds.y + bounds.height + 1);
            if (inside(clickbox, candidateX, candidateY, currentComponent))
            {
                return new java.awt.Point(candidateX, candidateY);
            }
        }

        if (inside(clickbox, centerX, centerY, currentComponent))
        {
            return new java.awt.Point(centerX, centerY);
        }

        int xStep = Math.max(1, bounds.width / 10);
        int yStep = Math.max(1, bounds.height / 10);
        java.awt.Point closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (int candidateY = bounds.y; candidateY <= bounds.y + bounds.height; candidateY += yStep)
        {
            for (int candidateX = bounds.x; candidateX <= bounds.x + bounds.width; candidateX += xStep)
            {
                if (inside(clickbox, candidateX, candidateY, currentComponent))
                {
                    double distance = java.awt.Point.distanceSq(candidateX, candidateY, centerX, centerY);
                    if (distance < closestDistance)
                    {
                        closest = new java.awt.Point(candidateX, candidateY);
                        closestDistance = distance;
                    }
                }
            }
        }
        return closest;
    }

    private static boolean inside(Shape shape, int candidateX, int candidateY, Component currentComponent)
    {
        int width = currentComponent.getWidth();
        int height = currentComponent.getHeight();
        return candidateX >= 0 && candidateY >= 0
            && candidateX < width && candidateY < height
            && shape.contains(candidateX + 0.5, candidateY + 0.5);
    }

    private static void dispatchMove(Component currentComponent, int mouseX, int mouseY)
    {
        long when = System.currentTimeMillis();
        EventQueue.invokeLater(() -> dispatchSynthetic(currentComponent, new MouseEvent(
                currentComponent,
                MouseEvent.MOUSE_MOVED,
                when,
                0,
                mouseX,
                mouseY,
                0,
                false,
                MouseEvent.NOBUTTON)));
    }

    private static void dispatchLeftClick(Component currentComponent, int mouseX, int mouseY)
    {
        dispatchClick(currentComponent, mouseX, mouseY, MouseButton.LEFT_CLICK);
    }

    private static CompletableFuture<Boolean> dispatchClick(
        Component currentComponent, int mouseX, int mouseY, MouseButton button)
    {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        if (button == null || button == MouseButton.NULL)
        {
            result.complete(false);
            return result;
        }
        long generation = commandGeneration;
        EventQueue.invokeLater(() ->
        {
            if (generation != commandGeneration)
            {
                result.complete(false);
                return;
            }
            long when = System.currentTimeMillis();
            dispatchSynthetic(currentComponent, new MouseEvent(currentComponent,
                MouseEvent.MOUSE_MOVED, when, 0, mouseX, mouseY, 0, false, MouseEvent.NOBUTTON));
            MouseButton.markPressed(button, true);
            pressedUntilNanos = System.nanoTime() + 160_000_000L;
            dispatchSynthetic(currentComponent, new MouseEvent(currentComponent,
                MouseEvent.MOUSE_PRESSED, when, button.getMask(), mouseX, mouseY,
                1, false, button.getId()));

            int hold = Math.max(1, MouseProfile.getMouseTiming().getClickTimingInMilliseconds());
            javax.swing.Timer releaseTimer = new javax.swing.Timer(hold, event ->
            {
                ((javax.swing.Timer) event.getSource()).stop();
                if (generation != commandGeneration)
                {
                    MouseButton.markPressed(button, false);
                    result.complete(false);
                    return;
                }
                long releasedAt = System.currentTimeMillis();
                dispatchSynthetic(currentComponent, new MouseEvent(currentComponent,
                    MouseEvent.MOUSE_RELEASED, releasedAt, 0, mouseX, mouseY,
                    1, false, button.getId()));
                dispatchSynthetic(currentComponent, new MouseEvent(currentComponent,
                    MouseEvent.MOUSE_CLICKED, releasedAt, 0, mouseX, mouseY,
                    1, false, button.getId()));
                MouseButton.markPressed(button, false);
                lastClicked = new java.awt.Point(mouseX, mouseY);
                lastClickedTime = releasedAt;
                crosshairState = CrosshairState.CLICKED;
                result.complete(true);
            });
            releaseTimer.setRepeats(false);
            releaseTimer.start();
        });
        return result;
    }

    private static void dispatchSynthetic(Component currentComponent, MouseEvent event)
    {
        beginSyntheticEvent();
        try
        {
            currentComponent.dispatchEvent(event);
        }
        finally
        {
            endSyntheticEvent();
        }
    }

    private static void beginSyntheticEvent()
    {
        SYNTHETIC_EVENT_DEPTH.set(SYNTHETIC_EVENT_DEPTH.get() + 1);
    }

    private static void endSyntheticEvent()
    {
        int depth = SYNTHETIC_EVENT_DEPTH.get() - 1;
        if (depth <= 0)
        {
            SYNTHETIC_EVENT_DEPTH.remove();
        }
        else
        {
            SYNTHETIC_EVENT_DEPTH.set(depth);
        }
    }

    private static boolean isSyntheticEvent()
    {
        return SYNTHETIC_EVENT_DEPTH.get() > 0;
    }

    private abstract static class Command
    {
        private final CompletableFuture<Boolean> result = new CompletableFuture<>();
        private final long expiresAtNanos = System.nanoTime() + 20_000_000_000L;
        private boolean movementStarted;

        abstract java.awt.Point target(Component currentComponent);

        abstract void arrive();

        MouseSettings settings()
        {
            return new MouseSettings();
        }

        boolean arrived(java.awt.Point current, java.awt.Point target)
        {
            return current.distance(target) <= 1.5;
        }

        boolean completeOnArrival()
        {
            return true;
        }

        final CompletableFuture<Boolean> future()
        {
            return result;
        }

        final boolean expired(long now)
        {
            return now >= expiresAtNanos;
        }

        final void complete(boolean value)
        {
            result.complete(value);
        }

        final void fail(RuntimeException ex)
        {
            result.completeExceptionally(ex);
        }
    }

    private static final class MoveCommand extends Command
    {
        private final AbstractMouseDestination<?> destination;
        private java.awt.Point selectedTarget;

        private MoveCommand(AbstractMouseDestination<?> destination)
        {
            this.destination = destination;
        }

        @Override
        java.awt.Point target(Component currentComponent)
        {
            if (!destination.valid()) return null;
            if (selectedTarget == null || !destination.contains(selectedTarget))
            {
                selectedTarget = destination.getSuitablePoint();
            }
            if (selectedTarget == null)
            {
                return null;
            }
            int width = currentComponent.getWidth();
            int height = currentComponent.getHeight();
            if (width <= 0 || height <= 0)
            {
                return null;
            }
            return new java.awt.Point(
                Math.max(0, Math.min(selectedTarget.x, width - 1)),
                Math.max(0, Math.min(selectedTarget.y, height - 1)));
        }

        @Override
        void arrive()
        {
        }

        @Override
        boolean arrived(java.awt.Point current, java.awt.Point target)
        {
            return destination.contains(current) || super.arrived(current, target);
        }
    }

    private static final class InteractionCommand extends Command
    {
        private final MouseTarget object;
        private final int actionIndex;
        private java.awt.Point selectedTarget;

        private InteractionCommand(MouseTarget object, int actionIndex)
        {
            this.object = object;
            this.actionIndex = actionIndex;
        }

        @Override
        java.awt.Point target(Component currentComponent)
        {
            Shape clickbox = object.clickShape(currentComponent);
            if (selectedTarget == null
                || clickbox == null
                || !inside(clickbox, selectedTarget.x, selectedTarget.y, currentComponent))
            {
                ShapeDestination<Shape> destination = new ShapeDestination<>(clickbox);
                destination.setContainmentBounds(new Rectangle(
                    0, 0, currentComponent.getWidth(), currentComponent.getHeight()));
                selectedTarget = destination.getSuitablePoint();
            }
            return selectedTarget;
        }

        @Override
        void arrive()
        {
            MouseTarget.InteractionSpec interaction = object.interactionAt(actionIndex);
            if (interaction == null || interaction.option == null)
            {
                throw new IllegalStateException("The interaction target is no longer available");
            }
            pendingTargetMenu = new TargetMenu(
                this,
                interaction.param0,
                interaction.param1,
                interaction.action,
                interaction.identifier,
                interaction.option,
                interaction.target,
                interaction.worldViewId,
                System.nanoTime() + 2_000_000_000L);

            Component currentComponent = inputComponent(requireInitialized());
            int clickX = x;
            int clickY = y;
            EventQueue.invokeLater(() -> dispatchLeftClick(currentComponent, clickX, clickY));
        }

        @Override
        MouseSettings settings()
        {
            MouseTarget.InteractionSpec interaction = object.interactionAt(actionIndex);
            return new MouseSettings().setTarget(
                interaction == null ? null : interaction.option,
                interaction == null ? null : interaction.target);
        }

        @Override
        boolean completeOnArrival()
        {
            return false;
        }
    }

    private static final class TargetMenu
    {
        private final InteractionCommand command;
        private final int param0;
        private final int param1;
        private final MenuAction action;
        private final int identifier;
        private final String option;
        private final String target;
        private final int worldViewId;
        private final long expiresAtNanos;

        private TargetMenu(
            InteractionCommand command,
            int param0,
            int param1,
            MenuAction action,
            int identifier,
            String option,
            String target,
            int worldViewId,
            long expiresAtNanos)
        {
            this.command = command;
            this.param0 = param0;
            this.param1 = param1;
            this.action = action;
            this.identifier = identifier;
            this.option = option;
            this.target = target;
            this.worldViewId = worldViewId;
            this.expiresAtNanos = expiresAtNanos;
        }

        private boolean matches(MenuEntry entry)
        {
            return entry != null
                && entry.getType() == action
                && entry.getIdentifier() == identifier
                && entry.getParam0() == param0
                && entry.getParam1() == param1;
        }
    }

    private static final class TrailPoint
    {
        private final int x;
        private final int y;
        private final long createdAtNanos;

        private TrailPoint(int x, int y, long createdAtNanos)
        {
            this.x = x;
            this.y = y;
            this.createdAtNanos = createdAtNanos;
        }
    }
}
