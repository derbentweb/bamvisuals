package ru.visuals.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import ru.visuals.client.config.ModConfig;
import ru.visuals.client.music.MusicPlayer;

import java.util.Map;
import java.util.Random;

/**
 * Своё главное меню вместо ванильного TitleScreen (подмена в MinecraftClientMixin).
 *  - 3 раскладки кнопок: Классика / Панель слева / Минимал
 *  - 8 живых фонов (градиент "дышит" + мерцающие звёзды или снег)
 *  - плеер музыки внизу (.ogg из config/visualsmod/music)
 * Всё рисуется обычными fill/fillGradient, без своих шейдеров и текстур, так что
 * меню почти бесплатное по ресурсам. Выбор раскладки и фона сохраняется в конфиг.
 */
public class MainMenuScreen extends Screen {

    /** true -> следующее открытие TitleScreen пропустит нашу подмену (кнопка "Ванильное"). */
    public static boolean bypassOnce = false;

    private record Bg(String name, int top, int bottom, int bottom2, boolean stars, boolean snow) {}

    private static final Bg[] BGS = {
            new Bg("Закат",          0xFF1B1030, 0xFFD9503A, 0xFFFF9A5A, true,  false),
            new Bg("Ночь",           0xFF04060E, 0xFF14223F, 0xFF22385F, true,  false),
            new Bg("Красные облака", 0xFF120000, 0xFF7A0C12, 0xFFB01820, true,  false),
            new Bg("Неон",           0xFF0A0220, 0xFF5A12B8, 0xFFB000FF, true,  false),
            new Bg("Аметист",        0xFF110A22, 0xFF4B2C8F, 0xFF7A4FD0, true,  false),
            new Bg("Зима",           0xFF7F98B3, 0xFFD5E2EE, 0xFFF1F6FB, false, true),
            new Bg("Океан",          0xFF021520, 0xFF0A5C66, 0xFF139AA3, false, false),
            new Bg("Сакура",         0xFF2E1829, 0xFFD98BAE, 0xFFF4B8CF, false, true),
    };
    private static final String[] LAYOUTS = {"Классика", "Панель", "Минимал"};
    private static final int PANEL_W = 220;

    private final float[] starX = new float[90];
    private final float[] starY = new float[90];
    private final float[] starPhase = new float[90];
    private final SnowRenderer snow = new SnowRenderer(140);

    private int layout;
    private int bg;

    // позиции для оверлея миниатюр фона
    private int[] swX;
    private int swY;
    private static final int SW_W = 34;
    private static final int SW_H = 20;

    private ButtonWidget playButton;

    public MainMenuScreen() {
        super(Text.literal("VisualsMod"));
        Map<String, String> extra = ModConfig.entryFor("menu").extra;
        layout = parse(extra.get("layout"), 0, LAYOUTS.length - 1);
        bg = parse(extra.get("bg"), 0, BGS.length - 1);
        Random r = new Random(7);
        for (int i = 0; i < starX.length; i++) {
            starX[i] = r.nextFloat();
            starY[i] = r.nextFloat();
            starPhase[i] = r.nextFloat();
        }
    }

    private static int parse(String s, int def, int max) {
        if (s == null) return def;
        try {
            return Math.max(0, Math.min(max, Integer.parseInt(s)));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private void saveChoice() {
        Map<String, String> extra = ModConfig.entryFor("menu").extra;
        extra.put("layout", Integer.toString(layout));
        extra.put("bg", Integer.toString(bg));
        ModConfig.save();
    }

    @Override
    protected void init() {
        int areaX = layout == 1 ? PANEL_W : 0;
        int areaW = width - areaX;

        // --- основные кнопки ---
        String[] labels = {"Одиночная игра", "Сетевая игра", "Настройки", "Выход"};
        Runnable[] actions = {
                () -> client.setScreen(new SelectWorldScreen(this)),
                () -> client.setScreen(new MultiplayerScreen(this)),
                () -> client.setScreen(new OptionsScreen(this, client.options)),
                () -> client.scheduleStop(),
        };
        if (layout == 0) {
            int bw = 200, bh = 22, gap = 6;
            int y = height / 2 - 24;
            for (int i = 0; i < labels.length; i++) {
                addMain(labels[i], width / 2 - bw / 2, y + i * (bh + gap), bw, bh, actions[i]);
            }
        } else if (layout == 1) {
            int bw = 180, bh = 22, gap = 6;
            int y = 90;
            for (int i = 0; i < labels.length; i++) {
                addMain(labels[i], (PANEL_W - bw) / 2, y + i * (bh + gap), bw, bh, actions[i]);
            }
        } else {
            int bw = 96, bh = 22, gap = 6;
            int total = labels.length * bw + (labels.length - 1) * gap;
            int x = width / 2 - total / 2;
            int y = height / 2 + 24;
            for (int i = 0; i < labels.length; i++) {
                addMain(labels[i], x + i * (bw + gap), y, bw, bh, actions[i]);
            }
        }

        // --- верхний правый угол: раскладка и ванильное меню ---
        addDrawableChild(ButtonWidget.builder(Text.literal("Меню: " + LAYOUTS[layout]), b -> {
            layout = (layout + 1) % LAYOUTS.length;
            saveChoice();
            clearAndInit();
        }).dimensions(width - 136, 6, 130, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Ванильное меню"), b -> {
            bypassOnce = true;
            client.setScreen(new TitleScreen());
        }).dimensions(width - 136, 30, 130, 20).build());

        // --- выбор фона (прозрачные кнопки, картинку рисуем в render) ---
        int n = BGS.length;
        int gap = 4;
        int total = n * SW_W + (n - 1) * gap;
        int sx = areaX + (areaW - total) / 2;
        swY = height - 78;
        swX = new int[n];
        for (int i = 0; i < n; i++) {
            final int idx = i;
            swX[i] = sx + i * (SW_W + gap);
            addDrawableChild(ButtonWidget.builder(Text.empty(), b -> {
                bg = idx;
                saveChoice();
            }).dimensions(swX[i], swY, SW_W, SW_H).build());
        }

        // --- плеер ---
        int[] widths = {22, 64, 22, 22, 22, 74};
        int mgap = 4;
        int mtotal = mgap * (widths.length - 1);
        for (int w : widths) mtotal += w;
        int mx = areaX + (areaW - mtotal) / 2;
        int my = height - 34;
        int cx = mx;
        addDrawableChild(ButtonWidget.builder(Text.literal("<"), b -> MusicPlayer.prev())
                .dimensions(cx, my, widths[0], 20).build());
        cx += widths[0] + mgap;
        playButton = ButtonWidget.builder(Text.literal("Пуск"), b -> MusicPlayer.togglePause())
                .dimensions(cx, my, widths[1], 20).build();
        addDrawableChild(playButton);
        cx += widths[1] + mgap;
        addDrawableChild(ButtonWidget.builder(Text.literal(">"), b -> MusicPlayer.next())
                .dimensions(cx, my, widths[2], 20).build());
        cx += widths[2] + mgap;
        addDrawableChild(ButtonWidget.builder(Text.literal("-"),
                        b -> MusicPlayer.setVolume(MusicPlayer.getVolume() - 0.1f))
                .dimensions(cx, my, widths[3], 20).build());
        cx += widths[3] + mgap;
        addDrawableChild(ButtonWidget.builder(Text.literal("+"),
                        b -> MusicPlayer.setVolume(MusicPlayer.getVolume() + 0.1f))
                .dimensions(cx, my, widths[4], 20).build());
        cx += widths[4] + mgap;
        addDrawableChild(ButtonWidget.builder(Text.literal("Обновить"), b -> MusicPlayer.rescan())
                .dimensions(cx, my, widths[5], 20).build());
    }

    private void addMain(String label, int x, int y, int w, int h, Runnable action) {
        addDrawableChild(ButtonWidget.builder(Text.literal(label), b -> action.run())
                .dimensions(x, y, w, h).build());
    }

    /** Свой фон рисуем в render(), поэтому ванильный (с блюром) отключаем. */
    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        float t = (System.currentTimeMillis() % 100_000_000L) / 1000f;
        Bg b = BGS[bg];

        // живой градиент: нижний цвет плавно "дышит" между двумя оттенками
        float k = (float) (Math.sin(t * 0.4f) * 0.5 + 0.5);
        ctx.fillGradient(0, 0, width, height, b.top(), lerp(b.bottom(), b.bottom2(), k));
        if (b.stars()) drawStars(ctx, t);
        if (b.snow()) snow.render(ctx, width, height, 140, 0.02f);

        if (layout == 1) {
            ctx.fill(0, 0, PANEL_W, height, 0xAA000000);
            ctx.fill(PANEL_W, 0, PANEL_W + 1, height, 0x55FFFFFF);
        }

        // заголовок и приветствие
        String name = client.getSession().getUsername();
        int tx, ty;
        if (layout == 0) {
            tx = width / 2;
            ty = height / 2 - 70;
        } else if (layout == 1) {
            tx = PANEL_W / 2;
            ty = 36;
        } else {
            tx = width / 2;
            ty = height / 2 - 24;
        }
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("V I S U A L S"), tx, ty, 0xFFFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Хорошего времени суток, " + name), tx, ty + 14, 0xFFBFC8D8);

        // обновляем подпись кнопки плеера
        if (playButton != null) {
            playButton.setMessage(Text.literal(MusicPlayer.isActive() ? "Пауза" : "Пуск"));
        }

        super.render(ctx, mouseX, mouseY, delta);

        // миниатюры фонов поверх прозрачных кнопок
        String hoverName = null;
        for (int i = 0; i < BGS.length; i++) {
            int x = swX[i];
            ctx.fillGradient(x, swY, x + SW_W, swY + SW_H, BGS[i].top(), BGS[i].bottom());
            boolean hovered = mouseX >= x && mouseX < x + SW_W && mouseY >= swY && mouseY < swY + SW_H;
            if (hovered) hoverName = BGS[i].name();
            if (i == bg) border(ctx, x, swY, SW_W, SW_H, 0xFFFFFFFF);
            else if (hovered) border(ctx, x, swY, SW_W, SW_H, 0xAAFFFFFF);
        }
        int areaX = layout == 1 ? PANEL_W : 0;
        int areaCx = areaX + (width - areaX) / 2;
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal(hoverName != null ? hoverName : "Фон: " + BGS[bg].name()),
                areaCx, swY - 12, 0xFFFFFFFF);

        // строка плеера
        String track;
        if (MusicPlayer.tracks().isEmpty()) {
            track = "Нет треков: положи .ogg в config/visualsmod/music и нажми «Обновить»";
        } else if (MusicPlayer.isLoading()) {
            track = "Загрузка трека...";
        } else if (MusicPlayer.currentName() == null) {
            track = "Треков: " + MusicPlayer.tracks().size() + ", нажми «Пуск»";
        } else {
            track = MusicPlayer.currentName();
        }
        String line = track + "   |   Громкость " + Math.round(MusicPlayer.getVolume() * 100) + "%";
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal(line), areaCx, height - 46, 0xFFE6ECF5);
    }

    private void drawStars(DrawContext ctx, float t) {
        for (int i = 0; i < starX.length; i++) {
            float tw = 0.5f + 0.5f * (float) Math.sin(t * 1.5f + starPhase[i] * 6.2831f);
            int a = 60 + (int) (170 * tw);
            int px = (int) (starX[i] * width);
            int py = (int) (starY[i] * height * 0.85f);
            int s = (i % 6 == 0) ? 2 : 1;
            ctx.fill(px, py, px + s, py + s, (a << 24) | 0xFFFFFF);
        }
    }

    private static void border(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x - 1, y - 1, x + w + 1, y, color);
        ctx.fill(x - 1, y + h, x + w + 1, y + h + 1, color);
        ctx.fill(x - 1, y, x, y + h, color);
        ctx.fill(x + w, y, x + w + 1, y + h, color);
    }

    private static int lerp(int a, int b, float t) {
        int r = (int) (((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
        int g = (int) (((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
        int bl = (int) ((a & 255) * (1 - t) + (b & 255) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | bl;
    }
}
