# AngryHomies v2: Review and Improvement Plan

Written 2026-10-17 from a full read of `core/` and `lwjgl3/` (about 4,200 lines of Java) and a build attempt.

**Status (2026-10-17): Phases 0–8 implemented on branch `v2/polish`.**
- Tests: 30 unit tests pass.
- Demo runs: verified on all three levels in autoplay runs (screenshots and an event log).

Deviations from the plan:
- Red's move became the two-beat *Whirlpool Blade* (vortex, then cut) instead of a plain shockwave.
- Damage uses the closing speed measured in `preSolve`, not the summed `postSolve` impulse. Summed impulse also counts the weight of a resting stack and would crush the pieces at the bottom.
- Broken blocks split by material (`ShatterPattern`) into textured polygon bodies, not a fixed grid.
- A demo autoplay mode plus an audit log (`Demo`, `DevLog`) were added to verify behaviour without a human player.

---

## 0. Short answer: why "adding stuff" means big changes

The game works as a course project, but several basics block every new feature:

| Blocker | Why it blocks new features |
|---|---|
| Every position is a hard-coded pixel for 1920×1080, and the game forces fullscreen | Camera follow, zoom and screen shake all need a world camera. On a Mac screen (e.g. 1512×982) the layout is already off. |
| `LevelOne`, `LevelTwo` and `LevelThree` are about 250 lines each and almost identical | Every gameplay change has to be made three times. New levels mean copying the class again. |
| Collision damage fires on *any* touch, and removal goes by type-id string (`==`) | Realistic destruction, debris and score popups need to know *which* object broke and *how hard* it was hit. |
| There is no central audio or asset manager. Sounds and textures are created with `new` on every ability use | Adding sound effects would multiply the memory leaks. |
| Physics steps a fixed 1/60 s once per frame, whatever the frame rate | Smooth animation needs a fixed timestep with interpolation; otherwise the game speed changes with the FPS. |
| Screens are swapped instantly with `setScreen` and dispose themselves on `hide()` | Fade or slide transitions need a screen manager that keeps two screens alive for about 0.4 s. |

So the plan is **foundation first (Phases 0–1), then features (Phases 2–6)**. Rough size:
about 25 new classes, 15 existing classes heavily changed, 3 level classes replaced by JSON
files, and roughly 3,000–4,000 lines touched.

---

## 1. Detailed review: bugs found (with file:line)

### P0: the game does not build or run as committed

| # | Bug | Where |
|---|---|---|
| B1 | **Build fails.** The launcher imports `com.AngryHomies.AngryBirds.Main`, but `Main` is in `...assetManager`. | `lwjgl3/.../Lwjgl3Launcher.java:5` |
| B2 | **Skin path does not exist.** The code loads `uiskin/uiskin2/freezing-ui.json`, but the folder is `assets/uiskin2/` (no `uiskin/` parent). This crashes Home and every level, and LevelSelect's `uiskin/uiskin1` has the same problem. *(To confirm at first run.)* | `HomeScreen`, `Level.java:100`, `LevelSelectScreen` |
| B3 | **Save path is hard-coded for Windows OneDrive** (`~/OneDrive/Desktop/AngryHomies/saves`). Save and load fail on the Mac. | `Level.java:89`, `LoadScreen` |
| B4 | **libGDX version mismatch.** `core` uses gdx 1.11.0 while `lwjgl3` uses 1.12.1, so two versions are on the classpath. | `core/build.gradle`, `gradle.properties` |

### P1: gameplay logic bugs

| # | Bug | Where |
|---|---|---|
| B5 | **The wrong block or pig gets removed.** Removal matches the *type id* (`"wood_block"`) with `==`, and every wood block has the same id, so hitting one wood block removes the *first* wood block in the list. Repeated contacts queue the same id several times, which removes several blocks. | `CollisionDetector.processRemovals` |
| B6 | **Damage ignores impact strength.** A bird brushing a block does full `attackPower` damage. Pigs touching each other lose 5 HP per contact, so stacked pigs hurt each other while resting. | `CollisionDetector.beginContact` |
| B7 | **The trajectory preview does not match the real flight.** The preview uses `velocity = drag × 5` with gravity −8, but the launch applies `impulse = drag × 500` (capped at 4,500), divided by bird mass. The dots lie. | `Slingshot.touchDragged` vs `touchUp` |
| B8 | **Bird gravity scale jumps around:** 100.5 at creation, 8 then 0 on reset, 1 at launch. Waiting birds fall at 100× gravity. | `Bird.createBody`, `Level.resetBirdPosition` |
| B9 | **The out-of-bounds check never fires to the right.** `getWidth()+20 / PIXELS_TO_METERS` divides only the 20 (operator precedence) and compares metres with pixels. | `Level.java:177-178` |
| B10 | **The black bird's explosion** divides by distance (infinite at 0) and only pushes bodies; it never damages pigs or blocks. | `BlackBird.onSpecialAbility` |
| B11 | **The pause button is checked with `isTouched()` every frame.** Holding the mouse adds a new menu table each frame, and dragging the bird over the icon pauses the game. | `Level.render:273-280` |
| B12 | **Pausing zeroes velocities** instead of stopping the physics step, and the original velocities are never copied (`getLinearVelocity()` returns a shared vector). Resume can restore wrong values. | `Level.showPauseMenu` |
| B13 | **Physics runs at frame rate:** `world.step(1/60f)` once per frame. On a 120 Hz Mac the game runs at 2× speed. | `Level.render:252` |
| B14 | **"Lost" fires 2 s after the last bird is removed**, even while a pig is still falling to its death. There is no settle check. | `Level.checkGameState` |
| B15 | **The bird is removed the moment its speed drops below 0.1**, which can happen at the top of a lob. | `Level.checkBirdLaunchState` |
| B16 | **LevelThree builds `WoodBlock` objects with the stone texture**, so the material and the look disagree. | `LevelThree` |

### P1: screen and flow bugs

| # | Bug | Where |
|---|---|---|
| B17 | **The back button on LevelSelect** opens a *LevelOne* stored as `"HomeScreen"` when Home isn't cached. | `LevelSelectScreen` back listener |
| B18 | **The `HomeScreen(game, levelScreens)` constructor leaves `music` null**, so `show()` crashes when it's used from `Level.exitToHomeScreen`. | `HomeScreen` |
| B19 | **WinScreen disposes `win_screen.png`, which the AssetManager owns.** The second win crashes or draws garbage. | `WinScreen.dispose` |
| B20 | **Win, Lose and Load draw their background at 650×530** using `game.batch`'s window-sized projection, so they look tiny or stretched. | `WinScreen`, `LoseScreen`, `LoadScreen`, `LevelSelectScreen` |
| B21 | **The loading screen is fake.** All assets load *synchronously* in `Main.create()` before anything is drawn (a black window), then a fixed 8-second timer runs. The text position uses the texture's pixel width, not the viewport. | `Main.create`, `FirstScreen` |
| B22 | **Fullscreen is forced twice** (`Main.create` and the `FirstScreen` constructor), and windowed mode is 640×480. | `Main`, `FirstScreen`, launcher |

### P2: resources, code quality and tests

| # | Issue | Where |
|---|---|---|
| B23 | **`new Texture(...)` runs for every block** (about 30 unmanaged textures per level, never disposed), and every ability use creates a new `Texture` and `Sound`. | `LevelOne/Two/Three`, bird abilities |
| B24 | **Sound effects are loaded as `Music`**, which streams; `Sound` is the right class for short clips. All three birds play `red_attack.mp3`. | `Main.loadAssets`, birds |
| B25 | **Three near-duplicate level classes** (250+ lines each). | `logic/level/levels/*` |
| B26 | **Static mutable lists in `CollisionDetector`** are shared across levels, so a restart can inherit queued removals. | `CollisionDetector` |
| B27 | **Java serialization of `LevelState` is fragile.** Any class change breaks old saves, and committed `.sav` files are binary. | `Serializables/*`, `saves/` |
| B28 | **Everything renders twice:** `stage.act/draw` in both `Level.render` and each `renderGame`, the pause icon is drawn twice, and `glClear` is called inside `batch.begin()`. | `Level`, `LevelOne/Two/Three` |
| B29 | **The tests need Box2D natives and mock final libGDX classes.** The root `build.gradle` points tests at `test/AB_Final` (overridden in core), and there is a stray copy of `CollisionDetector` under `test/`. | `core/src/test`, `build.gradle` |
| B30 | **`System.out.println` debug output** throughout, and no logging levels. | everywhere |

---

## 2. New architecture: more classes, single responsibility

```
core/src/main/java/com/AngryHomies/AngryBirds/
├── AngryHomiesGame.java            (was assetManager/Main) – owns ScreenManager, Assets, Audio, Prefs
├── screen/
│   ├── ScreenManager.java          push/replace screens, runs transitions, owns dispose
│   ├── transition/ScreenTransition.java   interface: render(current, next, alpha)
│   ├── transition/FadeTransition.java
│   ├── transition/SlideTransition.java
│   ├── BaseScreen.java             shared viewport, stage, input multiplexer, resize
│   ├── LoadingScreen.java          (was FirstScreen) real async progress
│   ├── HomeScreen.java  LevelSelectScreen.java  LoadScreen.java  SettingsScreen.java (new)
│   └── ResultScreen.java           (merges Win/Lose) stars, score, retry/next
├── assets/
│   ├── Assets.java                 AssetDescriptor constants + queueAll(), one place for paths
│   └── TextureAtlasPacker (gradle task, optional) → assets/atlas/game.atlas
├── audio/
│   ├── AudioManager.java           play(Sfx), pitch/volume variation, music crossfade, mute
│   └── Sfx.java (enum)             STRETCH, LAUNCH, WOOD_HIT, GLASS_BREAK, STONE_HIT, PIG_HIT, PIG_POP,
│                                   BIRD_ABILITY_RED/YELLOW/BLACK, EXPLOSION, WIN, LOSE, CLICK
├── level/
│   ├── LevelDefinition.java        POJO read from assets/levels/levelN.json
│   ├── LevelLoader.java            JSON → entities (replaces LevelOne/Two/Three)
│   └── GameplayScreen.java         (was Level) one render/update path for all levels
├── gameplay/
│   ├── GameState.java (enum)       AIMING, FLYING, SETTLING, WON, LOST, PAUSED
│   ├── GameSession.java            state machine + rules (win/lose/settle)
│   ├── PhysicsWorld.java           fixed timestep accumulator, interpolation alpha, removal queue
│   ├── DamageSystem.java           ContactListener; damage from postSolve impulse
│   ├── ScoreSystem.java            points, stars, best score per level
│   ├── SlingshotController.java    input → drag → launch (was Slingshot)
│   ├── TrajectoryPredictor.java    Box2D-exact preview (same impulse, mass, gravity)
│   └── CameraController.java       follow bird, zoom out, clamp, shake
├── entity/
│   ├── GameEntity.java             base: body, sprite, health, render(batch, alpha)
│   ├── bird/Bird.java + RedBird, YellowBird, BlackBird, BirdState (enum)
│   ├── pig/Pig.java + Small/Big/King
│   └── block/Block.java + Material (enum: WOOD, GLASS, STONE → density, hp, sound, debris colour)
├── fx/
│   ├── ParticleSystem.java         pooled particles (Pool<Particle>)
│   ├── Particle.java
│   ├── Effects.java                debris(material), dustPuff, feathers, explosion, smoke
│   ├── FloatingText.java           "+5000" score popups
│   └── Tweens.java                 small helpers on libGDX Interpolation (no extra lib)
├── ui/
│   ├── Hud.java                    score, birds left, pause ImageButton (scene2d, not isTouched)
│   └── PauseOverlay.java           dimmed Window, Resume / Restart / Save / Settings / Home
└── save/
    ├── SaveManager.java            Json → Gdx.files.local("saves/slotN.json"), versioned
    └── SaveData.java               plain data classes (replace Serializables/*)
```

The old classes are removed once the replacements pass their tests:
`LevelOne/Two/Three`, `Serializables/*`, `FirstScreen`, `WinScreen`/`LoseScreen`, `Texture/*`.

---

## 3. Phased plan: what changes and how it works

### Phase 0: build and run on the Mac (small, about 1 h)
1. Fix the launcher import (B1), unify the gdx version to `$gdxVersion` (B4) and fix the skin paths (B2).
2. Remove the forced fullscreen. Start windowed at 1600×900, resizable, with fullscreen on F11.
3. Move saves to `Gdx.files.local("saves/")` (B3).
4. Run `./gradlew lwjgl3:run` as a smoke test, take screenshots, and record the baseline FPS.

### Phase 1: foundation (large, the core of the refactor)

**1a. Resolution independence.** *How it works:* two viewports.
- The **world** uses an `ExtendViewport(192, 108)` in metres (1 m = 10 px of the old layout), so the old coordinates map directly. The camera can move and zoom.
- The **UI** uses an `ExtendViewport(1920, 1080)` in virtual pixels for menus and the HUD.
- The window can be any size. Each viewport scales its own content, and nothing reads `Gdx.graphics.getWidth()` for layout any more.

**1b. Fixed timestep with interpolation** (fixes B13). *How it works:*
```
accumulator += min(delta, 0.25)
while accumulator >= STEP (1/60):  savePrevTransforms(); world.step(STEP, 8, 3); accumulator -= STEP
alpha = accumulator / STEP
render each body at lerp(prev, current, alpha)
```
Physics is identical at 60, 120 or 144 Hz, and motion looks smooth on high-refresh screens.

**1c. ScreenManager and transitions** (fixes B17–B19 and B22). *How it works:*
- During a transition, the current and next screens each render into their own `FrameBuffer`.
- A shader-free blend draws them with `batch.setColor(1,1,1,a)`: fade = alpha crossfade, slide = offset by `Interpolation.pow3Out`.
- About 0.35 s. Input is disabled until the transition finishes.
- The manager, not `hide()`, decides when a screen is disposed.

**1d. Assets and the loading screen** (fixes B21, B23 and B24). *How it works:*
- Only the loading-screen background and a font are loaded synchronously.
- Everything else is queued, and each frame calls `assetManager.update(16ms)`.
- `getProgress()` drives a real progress bar.
- Short clips are `Sound` and long ones `Music`. All textures come from the manager, and none use `new Texture`.
- Optional: pack the images into one `TextureAtlas` (fewer texture switches, faster drawing).

**1e. Data-driven levels** (fixes B25 and B16). Each level becomes JSON:
```json
{ "name": "Level 1", "birds": ["red","black","yellow"],
  "blocks": [{"material":"WOOD","x":138.7,"y":17.4,"w":4,"h":4,"rot":0}],
  "pigs":   [{"type":"KING","x":149.6,"y":25.0}],
  "stars":  [20000, 35000, 50000] }
```
A Python one-off converts the current Java coordinates into these files, so the layouts stay exactly as they are now.

### Phase 2: game logic (large)

**2a. GameSession state machine** (fixes B14 and B15). *How it works:*
```
AIMING  --release-->  FLYING  --bird slow for 1.5 s or off-screen or 8 s-->  SETTLING
SETTLING --all bodies asleep (or 3 s) & no pigs--> WON
SETTLING --all bodies asleep & pigs remain & birds left--> AIMING (next bird hops to sling)
SETTLING --all asleep & pigs remain & no birds--> LOST
```
"Asleep" means Box2D's `body.isAwake()` is false, or speed stays below 0.05 m/s for 0.5 s.

**2b. DamageSystem based on impact impulse** (fixes B5, B6 and B26). *How it works:*
- Use `postSolve(contact, impulse)`, not `beginContact`, and sum `impulse.getNormalImpulses()`.
- `damage = max(0, impulse − threshold[material]) × factor`, with `bird.attackMultiplier` when a bird is involved.
- Resting contacts produce almost no impulse, so stacked pigs no longer hurt each other.
- Hits are queued by **object reference** in an instance-level `Set`, and bodies are destroyed after `world.step` (Box2D forbids destroying bodies during a step).

**2c. Slingshot and an exact trajectory** (fixes B7 and B8). *How it works:*
- Launch velocity is `v = impulse / mass`, and the preview uses the same formula, stepping `p += v·dt; v += g·dt` at the same `dt` as the world. The dots then match the flight.
- The bird is a kinematic body while aimed, so gravity doesn't matter, and becomes dynamic at launch with gravity scale 1 and `setBullet(true)` (continuous collision, so fast birds don't tunnel through glass).
- The rubber band is drawn with `ShapeRenderer`: two lines from the fork tips to the bird, thicker and darker the more it's stretched, with a small snap-back tween after release.
- Pull strength rises along a curve (`Interpolation.pow2`) for finer control at low power.

**2d. Bird abilities, done properly** (fixes B10, B23 and B24).
- **Red:** a battle-cry sound, a feather burst, and +attack for 1 s.
- **Yellow:** a speed dash along the current velocity (×2.2 velocity, not a fixed impulse), with a speed-line trail.
- **Black:** after a 0.25 s fuse, a radial impulse of `strength × (1 − d/r)²` (no division by zero), *damage* to everything within the radius, an explosion effect and screen shake.
- Each ability fires once, on the next click after launch (not the spacebar only).

**2e. CameraController.** *How it works:*
- While aiming, the view frames the slingshot and the structure.
- In flight, the camera follows the bird with an exponential lerp (`cam += (target − cam) × (1 − e^(−8·dt))`, frame-rate independent) and zooms out with speed.
- During settling, it pans to the structure.
- Shake decays over time (`trauma²`), with a random offset each frame.

**2f. Scoring and progression.** Points come from block destruction (by material), +5,000 per pig and +10,000 per unused bird. The level's star thresholds give 1–3 stars, saved in `Preferences`. Level 2 unlocks when Level 1 is won.

### Phase 3: loading screen and menu animation (medium)

- **Loading screen:**
  - the background slowly zooms (Ken Burns effect, scale 1.0 → 1.05 over the load);
  - a real progress bar fills with `Interpolation.smooth`;
  - the red bird sprite hops along the bar with a squash and stretch (`scaleY = 1 + 0.15·sin`);
  - tips text cycles;
  - when progress reaches 1, "Tap to continue" pulses (with a 1.2 s minimum display, replacing the 8 s timer), then a fade into Home.
- **Home:** the logo drops in (`Interpolation.bounceOut`), the buttons slide in one after another (0.08 s apart), hover makes a button grow by 1.05 with a click sound, and the music fades in.
- **Level select:** cards pop in with `swingOut`, show stars and best score, and locked levels are greyed with a lock.
- **Results:** the score counts up, stars stamp in one by one (scale 2 → 1, then dust and a sound), with Retry / Next / Menu buttons.
- All of this uses scene2d `Actions` (`sequence`, `parallel`, `delay`, `moveTo`, `scaleTo`, `fadeIn`) and needs no extra library.

### Phase 4: effects and "real feel" (medium)

| Effect | How it works |
|---|---|
| Debris on block break | 8–14 pooled particles tinted by material (wood brown, glass light-blue, stone grey). Random velocity cone, gravity, spin, and an alpha fade over 0.6–1.0 s. |
| Pig pop | Green puff ring of particles plus scale 1 → 1.3 → 0, then a "+5000" floating text. |
| Hit flash | On damage, the sprite is tinted white or red for 80 ms (`batch.setColor`). |
| Damage states | Health below 66% gets a lighter tint; below 33%, a crack overlay or darker tint with a small wobble. |
| Bird trail | A dot dropped every 0.05 s during flight, kept on screen until the next launch (like the original game). |
| Squash and stretch | The bird stretches along its velocity in flight and squashes on its first impact (tween back over 0.15 s). |
| Idle life | Waiting birds blink and hop (random 1–3 s timer); pigs bob slightly. |
| Slow motion | When the last pig dies, time scales to 0.3 for 0.6 s (physics `STEP` unchanged; the accumulator scales `delta`). |
| Screen shake | Explosions and heavy stone hits feed `trauma`, and `CameraController` applies it. |

### Phase 5: audio (medium; content needed)

- **AudioManager:**
  - `play(Sfx id)` with a random pitch of 0.9–1.1 so repeats don't sound robotic;
  - volume scaled by impact impulse (soft taps are quiet);
  - a per-sound limit of at most 3 at once, to avoid clipping;
  - music crossfades over 0.6 s per screen;
  - master, music and SFX sliders and mute, saved in `Preferences`.
- **Where the sounds come from.** Only 3 sounds exist today. Two options, both license-safe:
  1. **Kenney.nl CC0 packs** ("Impact Sounds", "Interface Sounds"): public-domain, high quality.
  2. **Procedural generation:** a small `tools/gen_sfx.py` (numpy) that synthesises whoosh, thud, glass-shatter, pop and explosion clips as `.wav`. It is reproducible and free.
- Do **not** add sounds or art copied from the real Angry Birds game. The repo is public, and Rovio owns those.

### Phase 6: save/load and settings (small–medium)

- **SaveManager** writes JSON with a version number:
  - slots 1–3, saved via `Gdx.files.local`;
  - each slot stores bird, pig and block transforms, health, current state and score;
  - the Load screen shows the slot's level, date and score;
  - "Empty slot" is shown when there's no save.
- **SettingsScreen:** volume sliders, fullscreen toggle, trajectory on/off, and a reset-progress button.

### Phase 7: tests (alongside every phase, TDD)

Tests use the `gdx-backend-headless` dependency, so Box2D runs in tests without a window and without mocking final classes.

| Test | Proves |
|---|---|
| `TrajectoryPredictorTest` | The predicted point at t = 1 s is within 0.1 m of the real Box2D body after the same launch. |
| `DamageSystemTest` | A resting stack takes 0 damage; a high-impulse hit destroys glass; the correct object is removed (B5). |
| `GameSessionTest` | The state transitions in 2a, including "pig still falling → not lost yet". |
| `LevelLoaderTest` | Every `levels/*.json` loads, and the entity counts match. |
| `SaveManagerTest` | Save → load round trip keeps positions, health and state. |
| `ScoreSystemTest` | Points and star thresholds. |
| `PhysicsWorldTest` | The same simulation result at 60 Hz and 144 Hz frame deltas. |

### Phase 8: docs

Update `README.md` (controls, architecture diagram, how to add a level in JSON, credits for the CC0 audio) and add a `CHANGELOG.md` for v2.

---

## 4. Order of work and how big each step is

| Order | Phase | Size | Can the game run after it? |
|---|---|---|---|
| 1 | 0: build fixes | S | Yes (first time it runs on the Mac) |
| 2 | 1a–1b: viewports and timestep | M | Yes |
| 3 | 1c–1d: screens, transitions, assets, loading | L | Yes |
| 4 | 1e: JSON levels (delete LevelOne/Two/Three) | M | Yes, same layouts |
| 5 | 2a–2c: state machine, damage, slingshot | L | Yes, plays correctly |
| 6 | 2d–2f: abilities, camera, score | M | Yes |
| 7 | 3: menu and loading animation | M | Yes |
| 8 | 4: effects | M | Yes |
| 9 | 5: audio | M | Yes (needs the sound files) |
| 10 | 6: save and settings | S–M | Yes |
| 11 | 8: docs | S | Yes |

Each step ends with `./gradlew test` passing and a manual play-through: screenshots of the loading screen, aiming, a hit and the results screen.

---

## 4b. Research notes per component (2026-10-17)

| Component | What the research says | Decision for v2 |
|---|---|---|
| Timestep | Glenn Fiedler's "Fix Your Timestep!" is the standard pattern. The interpolation must be `prev + (cur − prev)·alpha`, starting from the *previous* state. A common bug interpolates from the current state. | `PhysicsWorld` keeps previous and current transforms per body. Frame delta is capped at 0.25 s to avoid a spiral of death. |
| Damage | Angry Birds clones read the collision strength from `PostSolve`'s impulse and compare it to a per-material threshold. PostSolve fires only for touching, awake contacts. The world is locked during callbacks, so queue and destroy after the step. | `DamageSystem` sums the normal impulses, uses per-material thresholds, and queues by object reference. |
| Transitions | Render both screens to FrameBuffers and blend or slide the two textures (libGDX Cookbook; *Learning LibGDX* ch. 9; Spectrio). Libraries such as Universal Tween Engine are optional; libGDX's `Interpolation` is enough. | `ScreenManager` with FBO snapshots plus `Interpolation`, and no extra library. |
| Game feel | "Juice it or lose it" (Jonasson & Purho, GDC 2012): flash, shake, particles, sound and floating text. Keep shake small. A 50–100 ms hit-stop on heavy hits sells the impact. Juice cannot fix unresponsive controls, so controls come first. | Phase 2 (controls and physics) comes before Phase 4 (juice). Hit-stop of 60 ms on a pig kill or stone break. Shake trauma capped at 0.6. |
| Audio content | Kenney.nl audio packs (Impact Sounds, 130+ sounds; Interface Sounds) are CC0, with attribution optional. | Use procedurally generated SFX now (offline, reproducible, `tools/gen_sfx.py`). Kenney CC0 is an optional drop-in replacement later. |

Sources: [libGDX Cookbook: fixed timestep](https://www.oreilly.com/library/view/libgdx-cross-platform-game/9781783287291/ch10s11.html) ·
[gamedev.net interpolation thread](https://gamedev.net/forums/topic/714764-fixed-timestep-with-interpolation-2d-movement-jitterstutter) ·
[Emanuele Feronato: killing the pigs with PostSolve](https://emanueleferonato.com/?p=4765) ·
[Seth Ladd: Box2D collision damage](https://blog.sethladd.com/2011/09/box2d-collision-damage-for-javascript.html) ·
[Spectrio: libGDX screen transitions](https://www.spectrio.com/team/animating-transitions-between-libgdx-screens-using-tweenengine/) ·
[Learning LibGDX ch. 9](https://www.oreilly.com/library/view/learning-libgdx-game/9781783554775/ch09s02.html) ·
[Game feedback feel overview](https://eastondev.com/blog/en/posts/dev/20260521-game-feedback-feel/) ·
[Kenney CC0 audio overview](https://gtstu.com/?p=5011)

---

## 5. Git handling

- The original history (153 commits) stays untouched. It is the record of the original work.
- All v2 work goes on the branch `v2/polish`, committed one file per commit with conventional messages, then merged by PR.
- Nothing is pushed or deleted on GitHub without an explicit go-ahead. The repo also carries the
  teammate's roll number (2023135), so deleting it or rewriting history affects them too.
