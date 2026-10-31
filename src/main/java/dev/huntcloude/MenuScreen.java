package dev.huntcloude;

import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MenuScreen extends Screen {
    public MenuScreen() {
        super(Component.literal("HuntCloude"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2 - 110;
        int y = Math.max(8, this.height / 2 - 120);

        addLabel("=== HuntCloude ===", cx, y);
        y += 24;
        addLabel("SoundCloude (.wav w huntcloude/music)", cx, y);
        y += 22;

        List<String> tracks = MusicPlayer.listTracks();
        if (tracks.isEmpty()) {
            addLabel("Brak plikow .wav w folderze", cx, y);
            y += 22;
        }
        for (int i = 0; i < Math.min(5, tracks.size()); i++) {
            String name = tracks.get(i);
            addRenderableWidget(Button.builder(Component.literal("> " + name), b -> MusicPlayer.play(name))
                    .bounds(cx, y, 220, 20).build());
            y += 22;
        }
        addRenderableWidget(Button.builder(Component.literal("Stop"), b -> MusicPlayer.stop())
                .bounds(cx, y, 220, 20).build());
        y += 30;

        addToggle(cx, y, "Chunki", () -> State.chunks, State::flipChunks);
        y += 22;
        addToggle(cx, y, "Hitboxy", () -> State.hitboxes, State::flipHitboxes);
        y += 22;
        addToggle(cx, y, "Noclip", () -> State.noclip, State::flipNoclip);
        y += 30;

        addRenderableWidget(Button.builder(Component.literal("Zamknij"), b -> this.onClose())
                .bounds(cx, y, 220, 20).build());
    }

    private void addLabel(String text, int x, int y) {
        Button b = Button.builder(Component.literal(text), btn -> {}).bounds(x, y, 220, 20).build();
        b.active = false;
        addRenderableWidget(b);
    }

    private void addToggle(int x, int y, String name, BooleanSupplier get, Runnable flip) {
        Button b = Button.builder(label(name, get), btn -> {
            flip.run();
            btn.setMessage(label(name, get));
        }).bounds(x, y, 220, 20).build();
        addRenderableWidget(b);
    }

    private static Component label(String name, BooleanSupplier get) {
        return Component.literal(name + ": " + (get.getAsBoolean() ? "WLACZONE" : "wylaczone"));
    }
}
