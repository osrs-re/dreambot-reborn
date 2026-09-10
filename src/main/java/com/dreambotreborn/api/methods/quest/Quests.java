package com.dreambotreborn.api.methods.quest;

import java.util.Locale;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.methods.quest.book.Quest;

/** Live quest-state facade. */
public final class Quests
{
    private Quests() { }
    public static boolean isStarted(Quest quest) { return quest != null && quest.isStarted(); }
    public static boolean isFinished(Quest quest) { return quest != null && quest.isFinished(); }
    public static int getQuestPoints()
    {
        return DreamBotRebornApi.requireClient().getVarpValue(net.runelite.api.VarPlayer.QUEST_POINTS);
    }
    public static void reset() { }

    public static Quest.State getState(Quest quest)
    {
        net.runelite.api.Quest runeLite = toRuneLite(quest);
        if (runeLite == null) return Quest.State.INVALID;
        try
        {
            switch (runeLite.getState(DreamBotRebornApi.requireClient()))
            {
                case FINISHED: return Quest.State.FINISHED;
                case IN_PROGRESS: return Quest.State.STARTED;
                case NOT_STARTED: return Quest.State.NOT_STARTED;
                default: return Quest.State.INVALID;
            }
        }
        catch (RuntimeException unavailable)
        {
            return Quest.State.INVALID;
        }
    }

    public static net.runelite.api.Quest toRuneLite(Quest quest)
    {
        if (!(quest instanceof Enum<?>)) return null;
        String name = alias(((Enum<?>) quest).name());
        try { return net.runelite.api.Quest.valueOf(name); }
        catch (IllegalArgumentException ignored)
        {
            String canonical = canonical(name);
            for (net.runelite.api.Quest candidate : net.runelite.api.Quest.values())
                if (canonical(candidate.name()).equals(canonical)) return candidate;
            return null;
        }
    }

    public static String getDisplayName(Quest quest)
    {
        net.runelite.api.Quest runeLite = toRuneLite(quest);
        if (runeLite != null) return runeLite.getName();
        String value = quest instanceof Enum<?> ? ((Enum<?>) quest).name() : String.valueOf(quest);
        value = value.replace('_', ' ').toLowerCase(Locale.ENGLISH);
        StringBuilder result = new StringBuilder(value.length());
        boolean uppercase = true;
        for (char character : value.toCharArray())
        {
            result.append(uppercase ? Character.toUpperCase(character) : character);
            uppercase = character == ' ';
        }
        return result.toString();
    }

    private static String alias(String name)
    {
        switch (name)
        {
            case "CORSAIR_CURSE": return "THE_CORSAIR_CURSE";
            case "DRAGON_SLAYER": return "DRAGON_SLAYER_I";
            case "ROMEO_AND_JULIET": return "ROMEO__JULIET";
            case "VAMPIRE_SLAYER": return "VAMPYRE_SLAYER";
            case "DESERT_TREASURE": return "DESERT_TREASURE_I";
            case "DRAGON_SLAYER_2": return "DRAGON_SLAYER_II";
            case "FAIRYTALE_I": return "FAIRYTALE_I__GROWING_PAINS";
            case "FAIRYTALE_I_GROWING_PAINS": return "FAIRYTALE_I__GROWING_PAINS";
            case "FAIRYTALE_II": return "FAIRYTALE_II__CURE_A_QUEEN";
            case "FAIRYTALE_II_CURE_A_QUEEN": return "FAIRYTALE_II__CURE_A_QUEEN";
            case "GARDEN_OF_TRANQUILITY": return "GARDEN_OF_TRANQUILLITY";
            case "MONKEY_MADNESS": return "MONKEY_MADNESS_I";
            case "TASTE_OF_HOPE": return "A_TASTE_OF_HOPE";
            case "RAG_AND_BONE_MAN": return "RAG_AND_BONE_MAN_I";
            case "RAG_AND_BONE_MAN_2": return "RAG_AND_BONE_MAN_II";
            case "RAT_CATCHERS": return "RATCATCHERS";
            case "DESERT_TREASURE_II": return "DESERT_TREASURE_II__THE_FALLEN_EMPIRE";
            case "DESERT_TREASURE_II_THE_FALLEN_EMPIRE": return "DESERT_TREASURE_II__THE_FALLEN_EMPIRE";
            case "RIBBITING_TALE": return "THE_RIBBITING_TALE_OF_A_LILY_PAD_LABOUR_DISPUTE";
            case "THE_MAGE_ARENA": return "MAGE_ARENA_I";
            case "THE_MAGE_ARENA_2": return "MAGE_ARENA_II";
            case "ENCHANTED_KEY": return "THE_ENCHANTED_KEY";
            default: return name;
        }
    }

    private static String canonical(String name)
    {
        return name.replaceFirst("^THE_", "").replaceAll("_+", "_");
    }
}
