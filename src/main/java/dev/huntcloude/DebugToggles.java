package dev.huntcloude;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;

/** Przelacza hitboxy i granice chunkow przez wbudowane debugi Minecrafta (refleksja, zeby przetrwac zmiany API). */
public final class DebugToggles {
    private static final Map<String, Boolean> tracked = new HashMap<>();

    private DebugToggles() {}

    public static boolean hitboxes(boolean on) {
        if (viaEntries("ENTITY_HITBOXES", on)) return true;
        try {
            Minecraft mc = Minecraft.getInstance();
            Object d = mc.getClass().getMethod("getEntityRenderDispatcher").invoke(mc);
            d.getClass().getMethod("setRenderHitBoxes", boolean.class).invoke(d, on);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean chunks(boolean on) {
        if (viaEntries("CHUNK_BORDERS", on)) return true;
        try {
            Minecraft mc = Minecraft.getInstance();
            Field f = mc.getClass().getField("debugRenderer");
            Object r = f.get(mc);
            boolean cur = tracked.getOrDefault("legacy_chunks", false);
            if (cur != on) r.getClass().getMethod("switchRenderChunkborder").invoke(r);
            tracked.put("legacy_chunks", on);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean viaEntries(String name, boolean on) {
        try {
            Minecraft mc = Minecraft.getInstance();
            Object list = null;
            for (Field f : Minecraft.class.getDeclaredFields()) {
                if (f.getType().getSimpleName().equals("DebugScreenEntryList")) {
                    f.setAccessible(true);
                    list = f.get(mc);
                    break;
                }
            }
            if (list == null) return false;

            Class<?> entries = Class.forName("net.minecraft.client.gui.components.debug.DebugScreenEntries");
            Field cf = entries.getDeclaredField(name);
            cf.setAccessible(true);
            Object id = cf.get(null);

            Boolean cur = null;
            Method toggle = null;
            for (Method m : list.getClass().getMethods()) {
                if (m.getParameterCount() != 1) continue;
                if (m.getName().equals("isCurrentlyEnabled") && m.getReturnType() == boolean.class) {
                    cur = (Boolean) m.invoke(list, id);
                }
                if (m.getName().equals("toggleStatus")) toggle = m;
            }
            if (toggle == null) return false;
            if (cur == null) cur = tracked.getOrDefault(name, false);
            if (cur.booleanValue() != on) toggle.invoke(list, id);
            tracked.put(name, on);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
