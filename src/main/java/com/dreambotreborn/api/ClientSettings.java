package com.dreambotreborn.api;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.dreambotreborn.api.data.ActionMode;
import com.dreambotreborn.api.data.ClientLayout;
import com.dreambotreborn.api.internal.ClientThread;
import com.dreambotreborn.api.methods.tabs.Tab;
import com.dreambotreborn.api.methods.tabs.Tabs;
import com.dreambotreborn.api.methods.widget.Widgets;
import com.dreambotreborn.api.utilities.Await;
import com.dreambotreborn.api.utilities.impl.Condition;
import com.dreambotreborn.api.wrappers.widgets.WidgetChild;
import net.runelite.api.gameval.VarbitID;

/** DreamBot-style access to RuneScape client preferences and option varbits. */
public final class ClientSettings
{
    public enum SettingsTab
    {
        ACTIVITIES, AUDIO, CHAT, CONTROLS, DISPLAY, GAMEPLAY, INTERFACES, WARNINGS, POPOUT
    }

    private static final Map<Integer, Integer> CLIENT_PARAMETERS = new ConcurrentHashMap<>();
    private static final Map<String, Integer> STORED_AUTH = new ConcurrentHashMap<>();
    private static volatile boolean displayFps;
    private static volatile boolean hideUsername;
    private static volatile boolean rememberUsername;
    private static volatile ClientLayout requestedLayout;

    private ClientSettings() { }
    public static boolean isDisplayFps() { return displayFps; }
    public static void setDisplayFps(boolean value) { displayFps = value; }
    public static boolean isLoginMusicEnabled() { return !isLoginMusicDisabled(); }
    public static boolean isLoginMusicDisabled() { return getMusicVolume() == 0; }
    public static void setLoginMusicDisabled(boolean value)
    {
        DreamBotRebornApi.requireClient().setMusicVolume(value ? 0 : 127);
    }
    public static String getSavedUsername()
    {
        net.runelite.api.Preferences preferences = DreamBotRebornApi.requireClient().getPreferences();
        return preferences == null || preferences.getRememberedUsername() == null
            ? "" : preferences.getRememberedUsername();
    }
    public static void setSavedUsername(String value)
    {
        net.runelite.api.Preferences preferences = DreamBotRebornApi.requireClient().getPreferences();
        if (preferences != null) preferences.setRememberedUsername(value == null ? "" : value);
        rememberUsername = value != null && !value.isEmpty();
    }
    public static boolean isHideUsername()
    {
        net.runelite.api.Preferences preferences = DreamBotRebornApi.requireClient().getPreferences();
        return preferences == null ? hideUsername : preferences.getHideUsername();
    }
    public static void setHideUsername(boolean value) { hideUsername = value; }
    public static boolean isRememberUsername() { return rememberUsername || !getSavedUsername().isEmpty(); }
    public static int getResizableValue() { return DreamBotRebornApi.requireClient().isResized() ? 1 : 0; }
    public static boolean isResizableActive() { return DreamBotRebornApi.requireClient().isResized(); }
    public static Map<Integer, Integer> getClientParameters()
    { return Collections.unmodifiableMap(CLIENT_PARAMETERS); }
    public static void addStoredAuth(String username, int auth)
    { if (username != null) STORED_AUTH.put(username, auth); }
    public static boolean roofsEnabled() { return !areRoofsHidden(); }
    public static boolean areRoofsHidden() { return bit(1238); }
    public static boolean toggleRoofs(boolean enabled) { return setBit(1238, !enabled); }
    public static boolean closeSettingsInterface()
    {
        if (!isOpen()) return true;
        com.dreambotreborn.api.methods.input.Keyboard.pressEsc();
        return true;
    }
    public static boolean isOpen() { return Tabs.isOpen(Tab.OPTIONS); }
    public static boolean toggleGameAudio(boolean enabled)
    {
        net.runelite.api.Preferences preferences = DreamBotRebornApi.requireClient().getPreferences();
        if (preferences == null) return false;
        preferences.setSoundEffectVolume(enabled ? 127 : 0);
        preferences.setAreaSoundEffectVolume(enabled ? 127 : 0);
        DreamBotRebornApi.requireClient().setMusicVolume(enabled ? 127 : 0);
        return true;
    }
    public static boolean toggleResizable(boolean enabled)
    {
        return setClientLayout(enabled ? ClientLayout.RESIZABLE_CLASSIC : ClientLayout.FIXED_CLASSIC);
    }
    public static int getClientBrightness() { return varp(166); }
    public static boolean setClientBrightness(int value) { return setVarp(166, Math.max(0, Math.min(4, value))); }
    public static void clearLayoutPreferences() { requestedLayout = null; }
    public static boolean setClientLayout(ClientLayout value)
    {
        if (value == null) return false;
        requestedLayout = value;
        return value == ClientLayout.FIXED_CLASSIC ? !isResizableActive() : isResizableActive();
    }
    public static ClientLayout getClientLayout()
    {
        if (requestedLayout != null) return requestedLayout;
        return isResizableActive() ? ClientLayout.RESIZABLE_CLASSIC : ClientLayout.FIXED_CLASSIC;
    }
    public static boolean isGameAudioOn()
    {
        return getMusicVolume() > 0 || getSoundEffectVolume() > 0 || getAreaVolume() > 0;
    }
    public static boolean toggleAcceptAid(boolean value) { return setBit(VarbitID.OPTION_ACCEPTAID, value); }
    public static boolean isAcceptAidEnabled() { return bit(VarbitID.OPTION_ACCEPTAID); }
    public static boolean toggleDataOrbs(boolean value) { return setBit(VarbitID.ORBS_DISABLED, !value); }
    public static boolean areDataOrbsEnabled() { return !bit(VarbitID.ORBS_DISABLED); }
    public static boolean toggleLevelUpInterface(boolean value)
    { return setBit(net.runelite.api.Varbits.DISABLE_LEVEL_UP_INTERFACE, !value); }
    public static boolean isLevelUpInterfaceEnabled()
    { return !bit(net.runelite.api.Varbits.DISABLE_LEVEL_UP_INTERFACE); }
    public static boolean toggleEscInterfaceClosing(boolean value)
    { return setBit(VarbitID.KEYBINDING_ESC_TO_CLOSE, value); }
    public static boolean isEscInterfaceClosingEnabled() { return bit(VarbitID.KEYBINDING_ESC_TO_CLOSE); }
    public static boolean isShiftClickDroppingEnabled() { return bit(554); }
    public static boolean toggleShiftClickDropping(boolean value) { return setBit(554, value); }
    public static boolean toggleScrollToZoom(boolean value)
    { return setBit(VarbitID.CAMERA_ZOOM_MOUSE_DISABLED, !value); }
    public static boolean isScrollToZoomEnabled() { return !bit(VarbitID.CAMERA_ZOOM_MOUSE_DISABLED); }
    public static boolean canUseMiddleMouseForCamera() { return true; }
    public static boolean toggleChatEffects(boolean value) { return setVarp(171, value ? 0 : 1); }
    public static boolean areChatEffectsEnabled() { return varp(171) == 0; }
    public static boolean toggleTransparentChatbox(boolean value)
    { return setBit(net.runelite.api.Varbits.TRANSPARENT_CHATBOX, value); }
    public static boolean isTransparentSidePanelEnabled() { return bit(VarbitID.SIDE_TRANSPARENCY); }
    public static boolean toggleTransparentSidePanel(boolean value)
    { return setBit(VarbitID.SIDE_TRANSPARENCY, value); }
    public static boolean isTransparentChatboxEnabled()
    { return bit(net.runelite.api.Varbits.TRANSPARENT_CHATBOX); }
    public static boolean toggleClickThroughChatbox(boolean value)
    { return setBit(VarbitID.TRANSPARENT_CHATBOX_BLOCKCLICK, value); }
    public static boolean isClickThroughChatboxEnabled()
    { return bit(VarbitID.TRANSPARENT_CHATBOX_BLOCKCLICK); }
    public static boolean toggleItemPilesOnDeath(boolean value)
    { return setBit(VarbitID.OSB10_POST_ENABLE_DROPS, value); }
    public static boolean areItemPilesOnDeathEnabled() { return bit(VarbitID.OSB10_POST_ENABLE_DROPS); }
    public static boolean toggleLootingBagStoreAll(boolean value)
    { return setBit(VarbitID.LOOTINGBAG_USEALLITEMS, value); }
    public static boolean isLootingBagStoreAllEnabled() { return bit(VarbitID.LOOTINGBAG_USEALLITEMS); }
    public static boolean toggleLootingBagIgnoreSupplies(boolean value)
    { return setBit(10080, value); }
    public static boolean isLootingBagIgnoreSuppliesEnabled() { return bit(10080); }
    public static boolean toggleAutomaticallySmashEmptyVials(boolean value)
    { return setBit(VarbitID.AUTO_SMASH_VIALS, value); }
    public static boolean isAutomaticallySmashEmptyVialsEnabled() { return bit(VarbitID.AUTO_SMASH_VIALS); }
    public static boolean toggleMoveFollowerOptionsLower(boolean value)
    { return setBit(VarbitID.FOLLOWEROPS_DEPRIORITISED, value); }
    public static boolean isMoveFollowerOptionsLowerEnabled() { return bit(VarbitID.FOLLOWEROPS_DEPRIORITISED); }
    public static boolean toggleSellPriceWarning(boolean value) { return setBit(11002, value); }
    public static boolean isSellPriceWarningEnabled() { return bit(11002); }
    public static boolean toggleFeroxExitWarning(boolean value) { return setBit(10292, value); }
    public static boolean isFeroxExitWarningEnabled() { return bit(10292); }
    public static boolean toggleFeroxExitWarningOnHighRiskWorld(boolean value) { return setBit(10293, value); }
    public static boolean isFeroxExitWarningOnHighRiskWorldEnabled() { return bit(10293); }
    public static boolean toggleWildernessLeversWarning(boolean value) { return setBit(10294, value); }
    public static boolean isWildernessLeversWarningEnabled() { return bit(10294); }
    public static boolean toggleBuyPriceWarning(boolean value) { return setBit(11003, value); }
    public static boolean isBuyPriceWarningEnabled() { return bit(11003); }
    public static ActionMode getPlayerAttackOptionsMode()
    { return ActionMode.fromVarbitValue(varp(1107)); }
    public static boolean setPlayerAttackOptionsMode(ActionMode value)
    { return value != null && setVarp(1107, value.getVarbitValue()); }
    public static boolean setNPCAttackOptionsMode(ActionMode value)
    { return value != null && setVarp(1306, value.getVarbitValue()); }
    public static ActionMode getNPCAttackOptionsMode()
    { return ActionMode.fromVarbitValue(varp(1306)); }
    public static boolean areLootNotificationsEnabled() { return bit(VarbitID.OPTION_LOOTNOTIFICATION_ON); }
    public static int getLootDropMinimumValue()
    { return DreamBotRebornApi.requireClient().getVarbitValue(VarbitID.OPTION_LOOTNOTIFICATION_VALUE); }
    public static boolean setLootDropMinimumValue(int value)
    { return setVarbitValue(VarbitID.OPTION_LOOTNOTIFICATION_VALUE, Math.max(0, value)); }
    public static boolean toggleLootNotifications(boolean value)
    { return setBit(VarbitID.OPTION_LOOTNOTIFICATION_ON, value); }
    public static boolean toggleFoodSupplyPilesOnDeath(boolean value) { return setBit(11004, value); }
    public static boolean areFoodSupplyPilesOnDeathEnabled() { return bit(11004); }
    public static boolean isSkullPreventionActive() { return bit(VarbitID.BH_SKULL_SETTING_WARNING); }
    public static boolean toggleSkullPrevention(boolean value)
    { return setBit(VarbitID.BH_SKULL_SETTING_WARNING, value); }
    public static boolean restoreDefaultKeybinds()
    { return toggleSetting(SettingsTab.CONTROLS, "Restore defaults", () -> true); }
    public static boolean toggleSetting(SettingsTab tab, String name, Condition condition)
    {
        if (condition != null && condition.verify()) return true;
        if (!Tabs.open(Tab.OPTIONS) || name == null) return false;
        WidgetChild setting = Widgets.getMatchingWidget(widget -> widget.visible
            && (widget.text.equalsIgnoreCase(name) || widget.name.equalsIgnoreCase(name)
                || widget.actions.stream().anyMatch(action -> action.equalsIgnoreCase(name))));
        if (setting == null) return false;
        boolean clicked = setting.actions.isEmpty() ? setting.click() : setting.interact();
        return clicked && (condition == null || condition.verify());
    }
    public static boolean setDefaultZoom() { return setVarp(1055, 512); }
    public static boolean isZoomingEnabled() { return isScrollToZoomEnabled(); }
    public static boolean isShiftInteractionEnabled() { return isShiftClickDroppingEnabled(); }
    public static boolean isTradeDelayEnabled() { return bit(VarbitID.TRADEOPTION_DISABLED); }
    public static boolean toggleTradeDelay(boolean value) { return setBit(VarbitID.TRADEOPTION_DISABLED, value); }
    public static boolean isAmmoAutoEquipping() { return bit(VarbitID.TAKE_AMMO_TOGGLE); }
    public static boolean toggleAmmoAutoEquipping(boolean value) { return setBit(VarbitID.TAKE_AMMO_TOGGLE, value); }
    public static boolean areRunesAutoPouching() { return bit(VarbitID.TAKE_RUNES_TOGGLE); }
    public static boolean toggleRuneAutoPouching(boolean value) { return setBit(VarbitID.TAKE_RUNES_TOGGLE, value); }
    public static boolean areCollectionLogNotificationsEnabled()
    { return bit(net.runelite.api.Varbits.COLLECTION_LOG_NOTIFICATION); }
    public static boolean areCollectionLogPopupsEnabled()
    { return bit(net.runelite.api.Varbits.COMBAT_ACHIEVEMENTS_POPUP); }
    public static boolean toggleCollectionLogNotifications(boolean value)
    { return setBit(net.runelite.api.Varbits.COLLECTION_LOG_NOTIFICATION, value); }
    public static boolean toggleCollectionLogPopups(boolean value)
    { return setBit(net.runelite.api.Varbits.COMBAT_ACHIEVEMENTS_POPUP, value); }
    public static int getMusicVolume() { return DreamBotRebornApi.requireClient().getMusicVolume(); }
    public static int getSoundEffectVolume()
    {
        net.runelite.api.Preferences preferences = DreamBotRebornApi.requireClient().getPreferences();
        return preferences == null ? 0 : preferences.getSoundEffectVolume();
    }
    public static int getAreaVolume()
    {
        net.runelite.api.Preferences preferences = DreamBotRebornApi.requireClient().getPreferences();
        return preferences == null ? 0 : preferences.getAreaSoundEffectVolume();
    }
    public static double getBrightness() { return Math.max(0.0, Math.min(1.0, (4 - getClientBrightness()) / 4.0)); }
    public static boolean isWorldHopConfirmationEnabled() { return !bit(VarbitID.WORLDSWITCHER_DISABLE_CONFIRMATION); }
    public static boolean toggleWorldHopConfirmation(boolean value)
    { return setBit(VarbitID.WORLDSWITCHER_DISABLE_CONFIRMATION, !value); }
    public static boolean isMakeXDartsEnabled() { return bit(11372); }
    public static boolean toggleMakeXDarts(boolean value) { return setBit(11372, value); }
    public static int getMinimumAlchWarningValue()
    { return DreamBotRebornApi.requireClient().getVarbitValue(VarbitID.ALCHEMY_WARNING_VALUETHRESHOLD); }
    public static boolean setMinimumAlchWarningValue(int value)
    { return setVarbitValue(VarbitID.ALCHEMY_WARNING_VALUETHRESHOLD, Math.max(0, value)); }
    public static int getMinimumDropWarningValue()
    { return DreamBotRebornApi.requireClient().getVarbitValue(VarbitID.OPTION_DROPWARNING_VALUE); }
    public static boolean setMinimumDropWarningValue(int value)
    { return setVarbitValue(VarbitID.OPTION_DROPWARNING_VALUE, Math.max(0, value)); }
    public static int getEnergyThresholdToEnableRunning()
    { return com.dreambotreborn.api.methods.walking.impl.Walking.getRunThreshold(); }
    public static boolean setEnergyThresholdToEnableRunning(int value)
    {
        if (value < 0 || value > 100) return false;
        com.dreambotreborn.api.methods.walking.impl.Walking.setRunThreshold(value);
        return true;
    }

    private static boolean bit(int id)
    {
        return DreamBotRebornApi.requireClient().getVarbitValue(id) != 0;
    }
    private static int varp(int id) { return DreamBotRebornApi.requireClient().getVarpValue(id); }
    private static boolean setBit(int id, boolean value) { return setVarbitValue(id, value ? 1 : 0); }
    private static boolean setVarbitValue(int id, int value)
    {
        return Await.result(ClientThread.invoke(() ->
        {
            DreamBotRebornApi.requireClient().setVarbit(id, value);
            return DreamBotRebornApi.requireClient().getVarbitValue(id) == value;
        }), false);
    }
    private static boolean setVarp(int id, int value)
    {
        return Await.result(ClientThread.invoke(() ->
        {
            int[] varps = DreamBotRebornApi.requireClient().getVarps();
            if (varps == null || id < 0 || id >= varps.length) return false;
            varps[id] = value;
            DreamBotRebornApi.requireClient().queueChangedVarp(id);
            return true;
        }), false);
    }
}
