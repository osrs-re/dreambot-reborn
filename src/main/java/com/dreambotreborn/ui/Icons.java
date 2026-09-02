package com.dreambotreborn.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import javax.swing.Icon;

/** Small code-drawn icons, avoiding image files and platform font glyphs. */
public final class Icons
{
    private static final Color FOREGROUND = new Color(210, 214, 220);

    private Icons()
    {
    }

    public static Icon play()
    {
        return new PaintedIcon(IconType.PLAY, 18);
    }

    public static Icon pause()
    {
        return new PaintedIcon(IconType.PAUSE, 18);
    }

    public static Icon objects()
    {
        return new PaintedIcon(IconType.OBJECTS, 19);
    }

    public static Icon close()
    {
        return new PaintedIcon(IconType.CLOSE, 14);
    }

    public static Icon mouse()
    {
        return new PaintedIcon(IconType.MOUSE, 18);
    }

    public static Icon mouseOff()
    {
        return new PaintedIcon(IconType.MOUSE_OFF, 18);
    }

    public static Icon developer()
    {
        return new PaintedIcon(IconType.DEVELOPER, 19);
    }

    public static Icon accounts()
    {
        return new PaintedIcon(IconType.ACCOUNTS, 19);
    }

    public static Icon settings()
    {
        return new PaintedIcon(IconType.SETTINGS, 19);
    }

    public static Icon scripts()
    {
        return new PaintedIcon(IconType.SCRIPTS, 19);
    }

    public static Icon stop() { return new PaintedIcon(IconType.STOP, 18); }
    public static Icon reload() { return new PaintedIcon(IconType.RELOAD, 18); }
    public static Icon console() { return new PaintedIcon(IconType.CONSOLE, 19); }
    public static Icon schedule() { return new PaintedIcon(IconType.SCHEDULE, 19); }
    public static Icon breaks() { return new PaintedIcon(IconType.BREAKS, 19); }
    public static Icon tools() { return new PaintedIcon(IconType.TOOLS, 19); }
    public static Icon status() { return new PaintedIcon(IconType.STATUS, 13); }
    public static Icon client() { return new PaintedIcon(IconType.CLIENT, 16); }

    private enum IconType
    {
        PLAY,
        PAUSE,
        OBJECTS,
        CLOSE,
        MOUSE,
        MOUSE_OFF,
        DEVELOPER,
        ACCOUNTS,
        SETTINGS,
        SCRIPTS,
        STOP,
        RELOAD,
        CONSOLE,
        SCHEDULE,
        BREAKS,
        TOOLS,
        STATUS,
        CLIENT
    }

    private static final class PaintedIcon implements Icon
    {
        private final IconType type;
        private final int size;

        private PaintedIcon(IconType type, int size)
        {
            this.type = type;
            this.size = size;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y)
        {
            Graphics2D g = (Graphics2D) graphics.create();
            try
            {
                g.translate(x, y);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(component.isEnabled() ? FOREGROUND : FOREGROUND.darker());
                g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                switch (type)
                {
                    case PLAY:
                        g.fill(new Polygon(
                            new int[] {4, 4, size - 3},
                            new int[] {2, size - 2, size / 2},
                            3));
                        break;
                    case PAUSE:
                        g.fillRoundRect(3, 2, 4, size - 4, 2, 2);
                        g.fillRoundRect(size - 7, 2, 4, size - 4, 2, 2);
                        break;
                    case OBJECTS:
                        g.drawRoundRect(2, 2, 7, 7, 2, 2);
                        g.drawRoundRect(size - 9, 2, 7, 7, 2, 2);
                        g.drawRoundRect(2, size - 9, 7, 7, 2, 2);
                        g.drawRoundRect(size - 9, size - 9, 7, 7, 2, 2);
                        break;
                    case CLOSE:
                        g.drawLine(3, 3, size - 3, size - 3);
                        g.drawLine(size - 3, 3, 3, size - 3);
                        break;
                    case MOUSE:
                    case MOUSE_OFF:
                        g.drawRoundRect(4, 1, size - 8, size - 2, size - 8, size - 8);
                        g.drawLine(size / 2, 2, size / 2, 7);
                        g.drawLine(4, 8, size - 4, 8);
                        if (type == IconType.MOUSE_OFF)
                        {
                            g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                            g.drawLine(2, 2, size - 2, size - 2);
                        }
                        break;
                    case DEVELOPER:
                        g.drawLine(2, size / 2, 7, 4);
                        g.drawLine(2, size / 2, 7, size - 4);
                        g.drawLine(size - 2, size / 2, size - 7, 4);
                        g.drawLine(size - 2, size / 2, size - 7, size - 4);
                        g.drawLine(size / 2 + 2, 2, size / 2 - 2, size - 2);
                        break;
                    case ACCOUNTS:
                        g.drawOval(3, 2, 7, 7);
                        g.drawArc(1, 9, 11, 9, 0, 180);
                        g.drawOval(size - 8, size - 8, 5, 5);
                        g.drawLine(size - 6, size - 8, size - 2, 2);
                        g.drawLine(size - 3, 3, size - 1, 5);
                        break;
                    case SETTINGS:
                        g.drawOval(6, 6, size - 12, size - 12);
                        for (int index = 0; index < 8; index++)
                        {
                            double angle = Math.PI * index / 4.0;
                            int innerX = size / 2 + (int) Math.round(Math.cos(angle) * 6);
                            int innerY = size / 2 + (int) Math.round(Math.sin(angle) * 6);
                            int outerX = size / 2 + (int) Math.round(Math.cos(angle) * 8);
                            int outerY = size / 2 + (int) Math.round(Math.sin(angle) * 8);
                            g.drawLine(innerX, innerY, outerX, outerY);
                        }
                        break;
                    case SCRIPTS:
                        g.drawRoundRect(2, 1, size - 5, size - 3, 2, 2);
                        g.drawLine(5, 5, size - 7, 5);
                        g.drawLine(5, 9, size - 10, 9);
                        g.fill(new Polygon(
                            new int[] {size - 9, size - 9, size - 2},
                            new int[] {size - 10, size - 2, size - 6},
                            3));
                        break;
                    case STOP:
                        g.fillRoundRect(3, 3, size - 6, size - 6, 2, 2);
                        break;
                    case RELOAD:
                        g.drawArc(3, 3, size - 7, size - 7, 35, 285);
                        g.fill(new Polygon(
                            new int[] {size - 5, size - 1, size - 6},
                            new int[] {2, 7, 8}, 3));
                        break;
                    case CONSOLE:
                        g.drawRoundRect(1, 2, size - 3, size - 5, 2, 2);
                        g.drawLine(4, 6, 7, 9);
                        g.drawLine(7, 9, 4, 12);
                        g.drawLine(9, 12, size - 5, 12);
                        break;
                    case SCHEDULE:
                        g.drawRoundRect(2, 4, size - 5, size - 6, 2, 2);
                        g.drawLine(2, 8, size - 3, 8);
                        g.drawLine(6, 1, 6, 6);
                        g.drawLine(size - 7, 1, size - 7, 6);
                        g.drawLine(6, 11, 9, 11);
                        g.drawLine(11, 11, size - 5, 11);
                        break;
                    case BREAKS:
                        g.drawOval(2, 2, size - 5, size - 5);
                        g.drawLine(size / 2, 5, size / 2, size / 2);
                        g.drawLine(size / 2, size / 2, size - 5, size / 2 + 3);
                        break;
                    case TOOLS:
                        g.drawArc(2, 1, 8, 8, 200, 235);
                        g.drawLine(8, 8, size - 3, size - 3);
                        g.drawOval(size - 5, size - 5, 3, 3);
                        break;
                    case STATUS:
                        g.drawRoundRect(1, 2, size - 3, size - 5, 1, 1);
                        g.drawLine(3, 5, 5, 7);
                        g.drawLine(5, 7, 3, 9);
                        g.drawLine(7, 9, size - 3, 9);
                        break;
                    case CLIENT:
                        g.setColor(new Color(50, 190, 193));
                        g.fillRoundRect(1, 1, size - 2, size - 2, 5, 5);
                        g.setColor(new Color(24, 38, 39));
                        g.fillOval(4, 5, 3, 3);
                        g.fillOval(size - 7, 5, 3, 3);
                        g.setStroke(new BasicStroke(1.5f));
                        g.drawArc(4, 6, size - 8, size - 6, 200, 140);
                        break;
                    default:
                        throw new IllegalStateException("Unknown icon type " + type);
                }
            }
            finally
            {
                g.dispose();
            }
        }

        @Override
        public int getIconWidth()
        {
            return size;
        }

        @Override
        public int getIconHeight()
        {
            return size;
        }
    }
}
