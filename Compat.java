package dev.huntcloude;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Pomocnik odporny na zmiany API: ekran, czat i wiadomosc nad paskiem. */
public final class Compat {
    private Compat() {}

    private static List<Object> holders() {
        Minecraft mc = Minecraft.getInstance();
        List<Object> out = new ArrayList<>();
        out.add(mc);
        for (Field f : Minecraft.class.getDeclaredFields()) {
            if (f.getType().getSimpleName().equals("Gui")) {
                try {
                    f.setAccessible(true);
                    Object g = f.get(mc);
                    if (g != null) out.add(g);
                } catch (Throwable ignored) {
                }
            }
        }
        return out;
    }

    private static Field findField(Class<?> c, String name) {
        while (c != null) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                c = c.getSuperclass();
            }
        }
        return null;
    }

    private static boolean matches(Class<?> p, Object a) {
        return p.isInstance(a) || (p == boolean.class && a instanceof Boolean);
    }

    private static boolean call(Object target, String name, Object... args) {
        for (Method m : target.getClass().getMethods()) {
            if (!m.getName().equals(name) || m.getParameterCount() != args.length) continue;
            Class<?>[] p = m.getParameterTypes();
            boolean ok = true;
            for (int i = 0; i < p.length; i++) {
                if (!matches(p[i], args[i])) {
                    ok = false;
                    break;
                }
            }
            if (!ok) continue;
            try {
                m.invoke(target, args);
                return true;
            } catch (Throwable ignored) {
            }
        }
        return false;
    }

    private static Object callGet(Object target, String name) {
        try {
            return target.getClass().getMethod(name).invoke(target);
        } catch (Throwable t) {
            return null;
        }
    }

    public static boolean hasScreen() {
        for (Object h : holders()) {
            try {
                Field f = findField(h.getClass(), "screen");
                if (f != null && Screen.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    return f.get(h) != null;
                }
                for (Method m : h.getClass().getMethods()) {
                    if (m.getParameterCount() == 0 && (m.getName().equals("screen") || m.getName().equals("getScreen"))
                            && Screen.class.isAssignableFrom(m.getReturnType())) {
                        return m.invoke(h) != null;
                    }
                }
            } catch (Throwable ignored) {
            }
        }
        return false;
    }

    public static void setScreen(Screen screen) {
        for (Object h : holders()) {
            if (call(h, "setScreen", screen)) return;
        }
    }

    public static void chat(String text) {
        Component c = Component.literal(text);
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            if (call(mc.player, "sendSystemMessage", c)) return;
            if (call(mc.player, "displayClientMessage", c, false)) return;
        }
        for (Object h : holders()) {
            Object chat = callGet(h, "getChat");
            if (chat != null && call(chat, "addMessage", c)) return;
        }
    }

    public static void overlay(String text) {
        Component c = Component.literal(text);
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            if (call(mc.player, "displayClientMessage", c, true)) return;
            if (call(mc.player, "sendOverlayMessage", c)) return;
        }
        for (Object h : holders()) {
            if (call(h, "setOverlayMessage", c, false)) return;
        }
    }
}
