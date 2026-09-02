package com.dreambotreborn.api.input.keyboard;

/** Global typing profile used by the virtual keyboard. */
public final class KeyboardProfile
{
    private static final double DEFAULT_WORDS_PER_MINUTE = 105.0;
    private static volatile double wordsPerMinute = DEFAULT_WORDS_PER_MINUTE;
    private static volatile boolean makeMistakes;

    private KeyboardProfile()
    {
    }

    public static void resetToDefaults()
    {
        wordsPerMinute = DEFAULT_WORDS_PER_MINUTE;
        makeMistakes = false;
    }

    public static void setWordsPerMinute(double value)
    {
        if (!Double.isFinite(value) || value < 10.0 || value > 300.0)
        {
            throw new IllegalArgumentException("wordsPerMinute must be between 10 and 300");
        }
        wordsPerMinute = value;
    }

    public static double getWordsPerMinute()
    {
        return wordsPerMinute;
    }

    public static boolean isMakeMistakes()
    {
        return makeMistakes;
    }

    public static void setMakeMistakes(boolean value)
    {
        makeMistakes = value;
    }
}
