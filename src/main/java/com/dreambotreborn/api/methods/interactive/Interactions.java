package com.dreambotreborn.api.methods.interactive;

import java.awt.Shape;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import com.dreambotreborn.api.input.VirtualMouse;
import com.dreambotreborn.api.internal.ClientThread;
import com.dreambotreborn.api.internal.MouseTarget;
import com.dreambotreborn.api.methods.input.Camera;
import com.dreambotreborn.api.methods.walking.impl.Walking;
import com.dreambotreborn.api.wrappers.interactive.Entity;
import com.dreambotreborn.api.wrappers.interactive.Interactable;
import com.dreambotreborn.api.utilities.Await;

/** Transactional interactions with preparation, retries, and optional postconditions. */
public final class Interactions
{
    private static final ScheduledExecutorService POLLER =
        Executors.newSingleThreadScheduledExecutor(runnable ->
        {
            Thread thread = new Thread(runnable, "DreamBot Reborn Interaction Verifier");
            thread.setDaemon(true);
            return thread;
        });

    private Interactions()
    {
    }

    public static InteractionResult interact(
        Interactable interactable, InteractionOptions options)
    {
        return Await.result(interactAsync(interactable, options),
            new InteractionResult(InteractionResult.Stage.CANCELLED, 0));
    }

    public static CompletableFuture<InteractionResult> interactAsync(
        Interactable interactable, InteractionOptions options)
    {
        Objects.requireNonNull(interactable, "interactable");
        Objects.requireNonNull(options, "options");
        if (!(interactable instanceof MouseTarget))
        {
            return CompletableFuture.completedFuture(new InteractionResult(
                InteractionResult.Stage.REJECTED, 0));
        }
        return attempt((MouseTarget) interactable, interactable, options, 1);
    }

    private static CompletableFuture<InteractionResult> attempt(
        MouseTarget target,
        Interactable interactable,
        InteractionOptions options,
        int attempt)
    {
        if (options.postCondition != null && safeVerify(options))
        {
            return CompletableFuture.completedFuture(new InteractionResult(
                InteractionResult.Stage.POST_CONDITION_MET, attempt - 1));
        }

        return prepare(interactable, options).thenCompose(prepared ->
        {
            if (!Boolean.TRUE.equals(prepared))
            {
                return retryOrFinish(target, interactable, options, attempt,
                    InteractionResult.Stage.REJECTED);
            }
            CompletableFuture<Boolean> click = options.action == null || options.action.trim().isEmpty()
                ? VirtualMouse.interact(target) : VirtualMouse.interact(target, options.action);
            return click.thenCompose(accepted ->
            {
                if (!Boolean.TRUE.equals(accepted))
                {
                    return retryOrFinish(target, interactable, options, attempt,
                        InteractionResult.Stage.REJECTED);
                }
                if (options.postCondition == null)
                {
                    return CompletableFuture.completedFuture(new InteractionResult(
                        InteractionResult.Stage.MENU_ACCEPTED, attempt));
                }
                long deadline = System.nanoTime() + options.timeout.toNanos();
                return waitFor(options, deadline).thenCompose(verified -> verified
                    ? CompletableFuture.completedFuture(new InteractionResult(
                        InteractionResult.Stage.POST_CONDITION_MET, attempt))
                    : retryOrFinish(target, interactable, options, attempt,
                        InteractionResult.Stage.TIMED_OUT));
            });
        });
    }

    private static CompletableFuture<InteractionResult> retryOrFinish(
        MouseTarget target,
        Interactable interactable,
        InteractionOptions options,
        int attempt,
        InteractionResult.Stage failure)
    {
        if (attempt >= options.attempts)
        {
            return CompletableFuture.completedFuture(new InteractionResult(failure, attempt));
        }
        CompletableFuture<InteractionResult> result = new CompletableFuture<>();
        POLLER.schedule(() -> attempt(target, interactable, options, attempt + 1)
            .whenComplete((value, error) ->
            {
                if (error == null) result.complete(value); else result.completeExceptionally(error);
            }), 180L + attempt * 90L, TimeUnit.MILLISECONDS);
        return result;
    }

    private static CompletableFuture<Boolean> waitFor(InteractionOptions options, long deadline)
    {
        if (safeVerify(options)) return CompletableFuture.completedFuture(true);
        if (System.nanoTime() >= deadline) return CompletableFuture.completedFuture(false);
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        POLLER.schedule(() -> waitFor(options, deadline).whenComplete((value, error) ->
        {
            if (error == null) result.complete(value); else result.completeExceptionally(error);
        }), 50, TimeUnit.MILLISECONDS);
        return result;
    }

    private static boolean safeVerify(InteractionOptions options)
    {
        try { return options.postCondition != null && options.postCondition.verify(); }
        catch (RuntimeException ignored) { return false; }
    }

    private static CompletableFuture<Boolean> prepare(
        Interactable interactable, InteractionOptions options)
    {
        if (!(interactable instanceof Entity)) return CompletableFuture.completedFuture(true);
        Entity entity = (Entity) interactable;
        return isClickable(entity).thenCompose(clickable ->
        {
            if (clickable) return CompletableFuture.completedFuture(true);
            if (options.handleCamera) Camera.rotateToEntity(entity);
            CompletableFuture<Boolean> movement = options.handleWalk
                ? Walking.walkAsync(entity) : CompletableFuture.completedFuture(true);
            long preparationNanos = Math.min(options.timeout.toNanos(), 3_000_000_000L);
            long deadline = System.nanoTime() + Math.max(350_000_000L, preparationNanos);
            return movement.handle((ignored, error) -> error == null)
                .thenCompose(ignored -> waitUntilClickable(entity, deadline));
        });
    }

    private static CompletableFuture<Boolean> waitUntilClickable(Entity entity, long deadline)
    {
        return isClickable(entity).thenCompose(clickable ->
        {
            if (clickable) return CompletableFuture.completedFuture(true);
            if (System.nanoTime() >= deadline) return CompletableFuture.completedFuture(false);
            CompletableFuture<Boolean> result = new CompletableFuture<>();
            POLLER.schedule(() -> waitUntilClickable(entity, deadline).whenComplete((value, error) ->
            {
                if (error == null) result.complete(value); else result.completeExceptionally(error);
            }), 75L, TimeUnit.MILLISECONDS);
            return result;
        });
    }

    private static CompletableFuture<Boolean> isClickable(Entity entity)
    {
        return ClientThread.invoke(() ->
        {
            try
            {
                Shape shape = entity.clickShape(null);
                return shape != null && !shape.getBounds().isEmpty();
            }
            catch (RuntimeException ignored)
            {
                return false;
            }
        });
    }
}
