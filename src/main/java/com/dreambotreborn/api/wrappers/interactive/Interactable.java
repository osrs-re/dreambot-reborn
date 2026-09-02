package com.dreambotreborn.api.wrappers.interactive;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import com.dreambotreborn.api.input.VirtualMouse;
import com.dreambotreborn.api.internal.MouseTarget;
import com.dreambotreborn.api.methods.interactive.InteractionOptions;
import com.dreambotreborn.api.methods.interactive.InteractionResult;
import com.dreambotreborn.api.methods.interactive.Interactions;
import com.dreambotreborn.api.utilities.Await;
import com.dreambotreborn.api.utilities.impl.Condition;

/** Common surface for game entities and items that expose menu actions. */
public interface Interactable
{
    int getId();

    String getName();

    List<String> getActions();

    boolean hasAction(String action);

    default boolean interact()
    {
        return Await.success(interactAsync());
    }

    default boolean interact(String action)
    {
        return Await.success(interactAsync(action));
    }

    default CompletableFuture<Boolean> interactAsync()
    {
        return this instanceof MouseTarget
            ? VirtualMouse.interact((MouseTarget) this)
            : CompletableFuture.completedFuture(false);
    }

    default CompletableFuture<Boolean> interactAsync(String action)
    {
        return this instanceof MouseTarget
            ? VirtualMouse.interact((MouseTarget) this, action)
            : CompletableFuture.completedFuture(false);
    }

    default boolean interact(String action, Condition postCondition)
    {
        return interact(InteractionOptions.builder(action).until(postCondition).build())
            .isSuccessful();
    }

    default InteractionResult interact(InteractionOptions options)
    {
        return Await.result(interactAsync(options),
            new InteractionResult(InteractionResult.Stage.CANCELLED, 0));
    }

    default CompletableFuture<InteractionResult> interactAsync(InteractionOptions options)
    {
        return Interactions.interactAsync(this, options);
    }
}
