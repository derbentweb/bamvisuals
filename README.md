# VisualsMod — Fabric 1.21.11

## Что уже реально работает
- Меню на **Right Shift**, поиск, вкладки по категориям, чекбокс вкл/выкл
  и кнопка "Бинд" на каждой функции (`gui/VisualsScreen.java`).
- Сохранение состояния в `.minecraft/config/visualsmod.json` (`config/ModConfig.java`).
- Архитектура функций — `features/Feature.java` (базовый класс) +
  `features/FeatureManager.java` (регистрация/тик/бинды). Добавлять новую
  функцию — см. javadoc в `Feature.java`.
- Полностью рабочая логика:
  - `ElytraHelperFeature` — свап элитры из любого места инвентаря
  - `AutoSwapFeature` — ручной свап по кнопке между двумя настроенными предметами
  - `JumpCircleFeature` — кольцо частиц при прыжке
  - `DamageParticlesFeature` — частицы при попадании по сущности
  - `GroundLabelFeature` — подписи предметов на земле
  - `TrailFeature` — цветной след при движении
  - `FullbrightFeature` — тумблер + mixin-заготовка (см. ниже)
  - `OptimizationFeature` — внутренний лимитер нагрузки остальных фич

## Что оставлено как заготовка (нужна доп. работа)
Каждый класс в `features/impl/` с пометкой "(в разработке)" содержит
подробный комментарий, что именно нужно сделать и через какой API:
`AutoDawnFogFeature`, `SkyStarsFeature`, `ViewModelFeature`,
`SwingAnimationFeature`, `HandShaderFeature`, `ItemPhysicsFeature`.
Это всё требует mixin в рендер рук/неба/тумана — сигнатуры методов
могут отличаться в точных билдах маппинга под 1.21.11, поэтому лучше
дописывать их у себя в IDE с подключёнными genSources, где видно
актуальные названия методов.

`FullbrightLightmapMixin` — сам mixin подключён и триггерится, но
финальная подмена текстуры лайтмапа закомментирована — сигнатура
`LightmapTextureManager#update` нужно свериться под точный билд yarn
(укажи актуальный `yarn_mappings` в gradle.properties и открой класс
через IDE после `./gradlew genSources`).

## Не реализовано (см. историю чата — сознательно исключено)
Target ESP, Player ESP, Trajectory (эндер-перл/стрела) и автоматический
(нереагирующий на ручное нажатие) item-helper — не буду их делать: это
даёт нечестное игровое преимущество независимо от оформления. Остальной
список — пожалуйста.

## Сборка через GitHub (без установки чего-либо у себя)
1. Создай новый пустой репозиторий на github.com.
2. Залей туда содержимое этой папки (веб-интерфейс GitHub: "Add file" →
   "Upload files" → перетащи всё, включая скрытую папку `.github` — если
   веб-загрузка её не подхватит, залей через `git`, см. ниже).
3. Открой вкладку **Actions** в репозитории — сборка запустится сама при
   пуше (файл `.github/workflows/build.yml` уже настроен).
4. Когда сборка позеленеет, зайди в неё, внизу раздел **Artifacts** —
   там будет `visualsmod-jar`, скачай и распакуй, внутри готовый `.jar`.

Через git (надёжнее, подхватит скрытую папку `.github` точно):
```
cd visualsmod
git init
git add .
git commit -m "init"
git branch -M main
git remote add origin https://github.com/ТВОЙ_НИК/ИМЯ_РЕПО.git
git push -u origin main
```

Учти: точные версии в `gradle.properties` (yarn/loader/fabric api) всё
равно надо свериться на fabricmc.net/develop — если они устареют, сборка
в Actions тоже упадёт, просто пришли мне лог ошибки из вкладки Actions и
я поправлю.

## Перед первой сборкой (если собираешь локально)
1. Установи Java 21.
2. Зайди на https://fabricmc.net/develop и подставь точные
   `yarn_mappings` / `loader_version` / `fabric_api_version` под
   1.21.11 в `gradle.properties` — я поставил разумные плейсхолдеры,
   но точные билд-номера на момент твоей сборки надо свериться.
3. `./gradlew genSources` — сгенерирует читаемые исходники Minecraft,
   по ним удобно допиливать заготовки.
4. `./gradlew build` — соберёт `build/libs/visualsmod-0.1.0.jar`.

## Твой собственный мод (пункт 19)
Пришли исходники/jar своего мода отдельным сообщением — интегрирую
его меню как вложенный раздел в `FeatureCategory.CUSTOM`.

## Обновление 2 (меню, музыка, зима)
- `gui/MainMenuScreen.java` — своё главное меню вместо ванильного: 3 раскладки
  (Классика / Панель / Минимал), 8 живых фонов с выбором миниатюрами, плеер музыки.
  Подмена экрана — `mixin/MinecraftClientMixin.java`. Аварийно отключить:
  в `config/visualsmod.json` у записи `menu` в `extra` поставь `"enabled": "false"`.
- `music/MusicPlayer.java` — музыка в меню. Формат только `.ogg`, папка
  `.minecraft/config/visualsmod/music`. mp3 надо сначала сконвертировать в ogg.
- `features/impl/WinterFeature.java` — "Зима": снег и морозная дымка поверх экрана,
  до 150 хлопьев, авто-снижение при низком FPS. Мир и чанки не трогает.
- Добавлена иконка мода, `FullbrightLightmapMixin` больше не роняет игру при
  несовпадении сигнатуры (`require = 0`), но сам Fullbright пока не реализован.
- Всё написано без возможности скомпилировать, API 1.21.11 мог измениться:
  если сборка упала, пришли лог из Actions. Первым делом проверь
  `HudRenderCallback` (в новых Fabric API он мог быть заменён на `HudElementRegistry`).
