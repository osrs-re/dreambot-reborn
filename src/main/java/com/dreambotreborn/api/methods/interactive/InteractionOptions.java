package com.dreambotreborn.api.methods.interactive;

import java.time.Duration;
import com.dreambotreborn.api.utilities.impl.Condition;

/** Immutable retry, preparation, and verification policy for one interaction. */
public final class InteractionOptions
{
    public final String action;
    public final Condition postCondition;
    public final Duration timeout;
    public final int attempts;
    public final boolean handleCamera;
    public final boolean handleWalk;

    private InteractionOptions(Builder builder)
    {
        action = builder.action;
        postCondition = builder.postCondition;
        timeout = builder.timeout;
        attempts = builder.attempts;
        handleCamera = builder.handleCamera;
        handleWalk = builder.handleWalk;
    }

    public static Builder builder(String action)
    {
        return new Builder(action);
    }

    public static final class Builder
    {
        private final String action;
        private Condition postCondition;
        private Duration timeout = Duration.ofSeconds(3);
        private int attempts = 2;
        private boolean handleCamera = true;
        private boolean handleWalk;

        private Builder(String action)
        {
            this.action = action;
        }

        public Builder until(Condition condition)
        {
            postCondition = condition;
            return this;
        }

        public Builder timeout(Duration value)
        {
            if (value == null || value.isNegative())
                throw new IllegalArgumentException("timeout cannot be negative");
            timeout = value;
            return this;
        }

        public Builder attempts(int value)
        {
            if (value <= 0) throw new IllegalArgumentException("attempts must be positive");
            attempts = value;
            return this;
        }

        public Builder handleCamera(boolean value) { handleCamera = value; return this; }
        public Builder handleWalk(boolean value) { handleWalk = value; return this; }
        public InteractionOptions build() { return new InteractionOptions(this); }
    }
}
