package com.dreambotreborn.api.methods.dialogues;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.methods.widget.Widgets;
import com.dreambotreborn.api.wrappers.widgets.Widget;
import com.dreambotreborn.api.utilities.Await;
import net.runelite.api.widgets.WidgetInfo;

/** Dialogue text, options, and visible click helpers. */
public final class Dialogues
{
    private Dialogues()
    {
    }

    public static boolean inDialogue()
    {
        return getDialogueWidget() != null || areOptionsAvailable();
    }

    public static String getNPCDialogue()
    {
        net.runelite.api.widgets.Widget widget =
            DreamBotRebornApi.requireClient().getWidget(WidgetInfo.DIALOG_NPC_TEXT);
        return widget == null ? null : clean(widget.getText());
    }

    public static String getPlayerDialogue()
    {
        net.runelite.api.widgets.Widget widget =
            DreamBotRebornApi.requireClient().getWidget(WidgetInfo.DIALOG_PLAYER_TEXT);
        return widget == null ? null : clean(widget.getText());
    }

    public static boolean canContinue()
    {
        return continueWidget() != null;
    }

    public static boolean continueDialogue()
    {
        return clickContinue();
    }

    public static boolean clickContinue()
    {
        return Await.success(clickContinueAsync());
    }

    public static CompletableFuture<Boolean> clickContinueAsync()
    {
        Widget widget = continueWidget();
        return widget == null ? CompletableFuture.completedFuture(false) : widget.clickAsync();
    }

    public static boolean areOptionsAvailable()
    {
        return getOptions().length > 0;
    }

    public static String[] getOptions()
    {
        net.runelite.api.widgets.Widget parent =
            DreamBotRebornApi.requireClient().getWidget(WidgetInfo.DIALOG_OPTION_OPTIONS);
        if (parent == null || parent.isHidden())
        {
            return new String[0];
        }
        List<String> options = new ArrayList<>();
        collectOptionText(parent.getDynamicChildren(), options);
        if (options.isEmpty())
        {
            collectOptionText(parent.getChildren(), options);
        }
        return options.toArray(new String[0]);
    }

    public static int getOptionIndex(String option)
    {
        if (option != null)
        {
            String[] options = getOptions();
            for (int i = 0; i < options.length; i++)
            {
                if (options[i].equalsIgnoreCase(option))
                {
                    return i + 1;
                }
            }
        }
        return -1;
    }

    public static int getOptionIndexContaining(String text)
    {
        if (text != null)
        {
            String needle = text.toLowerCase();
            String[] options = getOptions();
            for (int i = 0; i < options.length; i++)
            {
                if (options[i].toLowerCase().contains(needle))
                {
                    return i + 1;
                }
            }
        }
        return -1;
    }

    public static boolean chooseOption(int option)
    {
        return Await.success(chooseOptionAsync(option));
    }

    public static CompletableFuture<Boolean> chooseOptionAsync(int option)
    {
        if (option <= 0)
        {
            return CompletableFuture.completedFuture(false);
        }
        net.runelite.api.widgets.Widget parent =
            DreamBotRebornApi.requireClient().getWidget(WidgetInfo.DIALOG_OPTION_OPTIONS);
        List<net.runelite.api.widgets.Widget> children = optionChildren(parent);
        int index = option - 1;
        if (index < 0 || index >= children.size())
        {
            return CompletableFuture.completedFuture(false);
        }
        return new Widget(children.get(index)).clickAsync();
    }

    public static boolean chooseOption(String option)
    {
        return chooseOption(getOptionIndex(option));
    }

    public static boolean chooseFirstOption(String... options)
    {
        if (options != null)
        {
            for (String option : options)
            {
                int index = getOptionIndex(option);
                if (index > 0)
                {
                    return chooseOption(index);
                }
            }
        }
        return false;
    }

    public static boolean chooseFirstOptionContaining(String... values)
    {
        if (values != null)
        {
            for (String value : values)
            {
                int index = getOptionIndexContaining(value);
                if (index > 0)
                {
                    return chooseOption(index);
                }
            }
        }
        return false;
    }

    public static boolean clickOption(int option) { return chooseOption(option); }
    public static boolean clickOption(String option) { return chooseOption(option); }
    public static boolean typeOption(int option)
    {
        return option > 0 && Await.success(
            com.dreambotreborn.api.input.Keyboard.type(option, false));
    }
    public static boolean typeOption(String option) { return typeOption(getOptionIndex(option)); }
    public static boolean spaceToContinue()
    {
        return canContinue() && Await.success(
            com.dreambotreborn.api.input.Keyboard.typeKey(java.awt.event.KeyEvent.VK_SPACE));
    }
    public static boolean canEnterInput()
    {
        net.runelite.api.widgets.Widget focused =
            DreamBotRebornApi.requireClient().getFocusedInputFieldWidget();
        return focused != null && !focused.isHidden();
    }
    public static boolean canEnterInput(String text)
    {
        return canEnterInput() && text != null && Await.success(
            com.dreambotreborn.api.input.Keyboard.type(text, true));
    }
    public static boolean isProcessing()
    {
        return com.dreambotreborn.api.input.Keyboard.isTyping();
    }

    private static Widget getDialogueWidget()
    {
        for (WidgetInfo info : new WidgetInfo[]
            {WidgetInfo.DIALOG_NPC_TEXT, WidgetInfo.DIALOG_PLAYER_TEXT, WidgetInfo.DIALOG_SPRITE_TEXT})
        {
            net.runelite.api.widgets.Widget widget = DreamBotRebornApi.requireClient().getWidget(info);
            if (widget != null && !widget.isHidden())
            {
                return new Widget(widget);
            }
        }
        return null;
    }

    private static Widget continueWidget()
    {
        return Widgets.visible().filter(widget ->
        {
            String text = widget.text.toLowerCase();
            return text.contains("click here to continue") || text.equals("continue");
        }).first();
    }

    private static void collectOptionText(
        net.runelite.api.widgets.Widget[] children, List<String> output)
    {
        if (children != null)
        {
            for (net.runelite.api.widgets.Widget child : children)
            {
                if (child != null && !child.isHidden())
                {
                    String text = clean(child.getText());
                    if (!text.isEmpty())
                    {
                        output.add(text);
                    }
                }
            }
        }
    }

    private static List<net.runelite.api.widgets.Widget> optionChildren(
        net.runelite.api.widgets.Widget parent)
    {
        List<net.runelite.api.widgets.Widget> result = new ArrayList<>();
        if (parent == null)
        {
            return result;
        }
        net.runelite.api.widgets.Widget[] children = parent.getDynamicChildren();
        if (children == null || children.length == 0)
        {
            children = parent.getChildren();
        }
        if (children != null)
        {
            for (net.runelite.api.widgets.Widget child : children)
            {
                if (child != null && !child.isHidden() && !clean(child.getText()).isEmpty())
                {
                    result.add(child);
                }
            }
        }
        return result;
    }

    private static String clean(String text)
    {
        return text == null ? "" : text.replaceAll("<[^>]*>", "");
    }
}
