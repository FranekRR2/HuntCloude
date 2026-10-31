package dev.huntcloude;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class State {
    public static boolean chunks, hitboxes, noclip;

    private State() {}

    public static void say(String msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal("[HuntCloude] " + msg), false);
        }
    }

    public static void flipChunks() {
        boolean now = !chunks;
        if (DebugToggles.chunks(now)) chunks = now;
        else say("Nie udalo sie przelaczyc chunkow (zmiana API w tej wersji). Uzyj F3+G.");
    }

    public static void flipHitboxes() {
        boolean now = !hitboxes;
        if (DebugToggles.hitboxes(now)) hitboxes = now;
        else say("Nie udalo sie przelaczyc hitboxow (zmiana API w tej wersji). Uzyj F3+B.");
    }

    public static void flipNoclip() {
        noclip = !noclip;
        if (noclip) say("Noclip wlaczony: WASD + spacja/shift, Ctrl = szybciej. Najlepiej w trybie kreatywnym.");
    }
}
