package com.dreambotreborn.ui;

/** Global access to the installed client toolbars. */
public final class ClientUi
{
    private static volatile ClientWindow window;

    private ClientUi()
    {
    }

    public static void install(ClientWindow value)
    {
        if (window != null)
        {
            throw new IllegalStateException("Client UI is already installed");
        }
        window = java.util.Objects.requireNonNull(value, "window");
    }

    public static ClientWindow window()
    {
        ClientWindow value = window;
        if (value == null)
        {
            throw new IllegalStateException("Client UI has not been installed");
        }
        return value;
    }

    public static BottomBar bottomBar()
    {
        return window().bottomBar();
    }

    public static Sidebar sidebar()
    {
        return window().sidebar();
    }
}
