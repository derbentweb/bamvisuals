@Override
public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    // === ФИКС КРАША "Can only blur once per frame" ===
    boolean renderedBlur = false;

    renderBackground(context, mouseX, mouseY, delta);

    super.render(context, mouseX, mouseY, delta);

    // === РЕНДЕР БЛЮРА (только один раз за кадр) ===
    if (blurEnabled) {
        BlurRenderer.renderBlur(context.getMatrices(), 1.0f); // ← сюда вставь свой вызов BlurRenderer
        renderedBlur = true;
    }

    // === ОБЯЗАТЕЛЬНО: если blur не был нарисован ===
    // Добавь сюда свою отрисовку меню, чтобы экран не был пустым
    // super.render(context, mouseX, mouseY, delta); // раскомментируй, если у тебя есть свой render
}@Override
public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    // === ФИКС КРАША "Can only blur once per frame" ===
    boolean renderedBlur = false;

    renderBackground(context, mouseX, mouseY, delta);

    super.render(context, mouseX, mouseY, delta);

    // === РЕНДЕР БЛЮРА (только один раз за кадр) ===
    if (blurEnabled) {
        BlurRenderer.renderBlur(context.getMatrices(), 1.0f); // ← сюда вставь свой вызов BlurRenderer
        renderedBlur = true;
    }

    // === ОБЯЗАТЕЛЬНО: если blur не был нарисован ===
    // Добавь сюда свою отрисовку меню, чтобы экран не был пустым
    // super.render(context, mouseX, mouseY, delta); // раскомментируй, если у тебя есть свой render
}
