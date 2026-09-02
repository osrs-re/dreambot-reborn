package com.dreambotreborn.api.input.keyboard;

import java.util.concurrent.ThreadLocalRandom;

/** Variable-cadence keyboard profile with optional neighbouring-key mistakes. */
public class StandardKeyboardAlgorithm implements KeyboardTypingAlgorithm
{
    private static final String[] ROWS = {"1234567890", "qwertyuiop", "asdfghjkl", "zxcvbnm"};

    @Override
    public int delayBefore(char previous, char current)
    {
        double charactersPerMinute = KeyboardProfile.getWordsPerMinute() * 5.0;
        double mean = 60_000.0 / charactersPerMinute;
        double multiplier = Character.isWhitespace(current) ? 1.35
            : isSameHand(previous, current) ? 1.08 : 0.93;
        double gaussian = ThreadLocalRandom.current().nextGaussian() * mean * 0.22;
        return (int) Math.max(18.0, Math.min(650.0, mean * multiplier + gaussian));
    }

    @Override
    public int keyHoldMillis(char value)
    {
        return ThreadLocalRandom.current().nextInt(24, 72);
    }

    @Override
    public Character mistakeFor(char intended)
    {
        if (!KeyboardProfile.isMakeMistakes()
            || !Character.isLetterOrDigit(intended)
            || ThreadLocalRandom.current().nextDouble() >= 0.018)
        {
            return null;
        }
        char lower = Character.toLowerCase(intended);
        for (String row : ROWS)
        {
            int index = row.indexOf(lower);
            if (index >= 0)
            {
                int neighbour = index == 0 ? 1 : index == row.length() - 1
                    ? index - 1 : index + (ThreadLocalRandom.current().nextBoolean() ? 1 : -1);
                char result = row.charAt(neighbour);
                return Character.isUpperCase(intended) ? Character.toUpperCase(result) : result;
            }
        }
        return null;
    }

    public String getMistake(char value)
    {
        Character mistake = mistakeFor(value);
        return mistake == null ? String.valueOf(value) : mistake.toString();
    }

    public String getRandomCharInString(String value)
    {
        return value == null || value.isEmpty() ? ""
            : String.valueOf(value.charAt(ThreadLocalRandom.current().nextInt(value.length())));
    }

    private static boolean isSameHand(char first, char second)
    {
        String left = "12345qwertasdfgzxcvb";
        boolean firstLeft = left.indexOf(Character.toLowerCase(first)) >= 0;
        boolean secondLeft = left.indexOf(Character.toLowerCase(second)) >= 0;
        return firstLeft == secondLeft;
    }
}
