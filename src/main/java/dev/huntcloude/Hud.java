package dev.huntcloude;

import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

/** Rysuje napisy z boku ekranu. Rysowanie przez refleksje, bo nazwy metod GUI zmieniaja sie miedzy wersjami. */
public final class Hud {
    private static boolean drawFailed = false;

    private Hud() {}

    public static void render(Object g) {
        if (!MusicPlayer.isPlaying()) return;
        Minecraft mc = Minecraft.getInstance();

        String title = "SoundCloude: " + MusicPlayer.current();
        String cur = "";
        String next = "";
        List<MusicPlayer.Line> ls = MusicPlayer.lines();
        double pos = MusicPlayer.position();
        for (int i = 0; i < ls.size(); i++) {
            if (ls.get(i).time() <= pos) {
                cur = ls.get(i).text();
                next = i + 1 < ls.size() ? ls.get(i + 1).text() : "";
            }
        }

        if (!drawFailed) {
            int w = Math.max(mc.font.width(title), Math.max(mc.font.width(cur), mc.font.width(next)));
            int right = mc.getWindow().getGuiScaledWidth() - 6;
            int left = right - w;
            int y = mc.getWindow().getGuiScaledHeight() / 2 - 20;
            fill(g, left - 4, y - 4, right + 4, y + 40, 0x90000000);
            boolean ok = text(g, title, left, y, 0xFF55FFFF);
            ok &= text(g, cur, left, y + 14, 0xFFFFFFFF);
            ok &= text(g, next, left, y + 26, 0xFFAAAAAA);
            if (!ok) drawFailed = true;
        }
        if (drawFailed && !cur.isEmpty() && mc.player != null) {
            mc.player.displayClientMessage(Component.literal(cur), true);
        }
    }

    private static boolean fill(Object g, int x1, int y1, int x2, int y2, int color) {
        try {
            for (Method m : g.getClass().getMethods()) {
                Class<?>[] p = m.getParameterTypes();
                if (m.getName().equals("fill") && p.length == 5 && p[0] == int.class && p[4] == int.class) {
                    m.invoke(g, x1, y1, x2, y2, color);
                    return true;
                }
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    private static boolean text(Object g, String s, int x, int y, int color) {
        if (s.isEmpty()) return true;
        Minecraft mc = Minecraft.getInstance();
        try {
            for (Method m : g.getClass().getMethods()) {
                String n = m.getName();
                if (!n.equals("text") && !n.equals("drawString")) continue;
                Class<?>[] p = m.getParameterTypes();
                if (p.length < 5 || p.length > 6 || p[0] != Font.class) continue;
                if (p[2] != int.class || p[3] != int.class || p[4] != int.class) continue;
                Object arg1;
                if (p[1] == String.class) arg1 = s;
                else if (p[1] == Component.class) arg1 = Component.literal(s);
                else continue;
                Object[] args = new Object[p.length];
                args[0] = mc.font;
                args[1] = arg1;
                args[2] = x;
                args[3] = y;
                args[4] = color;
                if (p.length == 6) {
                    if (p[5] != boolean.class) continue;
                    args[5] = true;
                }
                m.invoke(g, args);
                return true;
            }
        } catch (Throwable ignored) {
        }
        return false;
    }
}
