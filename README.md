# AngryHomies

A LibGDX + Box2D slingshot physics game inspired by Angry Birds, built for the IIIT-Delhi
Advanced Programming course (2023135, 2023296). **v2** reworks the original course submission
into a smoother, juicier game: real impact physics, material-specific breaking, three special
moves, animated screens, sound effects, and proper save/load.

## Run it

Requires **Java 11+** (tested on 22).

```bash
./gradlew lwjgl3:run      # play
./gradlew core:test       # 30 unit tests (headless Box2D, no window)
```

## How to play

| Input | Action |
|---|---|
| Drag the bird back, release | Launch. A dotted line shows the exact flight path. |
| Click or **Space** in flight | Special move (once per bird) |
| **Esc** / **P** | Pause (volume sliders, aim-line toggle, save, restart) |
| **R** | Restart the level |
| **F11** | Toggle fullscreen |
| Click during the opening fly-over | Skip it |

Destroy every pig. Unused birds score 10,000 each; score thresholds give 1–3 stars; winning a
level unlocks the next.

### Special moves

| Bird | Move | What happens |
|---|---|---|
| Red | **Whirlpool Blade** | Brakes and spins in mid-air while a vortex pulls loose pieces in, then cuts forward: everything on the cut line takes heavy damage, is knocked aside and splits at the cut. |
| Yellow | **Speed Dash** | Doubles speed along its heading with after-images; strongest against wood. |
| Black | **Bomb** | Short fuse, then a radial blast whose damage falls off with distance to each object's surface. It explodes by itself after landing. Strongest against stone. |

## What changed in v2

- **Physics feel**
  - fixed 60 Hz timestep with interpolation, so it plays the same on 60/120/144 Hz screens;
  - stronger gravity;
  - continuous collision on birds.
- **Damage from real impacts**
  - damage follows the impact energy along the contact normal, so resting stacks no longer wear each other down;
  - toughness and health come from the material;
  - cracks appear in three stages.
- **Breaking**
  - glass shatters into many shards radiating from the hit;
  - wood splinters along the grain at random cut points;
  - stone cracks into chunky wedges;
  - every piece is a textured physics body.
- **Accurate aim line**
  - it uses the same integration as Box2D, verified step by step by a unit test;
  - it stops where the path first hits something.
- **Screens**
  - a real async loading bar;
  - animated menus;
  - fade and slide transitions between screens;
  - camera fly-over, zoom-out and screen shake;
  - hit-stop and slow motion on the last pig;
  - a results card with a score count-up and stars.
- **Audio**
  - 23 generated sound effects plus an ambience loop;
  - pitch variation, and volume scaled by impact strength;
  - music crossfades between screens.
- **Save/load**
  - JSON slots next to the game (the old build wrote to a Windows OneDrive path);
  - saves are versioned.
- **Levels are data:** `assets/levels/level*.json`, converted from the old hard-coded Java levels with the layouts unchanged.

The full review behind these changes (30 bugs with file references, the research notes and the
plan) is in [`GAME_IMPROVEMENT_PLAN.md`](GAME_IMPROVEMENT_PLAN.md).

## Architecture

```
Main ── ScreenManager (FBO transitions) ── LoadingScreen / HomeScreen / LevelSelectScreen /
  │                                         LoadScreen / SettingsScreen / GameplayScreen
  ├── AudioManager (Sfx enum, music crossfade)      ├── GameSession   (rules state machine)
  ├── Progress / Settings (Preferences)             ├── LevelWorld    (Box2D, entities, damage,
  └── SaveManager (JSON)                            │                  blast / vortex / blade)
                                                    ├── SlingshotController + TrajectoryPredictor
                                                    ├── CameraController, WorldRenderer
                                                    ├── Effects, FragmentSystem, ShatterPattern
                                                    └── Hud, PauseOverlay, ResultOverlay
Entities: GameEntity → Bird (BirdType + BirdAbility strategy), Pig (PigType), Block (Material)
```

### Adding a level

Create `assets/levels/level4.json` (coordinates in metres; the world is 192 × 108 with the
ground at y = 15.5) and raise `LevelDefinition.LEVEL_COUNT`:

```json
{ "id": 4, "name": "Level 4", "stars": [20000, 35000, 50000],
  "birds": ["RED", "YELLOW", "BLACK"],
  "pigs":   [{"type": "KING", "x": 150, "y": 25, "w": 13.5, "h": 12, "density": 2, "radius": 5.75}],
  "blocks": [{"material": "STONE", "texture": "st_stoneblock.png", "x": 140, "y": 28, "w": 6, "h": 25, "density": 1}] }
```

## Developer tools

- `tools/convert_levels.py`: the one-off converter from the old Java levels to JSON.
- `tools/gen_sfx.py`: regenerates every sound effect, with fixed seeds so the output is reproducible.
- **Demo mode** (`ANGRYHOMIES_DEMO=<folder> ./gradlew lwjgl3:run`):
  - autoplays a level at low volume, aiming each shot at a pig with the trajectory predictor and firing each special move on contact;
  - saves window screenshots and an `events.jsonl` audit log: state changes, launches, moves, every hit and break, and entity snapshots;
  - `ANGRYHOMIES_DEMO_LEVEL` picks the level.

## Credits

- Original game: team 2023135 & 2023296.
- Sound effects: procedurally generated (`tools/gen_sfx.py`), except `red_attack.mp3`, the pig
  oink and the menu music from the original project.
- Bird and pig artwork: Rovio's Angry Birds characters, used here only for a non-commercial
  course project.
- UI skins: the "freezing" and default libGDX skins included with the original project.
