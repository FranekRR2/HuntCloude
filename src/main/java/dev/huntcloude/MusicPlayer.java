package dev.huntcloude;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import net.fabricmc.loader.api.FabricLoader;

/** SoundCloude: odtwarza pliki .wav z .minecraft/huntcloude/music, napisy z pliku .txt o tej samej nazwie. */
public final class MusicPlayer {
    public record Line(double time, String text) {}

    private static final Pattern LINE = Pattern.compile("^\\[?(\\d+):(\\d{1,2})(?:[.,]\\d+)?\\]?\\s*(.*)$");

    private static Clip clip;
    private static String current = "";
    private static List<Line> lines = List.of();

    private MusicPlayer() {}

    public static Path dir() {
        return FabricLoader.getInstance().getGameDir().resolve("huntcloude").resolve("music");
    }

    public static List<String> listTracks() {
        List<String> out = new ArrayList<>();
        try {
            Files.createDirectories(dir());
            try (var s = Files.list(dir())) {
                s.map(p -> p.getFileName().toString())
                        .filter(n -> n.toLowerCase().endsWith(".wav"))
                        .map(n -> n.substring(0, n.length() - 4))
                        .sorted()
                        .forEach(out::add);
            }
        } catch (Exception ignored) {
        }
        return out;
    }

    public static void play(String name) {
        stop();
        try {
            File f = dir().resolve(name + ".wav").toFile();
            clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(f));
            clip.start();
            current = name;
            loadLines(name);
        } catch (Throwable t) {
            clip = null;
            current = "";
            State.say("Nie moge odtworzyc '" + name + ".wav' (uzyj zwyklego WAV 16-bit PCM): " + t.getMessage());
        }
    }

    public static void stop() {
        try {
            if (clip != null) {
                clip.stop();
                clip.close();
            }
        } catch (Throwable ignored) {
        }
        clip = null;
        current = "";
        lines = List.of();
    }

    public static boolean isPlaying() {
        return clip != null && clip.isRunning();
    }

    public static String current() {
        return current;
    }

    public static double position() {
        return clip == null ? 0 : clip.getMicrosecondPosition() / 1_000_000.0;
    }

    public static List<Line> lines() {
        return lines;
    }

    private static void loadLines(String name) {
        List<Line> out = new ArrayList<>();
        try {
            Path p = dir().resolve(name + ".txt");
            if (Files.exists(p)) {
                for (String l : Files.readAllLines(p, StandardCharsets.UTF_8)) {
                    Matcher m = LINE.matcher(l.trim());
                    if (m.matches()) {
                        double t = Integer.parseInt(m.group(1)) * 60 + Integer.parseInt(m.group(2));
                        out.add(new Line(t, m.group(3)));
                    }
                }
            }
        } catch (Exception ignored) {
        }
        lines = out;
    }
}
