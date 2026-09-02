package com.dreambotreborn.devtools;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.GameState;

/** Adapts the directly-painted title/login UI into inspectable pseudo-widgets. */
final class LoginScreenInspector
{
    private static final int FIXED_WIDTH = 765;
    private static final int FIXED_HEIGHT = 503;

    private LoginScreenInspector()
    {
    }

    static WidgetSnapshot capture(Client client)
    {
        GameState gameState = client.getGameState();
        if (gameState != GameState.LOGIN_SCREEN
            && gameState != GameState.LOGIN_SCREEN_AUTHENTICATOR
            && gameState != GameState.LOGGING_IN)
        {
            return null;
        }

        int canvasWidth = Math.max(FIXED_WIDTH, client.getCanvasWidth());
        int canvasHeight = Math.max(FIXED_HEIGHT, client.getCanvasHeight());
        int xPadding = (canvasWidth - FIXED_WIDTH) / 2;
        int centerX = xPadding + 382;
        int loginIndex = client.getLoginIndex();
        int focusedField = client.getCurrentLoginField();

        List<WidgetSnapshot> children = new ArrayList<>();
        children.add(element(
            "background", "graphic", "Background", "Title background",
            new Rectangle(xPadding, 0, FIXED_WIDTH, Math.min(FIXED_HEIGHT, canvasHeight))));
        children.add(element(
            "logo", "graphic", "Logo", "RuneScape logo",
            new Rectangle(centerX - 100, 12, 200, 110)));

        switch (loginIndex)
        {
            case 0:
                addWelcome(children, centerX);
                break;
            case 2:
                addCredentials(children, centerX, focusedField);
                break;
            case 4:
                addAuthenticator(children, centerX);
                break;
            default:
                addGenericDialog(children, centerX, loginIndex);
                break;
        }

        return WidgetSnapshot.loginElement(
            "login/root-" + loginIndex,
            "root",
            "Login interface",
            "Login screen",
            "loginIndex=" + loginIndex + ", gameState=" + gameState,
            new Rectangle(xPadding, 0, FIXED_WIDTH, Math.min(FIXED_HEIGHT, canvasHeight)),
            Collections.emptyList(),
            children);
    }

    private static void addWelcome(List<WidgetSnapshot> children, int centerX)
    {
        List<WidgetSnapshot> panelChildren = Arrays.asList(
            element("welcome/new-user", "button", "Button", "New user",
                new Rectangle(centerX - 153, 271, 146, 40), "Activate"),
            element("welcome/existing-user", "button", "Button", "Existing user",
                new Rectangle(centerX + 7, 271, 146, 40), "Activate"));
        children.add(panel("welcome", "Welcome panel", centerX, panelChildren));
    }

    private static void addCredentials(List<WidgetSnapshot> children, int centerX, int focusedField)
    {
        List<WidgetSnapshot> panelChildren = new ArrayList<>();
        panelChildren.add(element(
            "credentials/title", "text", "Label", "Credential prompt",
            new Rectangle(centerX - 170, 193, 340, 18)));
        panelChildren.add(element(
            "credentials/username", "field", "Text field", "Username / email",
            focusedField == 0 ? "focused" : "not focused",
            new Rectangle(centerX - 110, 239, 220, 16), "Focus"));
        // Deliberately expose only focus state; never read or snapshot the password.
        panelChildren.add(element(
            "credentials/password", "field", "Password field", "Password",
            focusedField == 1 ? "focused; value redacted" : "not focused; value redacted",
            new Rectangle(centerX - 110, 254, 220, 16), "Focus"));
        panelChildren.add(element(
            "credentials/remember", "checkbox", "Checkbox", "Remember username",
            new Rectangle(centerX - 117, 276, 130, 18), "Toggle"));
        panelChildren.add(element(
            "credentials/hide", "checkbox", "Checkbox", "Hide username",
            new Rectangle(centerX + 24, 276, 117, 18), "Toggle"));
        panelChildren.add(element(
            "credentials/login", "button", "Button", "Login",
            new Rectangle(centerX - 153, 301, 146, 40), "Submit"));
        panelChildren.add(element(
            "credentials/cancel", "button", "Button", "Cancel",
            new Rectangle(centerX + 7, 301, 146, 40), "Activate"));
        panelChildren.add(element(
            "credentials/help", "link", "Link", "Can't login?",
            new Rectangle(centerX - 110, 344, 220, 16), "Open"));
        children.add(panel("credentials", "Credential panel", centerX, panelChildren));
    }

    private static void addAuthenticator(List<WidgetSnapshot> children, int centerX)
    {
        List<WidgetSnapshot> panelChildren = Arrays.asList(
            element("auth/title", "text", "Label", "Authenticator",
                new Rectangle(centerX - 170, 193, 340, 18)),
            element("auth/pin", "field", "PIN field", "Authenticator code",
                "value redacted", new Rectangle(centerX - 110, 249, 220, 18), "Focus"),
            element("auth/trust", "checkbox", "Checkbox", "Trust this computer",
                new Rectangle(centerX - 110, 278, 220, 18), "Toggle"),
            element("auth/continue", "button", "Button", "Continue",
                new Rectangle(centerX - 153, 301, 146, 40), "Submit"),
            element("auth/cancel", "button", "Button", "Cancel",
                new Rectangle(centerX + 7, 301, 146, 40), "Activate"));
        children.add(panel("auth", "Authenticator panel", centerX, panelChildren));
    }

    private static void addGenericDialog(List<WidgetSnapshot> children, int centerX, int loginIndex)
    {
        List<WidgetSnapshot> panelChildren = Arrays.asList(
            WidgetSnapshot.loginElement(
                "login/dialog-" + loginIndex + "/content",
                "content",
                "Login state",
                "Dialog content",
                "loginIndex=" + loginIndex,
                new Rectangle(centerX - 165, 190, 330, 100),
                Collections.emptyList(),
                Collections.emptyList()),
            element("dialog/primary", "button", "Button", "Primary action",
                new Rectangle(centerX - 153, 301, 146, 40), "Activate"),
            element("dialog/secondary", "button", "Button", "Secondary action",
                new Rectangle(centerX + 7, 301, 146, 40), "Activate"));
        children.add(panel("dialog-" + loginIndex, "Login dialog " + loginIndex, centerX, panelChildren));
    }

    private static WidgetSnapshot panel(
        String key,
        String name,
        int centerX,
        List<WidgetSnapshot> children)
    {
        return WidgetSnapshot.loginElement(
            "login/" + key,
            "child",
            "Panel",
            name,
            "",
            new Rectangle(centerX - 180, 171, 360, 200),
            Collections.emptyList(),
            children);
    }

    private static WidgetSnapshot element(
        String key,
        String relation,
        String type,
        String name,
        Rectangle bounds,
        String... actions)
    {
        return element(key, relation, type, name, "", bounds, actions);
    }

    private static WidgetSnapshot element(
        String key,
        String relation,
        String type,
        String name,
        String state,
        Rectangle bounds,
        String... actions)
    {
        return WidgetSnapshot.loginElement(
            "login/" + key,
            relation,
            type,
            name,
            state,
            bounds,
            Arrays.asList(actions),
            Collections.emptyList());
    }
}
