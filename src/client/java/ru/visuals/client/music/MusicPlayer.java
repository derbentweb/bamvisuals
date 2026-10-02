package ru.visuals.client.music;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.openal.AL10;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.libc.LibCStdlib;
import ru.visuals.client.config.ModConfig;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Лёгкий плеер для меню. Берёт .ogg из .minecraft/config/visualsmod/music,
 * декодирует в фоновом потоке (STB Vorbis из LWJGL, уже есть в игре) и играет
 * через OpenAL. Никаких доп. зависимостей, на кадры не влияет: в рендере плеер
 * вообще не участвует, раз в тик только проверяет, закончился ли трек.
 */
public final class MusicPlayer {

    private static final Path DIR = FabricLoader.getInstance().getConfigDir()
            .resolve("visualsmod").resolve("music");
    private static final long MAX_FILE_BYTES = 40L * 1024 * 1024;

    private static final List<Path> TRACKS = new ArrayList<>();
    private static int index = -1;
    private static int source = 0;
    private static int buffer = 0;
    private static float volume = 0.5f;
    private static volatile boolean loading = false;
    private static volatile long loadToken = 0;
    private static boolean paused = false;
    private static boolean playing = false;

    private MusicPlayer() {}

    public static void init() {
        try {
            Files.createDirectories(DIR);
        } catch (IOException ignored) {
        }
        String v = ModConfig.entryFor("menu").extra.get("volume");
        if (v != null) {
            try {
                volume = Math.max(0f, Math.min(1f, Float.parseFloat(v)));
            } catch (NumberFormatException ignored) {
            }
        }
        rescan();
    }

    public static void rescan() {
        TRACKS.clear();
        try (Stream<Path> s = Files.list(DIR)) {
            s.filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".ogg"))
                    .sorted()
                    .forEach(TRACKS::add);
        } catch (IOException ignored) {
        }
    }

    public static List<Path> tracks() {
        return Collections.unmodifiableList(TRACKS);
    }

    public static Path folder() {
        return DIR;
    }

    public static boolean isLoading() {
        return loading;
    }

    public static boolean isPaused() {
        return paused;
    }

    public static boolean isActive() {
        return playing && !paused;
    }

    public static float getVolume() {
        return volume;
    }

    public static String currentName() {
        if (index < 0 || index >= TRACKS.size()) return null;
        String n = TRACKS.get(index).getFileName().toString();
        int dot = n.lastIndexOf('.');
        return dot > 0 ? n.substring(0, dot) : n;
    }

    public static void setVolume(float v) {
        volume = Math.max(0f, Math.min(1f, v));
        ModConfig.entryFor("menu").extra.put("volume", Float.toString(volume));
        ModConfig.save();
        if (source != 0) {
            try {
                AL10.alSourcef(source, AL10.AL_GAIN, volume);
            } catch (Throwable ignored) {
            }
        }
    }

    public static void play(int i) {
        if (TRACKS.isEmpty()) return;
        index = Math.floorMod(i, TRACKS.size());
        paused = false;
        stopSource();
        loading = true;
        final long token = ++loadToken;
        final Path file = TRACKS.get(index);
        Thread t = new Thread(() -> decodeAndStart(file, token), "VisualsMod-Music");
        t.setDaemon(true);
        t.start();
    }

    public static void next() {
        play(index + 1);
    }

    public static void prev() {
        play(index - 1);
    }

    public static void togglePause() {
        if (source == 0) {
            if (!TRACKS.isEmpty() && !loading) play(Math.max(index, 0));
            return;
        }
        try {
            if (paused) {
                AL10.alSourcePlay(source);
                paused = false;
            } else {
                AL10.alSourcePause(source);
                paused = true;
            }
        } catch (Throwable ignored) {
        }
    }

    /** Вызывается раз в клиентский тик: переключает на следующий трек, когда текущий закончился. */
    public static void tick() {
        if (!playing || paused || source == 0) return;
        try {
            if (AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE) == AL10.AL_STOPPED) {
                next();
            }
        } catch (Throwable ignored) {
        }
    }

    public static void shutdown() {
        loadToken++;
        stopSource();
    }

    // ---- внутренности ----

    private static void decodeAndStart(Path file, long token) {
        ShortBuffer pcm = null;
        try {
            if (Files.size(file) > MAX_FILE_BYTES) throw new IOException("file too large");
            byte[] bytes = Files.readAllBytes(file);
            ByteBuffer mem = MemoryUtil.memAlloc(bytes.length);
            int channels;
            int rate;
            try {
                mem.put(bytes).flip();
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    IntBuffer ch = stack.mallocInt(1);
                    IntBuffer sr = stack.mallocInt(1);
                    pcm = STBVorbis.stb_vorbis_decode_memory(mem, ch, sr);
                    channels = ch.get(0);
                    rate = sr.get(0);
                }
            } finally {
                MemoryUtil.memFree(mem);
            }
            if (pcm == null || channels < 1 || channels > 2) throw new IOException("unsupported ogg");

            final ShortBuffer data = pcm;
            final int fch = channels;
            final int frate = rate;
            pcm = null; // дальше им владеет render-поток
            MinecraftClient.getInstance().execute(() -> startPlayback(data, fch, frate, token));
        } catch (Throwable e) {
            if (pcm != null) LibCStdlib.free(pcm);
            if (token == loadToken) {
                loading = false;
                playing = false;
            }
        }
    }

    private static void startPlayback(ShortBuffer pcm, int channels, int rate, long token) {
        try {
            if (token != loadToken) return;
            stopSource();
            int format = channels == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16;
            buffer = AL10.alGenBuffers();
            AL10.alBufferData(buffer, format, pcm, rate);
            source = AL10.alGenSources();
            AL10.alSourcei(source, AL10.AL_BUFFER, buffer);
            AL10.alSourcef(source, AL10.AL_GAIN, volume);
            AL10.alSourcei(source, AL10.AL_SOURCE_RELATIVE, AL10.AL_TRUE);
            AL10.alSourcePlay(source);
            playing = true;
        } catch (Throwable e) {
            playing = false;
        } finally {
            LibCStdlib.free(pcm);
            if (token == loadToken) loading = false;
        }
    }

    private static void stopSource() {
        try {
            if (source != 0) {
                AL10.alSourceStop(source);
                AL10.alDeleteSources(source);
            }
            if (buffer != 0) {
                AL10.alDeleteBuffers(buffer);
            }
        } catch (Throwable ignored) {
        }
        source = 0;
        buffer = 0;
        playing = false;
    }
}
