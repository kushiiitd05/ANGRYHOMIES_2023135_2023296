# Changelog

## v2.0.0: `v2/polish` (2026-10-17)

**Author:** Kush Tokas (2023296)

A rework of the original course submission. Every file has its own commit; the tables below list each
file and what changed in it. The reasons (30 bugs with file references, research notes, plan) are in
[`GAME_IMPROVEMENT_PLAN.md`](GAME_IMPROVEMENT_PLAN.md).

**Highlights:** fixed-timestep physics · impact-energy damage · material-specific shattering into textured
physics fragments · exact aim line · Whirlpool Blade / Speed Dash / Bomb · async loading · animated menus and
transitions · camera fly-over, shake, hit-stop, slow motion · 23 generated sound effects · JSON levels and saves ·
30 unit tests · demo autoplay with an audit log.

### Added: gameplay & engine

| File | Change |
|---|---|
| `core/…/Demo.java` | add demo autoplay with aimed shots and frame capture |
| `core/…/DevLog.java` | add demo-mode JSONL event log for auditing runs |
| `core/…/Main.java` | add application root with screen manager, audio, saves and progress |
| `core/…/assets/Assets.java` | centralise asset descriptors with async queueing |
| `core/…/entity/BirdType.java` | add bird types with material specialities |
| `core/…/entity/Block.java` | add blocks with three crack stages |
| `core/…/entity/BodyFactory.java` | add Box2D body factory |
| `core/…/entity/GameEntity.java` | add base entity with interpolation, hit flash and damage tint |
| `core/…/entity/Material.java` | give materials health, toughness, sounds and debris colour |
| `core/…/entity/Pig.java` | add breathing, squashing pigs |
| `core/…/entity/PigType.java` | add pig types with health and toughness |
| `core/…/gameplay/BirdQueue.java` | animate waiting birds and their hop onto the slingshot |
| `core/…/gameplay/CameraController.java` | add intro fly-over, follow zoom and screen shake |
| `core/…/gameplay/DamageModel.java` | model damage from impact energy |
| `core/…/gameplay/LevelWorld.java` | add level world with damage, blasts, vortex and blade cuts |
| `core/…/gameplay/WorldConfig.java` | add world constants and kill bounds |
| `core/…/gameplay/WorldRenderer.java` | draw level with rubber bands, aim dots and fragments |
| `core/…/level/LevelDefinition.java` | load levels from JSON data |
| `core/…/save/Progress.java` | track unlocked levels, best scores and stars |
| `core/…/save/SaveData.java` | add versioned JSON save format |
| `core/…/save/Settings.java` | persist volume, aim-line and fullscreen settings |

### Added: special moves

| File | Change |
|---|---|
| `core/…/entity/ability/AbilityContext.java` | add context for special move effects |
| `core/…/entity/ability/BirdAbility.java` | add special move strategy interface |
| `core/…/entity/ability/DashAbility.java` | add Yellow's Speed Dash with after-images |
| `core/…/entity/ability/WhirlBladeAbility.java` | add Red's Whirlpool Blade |

### Added: effects & audio

| File | Change |
|---|---|
| `core/…/assets/ProceduralTextures.java` | generate particle, cloud, star, ring and crack textures |
| `core/…/audio/AudioManager.java` | add sound manager with pitch variation and music crossfade |
| `core/…/audio/Sfx.java` | add sound effect catalogue with base volumes |
| `core/…/fx/Clouds.java` | add drifting parallax clouds |
| `core/…/fx/Effects.java` | add debris, dust, explosion, swirl, slash and score popups |
| `core/…/fx/FragmentSystem.java` | turn broken blocks into textured physics fragments |
| `core/…/fx/Particle.java` | add pooled particle |
| `core/…/fx/ParticleSystem.java` | add particle system with additive pass |
| `core/…/fx/ShatterPattern.java` | split blocks by material into glass shards, wood splinters and stone wedges |

### Added: screens & UI

| File | Change |
|---|---|
| `core/…/screen/BaseScreen.java` | add base screen with resolution-independent UI and button feedback |
| `core/…/screen/GameplayScreen.java` | rebuild level screen around session rules, effects and overlays |
| `core/…/screen/HomeScreen.java` | animate home menu with bouncing logo and staggered buttons |
| `core/…/screen/LevelSelectScreen.java` | show level cards with stars, best score and locks |
| `core/…/screen/LoadScreen.java` | list save slots with date, score and delete |
| `core/…/screen/LoadingScreen.java` | replace fake 8s loader with async progress bar and hopping bird |
| `core/…/screen/ScreenManager.java` | add screen manager with frame-buffer transitions |
| `core/…/screen/SettingsScreen.java` | add settings screen for volumes, aim line and fullscreen |
| `core/…/screen/transition/FadeTransition.java` | add fade-through-black transition |
| `core/…/screen/transition/ScreenTransition.java` | add screen transition interface |
| `core/…/screen/transition/SlideTransition.java` | add slide transition |
| `core/…/ui/ResultOverlay.java` | add result card with score count-up and stamped stars |

### Fixed (bugs from the original build)

| File | Change |
|---|---|
| `core/…/entity/Bird.java` | fly as a kinematic-then-dynamic body that faces its path |
| `core/…/entity/ability/BombAbility.java` | make Black's bomb damage by distance and auto-detonate |
| `core/…/gameplay/DamageSystem.java` | damage only real impacts and track the entity that was hit |
| `core/…/gameplay/GameSession.java` | wait for the world to settle before deciding win or loss |
| `core/…/gameplay/PhysicsWorld.java` | step Box2D on a fixed clock with render interpolation |
| `core/…/gameplay/SlingshotController.java` | aim in world units and launch with an exact velocity |
| `core/…/gameplay/TrajectoryPredictor.java` | predict the exact Box2D flight path for the aim line |
| `core/…/save/SaveManager.java` | write saves next to the game instead of a Windows OneDrive path |
| `core/…/ui/Hud.java` | use a real pause button and add rolling score and hints |
| `core/…/ui/PauseOverlay.java` | freeze the level on pause instead of zeroing velocities |
| `lwjgl3/src/main/java/com/AngryHomies/AngryBirds/lwjgl3/Lwjgl3Launcher.java` | open a resizable 1600x900 window |

### Tests

| File | Change |
|---|---|
| `test/…/gameplay/DamageModelTest.java` | cover impact energy, toughness and blast falloff |
| `test/…/gameplay/DamageSystemTest.java` | resting stacks take no damage and falls hit the right entity |
| `test/…/gameplay/GameSessionTest.java` | cover intro, settling, win, loss and timeouts |
| `test/…/gameplay/PhysicsWorldTest.java` | same simulation at 30, 60 and 144 Hz |
| `test/…/gameplay/ShatterPatternTest.java` | shatter pieces tile the block for every material |
| `test/…/gameplay/TrajectoryPredictorTest.java` | check the aim line against real Box2D flight |
| `test/…/level/LevelDefinitionTest.java` | every level JSON loads with the original layout |
| `test/…/save/SaveManagerTest.java` | round-trip, corrupt and old-version saves |

### Assets & tools

| File | Change |
|---|---|
| `assets/audio/sfx/*.wav` (24 files) | generated sound effects: ambience, bird hop, bird thud, dash, explosion, fuse, glass break, glass hit, lose, pig pop, score tick, slash, sling release, sling stretch, star, stone break, stone hit, ui click, ui hover, whirl, whoosh, win, wood break, wood hit |
| `assets/assets.txt` | refresh generated asset list |
| `assets/levels/level1.json` | add level1.json converted from the original layout |
| `assets/levels/level2.json` | add level2.json converted from the original layout |
| `assets/levels/level3.json` | add level3.json converted from the original layout |
| `tools/convert_levels.py` | convert the hard-coded Java levels to JSON |
| `tools/gen_sfx.py` | add reproducible procedural sound effect generator |

### Docs & build

| File | Change |
|---|---|
| `.gitignore` | ignore runtime save slots |
| `GAME_IMPROVEMENT_PLAN.md` | add v2 review, research notes and improvement plan |
| `README.md` | document v2 controls, special moves, architecture and tools |
| `core/build.gradle` | unify libGDX version and add headless test dependencies |

### Removed

| File | Change |
|---|---|
| `core/…/Serializables/LevelState.java` | remove LevelState.java (replaced by SaveData JSON) |
| `core/…/Serializables/SerializableBird.java` | remove SerializableBird.java (replaced by SaveData JSON) |
| `core/…/Serializables/SerializableBlock.java` | remove SerializableBlock.java (replaced by SaveData JSON) |
| `core/…/Serializables/SerializableBody.java` | remove SerializableBody.java (replaced by SaveData JSON) |
| `core/…/Serializables/SerializablePig.java` | remove SerializablePig.java (replaced by SaveData JSON) |
| `core/…/Texture/DotTextureFactory.java` | remove DotTextureFactory.java (replaced by ProceduralTextures) |
| `core/…/Texture/TextureFactory.java` | remove TextureFactory.java (replaced by ProceduralTextures) |
| `core/…/assetManager/Main.java` | remove Main.java (replaced by root Main) |
| `core/…/collision/CollisionDetector.java` | remove CollisionDetector.java (replaced by DamageSystem) |
| `core/…/entities/bird/Bird.java` | remove Bird.java (replaced by entity/Bird and BirdType) |
| `core/…/entities/bird/BlackBird.java` | remove BlackBird.java (replaced by entity/Bird and BirdType) |
| `core/…/entities/bird/RedBird.java` | remove RedBird.java (replaced by entity/Bird and BirdType) |
| `core/…/entities/bird/YellowBird.java` | remove YellowBird.java (replaced by entity/Bird and BirdType) |
| `core/…/entities/block/Block.java` | remove Block.java (replaced by entity/Block and Material) |
| `core/…/entities/block/GlassBlock.java` | remove GlassBlock.java (replaced by entity/Block and Material) |
| `core/…/entities/block/StoneBlock.java` | remove StoneBlock.java (replaced by entity/Block and Material) |
| `core/…/entities/block/WoodBlock.java` | remove WoodBlock.java (replaced by entity/Block and Material) |
| `core/…/entities/pig/BigPig.java` | remove BigPig.java (replaced by entity/Pig and PigType) |
| `core/…/entities/pig/KingPig.java` | remove KingPig.java (replaced by entity/Pig and PigType) |
| `core/…/entities/pig/Pig.java` | remove Pig.java (replaced by entity/Pig and PigType) |
| `core/…/entities/pig/SmallPig.java` | remove SmallPig.java (replaced by entity/Pig and PigType) |
| `core/…/logic/Slingshot.java` | remove Slingshot.java (replaced by SlingshotController) |
| `core/…/logic/level/Level.java` | remove Level.java (replaced by GameplayScreen and LevelWorld) |
| `core/…/logic/level/levels/LevelOne.java` | remove LevelOne.java (replaced by assets/levels JSON) |
| `core/…/logic/level/levels/LevelThree.java` | remove LevelThree.java (replaced by assets/levels JSON) |
| `core/…/logic/level/levels/LevelTwo.java` | remove LevelTwo.java (replaced by assets/levels JSON) |
| `core/…/screens/FirstScreen.java` | remove FirstScreen.java (replaced by LoadingScreen) |
| `core/…/screens/HomeScreen.java` | remove HomeScreen.java (replaced by the new screen package) |
| `core/…/screens/LevelSelectScreen.java` | remove LevelSelectScreen.java (replaced by the new screen package) |
| `core/…/screens/LoadScreen.java` | remove LoadScreen.java (replaced by the new screen package) |
| `core/…/screens/LoseScreen.java` | remove LoseScreen.java (replaced by ResultOverlay) |
| `core/…/screens/SaveSuccessScreen.java` | remove SaveSuccessScreen.java (replaced by an in-game toast) |
| `core/…/screens/WinScreen.java` | remove WinScreen.java (replaced by ResultOverlay) |
| `test/…/collision/CollisionDetector.java` | remove obsolete CollisionDetector.java (mocked final libGDX classes) |
| `test/…/entities/bird/BirdTest.java` | remove obsolete BirdTest.java (mocked final libGDX classes) |
| `test/…/logic/SlingshotTest.java` | remove obsolete SlingshotTest.java (mocked final libGDX classes) |
| `saves/default_save_LevelOne.sav` | remove stale Java-serialized save default_save_LevelOne.sav |
| `saves/default_save_LevelThree.sav` | remove stale Java-serialized save default_save_LevelThree.sav |
| `saves/default_save_LevelTwo.sav` | remove stale Java-serialized save default_save_LevelTwo.sav |

## v1.0.0: original course submission

**Authors:** Arshpreet Singh Gill (2023135) and Kush Tokas (2023296)

Static GUI → playable LibGDX game with Box2D, three levels, three birds, Java-serialized saves.
