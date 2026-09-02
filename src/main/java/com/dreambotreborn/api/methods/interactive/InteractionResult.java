package com.dreambotreborn.api.methods.interactive;

/** Detailed outcome; ordinary wrapper APIs continue to expose a boolean future. */
public final class InteractionResult
{
    public enum Stage
    {
        REJECTED,
        MENU_ACCEPTED,
        POST_CONDITION_MET,
        TIMED_OUT,
        CANCELLED
    }

    private final Stage stage;
    private final int attempts;

    public InteractionResult(Stage stage, int attempts)
    {
        this.stage = stage;
        this.attempts = attempts;
    }

    public Stage getStage() { return stage; }
    public int getAttempts() { return attempts; }
    public boolean isSuccessful()
    {
        return stage == Stage.MENU_ACCEPTED || stage == Stage.POST_CONDITION_MET;
    }

    @Override public String toString() { return stage + " after " + attempts + " attempt(s)"; }
}
