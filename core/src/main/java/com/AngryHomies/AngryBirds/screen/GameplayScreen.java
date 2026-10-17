package com.AngryHomies.AngryBirds.screen;

import com.AngryHomies.AngryBirds.Main;
import com.AngryHomies.AngryBirds.assets.Assets;
import com.AngryHomies.AngryBirds.audio.Sfx;
import com.AngryHomies.AngryBirds.entity.Bird;
import com.AngryHomies.AngryBirds.entity.BirdType;
import com.AngryHomies.AngryBirds.entity.Block;
import com.AngryHomies.AngryBirds.entity.GameEntity;
import com.AngryHomies.AngryBirds.entity.Pig;
import com.AngryHomies.AngryBirds.entity.ability.AbilityContext;
import com.AngryHomies.AngryBirds.fx.Clouds;
import com.AngryHomies.AngryBirds.fx.Effects;
import com.AngryHomies.AngryBirds.gameplay.CameraController;
import com.AngryHomies.AngryBirds.gameplay.GameSession;
import com.AngryHomies.AngryBirds.gameplay.LevelWorld;
import com.AngryHomies.AngryBirds.gameplay.PhysicsWorld;
import com.AngryHomies.AngryBirds.gameplay.SlingshotController;
import com.AngryHomies.AngryBirds.gameplay.TrajectoryPredictor;
import com.AngryHomies.AngryBirds.gameplay.WorldConfig;
import com.AngryHomies.AngryBirds.gameplay.WorldRenderer;
import com.AngryHomies.AngryBirds.level.LevelDefinition;
import com.AngryHomies.AngryBirds.save.SaveData;
import com.AngryHomies.AngryBirds.screen.transition.FadeTransition;
import com.AngryHomies.AngryBirds.screen.transition.SlideTransition;
import com.AngryHomies.AngryBirds.ui.Hud;
import com.AngryHomies.AngryBirds.ui.PauseOverlay;
import com.AngryHomies.AngryBirds.ui.ResultOverlay;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * One level being played. Wires input, the rules ({@link GameSession}), the physical level
 * ({@link LevelWorld}), camera, effects, sound, HUD and overlays together, and is the
 * {@link AbilityContext} special moves act through.
 */
public final class GameplayScreen extends BaseScreen
    implements AbilityContext, LevelWorld.Events, PauseOverlay.Listener, ResultOverlay.Listener {

    private static final float TRAIL_INTERVAL = 0.05f;
    private static final float SLOW_MO_SECONDS = 0.8f;
    private static final float SLOW_MO_SCALE = 0.35f;
    private static final int AIM_STEPS = 240;
    private static final int BIRD_BONUS = 10000;
    private static final Color SCORE_WHITE = new Color(1f, 1f, 1f, 1f);
    private static final Color SCORE_GREEN = new Color(0.55f, 1f, 0.3f, 1f);

    private final LevelDefinition level;
    private final LevelWorld world;
    private final SlingshotController sling = new SlingshotController();
    private final CameraController camera = new CameraController();
    private final GameSession session = new GameSession();
    private final Effects effects;
    private final Clouds clouds;
    private final WorldRenderer renderer;
    private final Hud hud;
    private final PauseOverlay pauseMenu;
    private final ResultOverlay results;
    private final Array<Vector2> aim = new Array<>();

    private int score;
    private float hitStop;
    private float slowMo;
    private float trailTimer;
    private int stretchStage;
    private boolean firstShot = true;
    private float endTimer;
    private boolean resultShown;
    private float demoTimer;
    private GameSession.State loggedState;
    private float snapshotTimer;
    private int loggedScore;
    private int demoShot;

    public GameplayScreen(Main game, LevelDefinition level, SaveData save) {
        super(game);
        this.level = level;
        this.world = new LevelWorld(game.assets(), game.textures(), this);
        com.AngryHomies.AngryBirds.DevLog.event("level_start", "level", level.id, "fromSave", save != null,
            "score", save != null ? save.score : 0, "birds", save != null ? save.birds.toString() : level.birds.toString());
        if (save != null) {
            world.restore(save);
            score = save.score;
            session.skipIntro();
        } else {
            world.build(level);
            Vector2 fortress = world.fortressCenter();
            camera.startIntro(fortress.x, fortress.y);
        }
        effects = new Effects(game.textures(), game.skin().getFont("title"));
        clouds = new Clouds(game.textures().cloud(), -40f, 420f, 72f, 102f);
        renderer = new WorldRenderer(game.assets(), game.textures().circle());
        hud = new Hud(stage, game.skin(), game.assets().get(Assets.PAUSE_ICON), level.name,
            game.progress().bestScore(level.id), this::togglePause);
        hud.setScore(score);
        pauseMenu = new PauseOverlay(stage, game.skin(), game.textures().pixel(), game.progress().settings(), this);
        results = new ResultOverlay(stage, game.skin(), game.audio(), game.textures().star(),
            game.textures().starEmpty(), game.textures().pixel());
        input.addProcessor(new WorldInput());
    }

    public static LevelDefinition loadLevel(int id) {
        return LevelDefinition.load(Gdx.files.internal(LevelDefinition.path(id)));
    }

    @Override
    public void show() {
        game.audio().playMusic(Assets.AMBIENCE, 0.7f);
    }

    // ---------- frame ----------

    @Override
    public void render(float delta) {
        time += delta;
        if (!pauseMenu.isOpen()) {
            update(delta);
        }
        renderer.render(game.batch(), game.shapes(), camera, world, sling, effects, clouds, aim, time);
        stage.getViewport().apply();
        stage.act(delta);
        stage.draw();
    }

    private void update(float delta) {
        float timeScale = 1f;
        if (hitStop > 0f) {
            hitStop -= delta;
            timeScale = 0f;
        } else if (slowMo > 0f) {
            slowMo -= delta;
            timeScale = SLOW_MO_SCALE;
        }
        float gameDelta = delta * timeScale;
        world.update(delta, gameDelta, this);
        sling.update(delta);
        effects.update(hitStop > 0f ? delta * 0.05f : gameDelta);
        clouds.update(delta);
        updateBird(gameDelta);
        updateCamera(delta);
        updateSession(delta);
        updateAim();
        updateHints();
        if (com.AngryHomies.AngryBirds.Demo.enabled()) {
            runDemo(delta);
            logChanges(delta);
        }
        hud.setScore(score);
        hud.update(delta);
        if (session.isOver() && !resultShown) {
            endTimer += delta;
            if (endTimer > 0.7f) {
                finishLevel();
            }
        }
    }

    private void updateBird(float delta) {
        Bird bird = world.bird();
        if (bird == null || bird.state() == Bird.State.LOADED) {
            return;
        }
        if (bird.state() == Bird.State.FLYING) {
            trailTimer -= delta;
            if (trailTimer <= 0f) {
                trailTimer = TRAIL_INTERVAL;
                effects.trailPuff(bird.position().x, bird.position().y, false);
            }
        }
        if (bird.isFinished()) {
            Vector2 p = bird.position();
            effects.feathers(bird.type().featherColor(), p.x, p.y, 12);
            effects.dust(p.x, p.y, 4, Color.WHITE);
            world.removeBird();
        }
    }

    private void updateCamera(float delta) {
        Bird bird = world.bird();
        if (bird != null && bird.state() != Bird.State.LOADED) {
            camera.keepInView(bird.position().x, bird.position().y);
        } else {
            camera.relax();
        }
        camera.update(delta);
    }

    private void updateSession(float delta) {
        Bird bird = world.bird();
        boolean birdActive = bird != null && bird.state() != Bird.State.LOADED;
        session.update(delta, birdActive, world.settled(), world.pigs().size, world.birdsLeft());
        if (session.consumeEnteredAiming() && world.bird() == null && world.queue().sendNext()) {
            game.audio().play(Sfx.BIRD_HOP);
        }
        BirdType landed = world.queue().pollLanded();
        if (landed != null) {
            Bird loaded = world.loadBird(landed);
            game.audio().play(Sfx.BIRD_HOP, 0.7f, 0.1f);
            effects.dust(loaded.position().x, loaded.position().y - 2f, 3, Color.WHITE);
        }
    }

    private void updateAim() {
        aim.clear();
        Bird bird = world.bird();
        if (!sling.isDragging() || bird == null || !game.progress().settings().showTrajectory()) {
            return;
        }
        TrajectoryPredictor.predict(bird.position(), sling.launchVelocity(), WorldConfig.GRAVITY, PhysicsWorld.STEP,
            AIM_STEPS, WorldConfig.GROUND_Y, world::pathBlocked, aim);
        int stage = (int) (sling.stretch() * 3.2f);
        if (stage > stretchStage) {
            game.audio().playPitched(Sfx.SLING_STRETCH, 0.6f + stage * 0.15f, 0.9f + stage * 0.1f);
        }
        stretchStage = stage;
    }

    private void updateHints() {
        Bird bird = world.bird();
        if (session.state() == GameSession.State.AIMING && firstShot && bird != null && !sling.isDragging()) {
            hud.showHint("Pull the bird back, aim, and let go");
        } else if (bird != null && bird.state() == Bird.State.FLYING && !bird.isAbilityUsed()) {
            hud.showHint("Click or press SPACE: " + abilityName(bird.type()));
        } else {
            hud.showHint("");
        }
    }

    private static String abilityName(BirdType type) {
        switch (type) {
            case YELLOW:
                return "Speed Dash";
            case BLACK:
                return "Bomb";
            default:
                return "Whirlpool Blade";
        }
    }

    /** Demo-mode audit trail: state changes, score changes and periodic entity snapshots. */
    private void logChanges(float delta) {
        if (session.state() != loggedState) {
            com.AngryHomies.AngryBirds.DevLog.event("state", "from", String.valueOf(loggedState), "to", session.state().name(),
                "pigs", world.pigs().size, "blocks", world.blocks().size, "birdsLeft", world.birdsLeft());
            loggedState = session.state();
        }
        if (score != loggedScore) {
            com.AngryHomies.AngryBirds.DevLog.event("score", "from", loggedScore, "to", score);
            loggedScore = score;
        }
        snapshotTimer -= delta;
        if (snapshotTimer <= 0f) {
            snapshotTimer = 0.5f;
            StringBuilder items = new StringBuilder("[");
            for (Pig pig : world.pigs()) {
                appendEntity(items, pig, pig.type().name());
            }
            for (Block block : world.blocks()) {
                appendEntity(items, block, block.material().name());
            }
            if (items.length() > 1) {
                items.setLength(items.length() - 1);
            }
            items.append(']');
            com.AngryHomies.AngryBirds.DevLog.event("snapshot", "settled", world.settled(), "fragments",
                world.fragments().count(), "items", items.toString());
        }
    }

    private static void appendEntity(StringBuilder out, GameEntity e, String kind) {
        out.append(String.format(java.util.Locale.ROOT, "[%d,\"%s\",%.2f,%.2f,%.1f,%.1f],", e.id, kind,
            e.position().x, e.position().y, e.body().getAngle() * com.badlogic.gdx.math.MathUtils.radiansToDegrees, e.health()));
    }

    /** Scripted play for the developer demo mode (see Demo). */
    private void runDemo(float delta) {
        Bird bird = world.bird();
        if (bird == null) {
            demoTimer = 0f;
            return;
        }
        demoTimer += delta;
        if (session.state() == GameSession.State.AIMING && bird.state() == Bird.State.LOADED) {
            if (!firstShot && demoTimer > 0.3f && com.AngryHomies.AngryBirds.Demo.takeSaveTest()) {
                game.saves().save(world.toSaveData(level.id, score));
                game.screens().set(new GameplayScreen(game, level, game.saves().load(level.id)), new FadeTransition());
                return;
            }
            if (demoTimer > 0.6f && !sling.isDragging()) {
                sling.begin(bird.position(), bird);
                demoPull.set(solvePull(demoTarget()));
            }
            if (sling.isDragging()) {
                float t = Math.min(1f, (demoTimer - 0.6f) / 0.6f);
                sling.drag(new Vector2(demoPull).scl(t).add(SlingshotController.REST), bird);
                if (t >= 1f && demoTimer > 1.6f) {
                    Vector2 velocity = sling.launchVelocity();
                    com.AngryHomies.AngryBirds.DevLog.event("launch", "bird", bird.type().name(), "shot", ++demoShot,
                        "pullX", demoPull.x, "pullY", demoPull.y, "speed", velocity.len(), "angleDeg", velocity.angleDeg());
                    if (sling.release(bird)) {
                        session.onLaunch();
                        firstShot = false;
                        effects.clearTrail();
                        game.audio().play(Sfx.SLING_RELEASE);
                    }
                    demoTimer = 0f;
                }
            }
        } else if (bird.state() != Bird.State.LOADED && !bird.isAbilityUsed() && demoShouldFire(bird)) {
            com.AngryHomies.AngryBirds.DevLog.event("ability", "bird", bird.type().name(), "x", bird.position().x,
                "y", bird.position().y, "speed", bird.body().getLinearVelocity().len(),
                "nearest", nearestTarget(bird.position()), "afterImpact", bird.state() == Bird.State.LANDED);
            bird.tryActivateAbility(this);
        }
    }

    private final Vector2 demoPull = new Vector2();

    /** Each bird fires its move where it shows best: Red close in, Yellow on approach, Black on contact. */
    private boolean demoShouldFire(Bird bird) {
        float nearest = nearestTarget(bird.position());
        switch (bird.type()) {
            case YELLOW:
                return nearest < 40f && bird.state() == Bird.State.FLYING;
            case BLACK:
                return bird.state() == Bird.State.LANDED || nearest < 5f;
            default:
                return nearest < 22f || bird.state() == Bird.State.LANDED;
        }
    }

    /** Cycles through the surviving pigs, left to right. */
    private Vector2 demoTarget() {
        if (world.pigs().isEmpty()) {
            return world.fortressCenter();
        }
        Array<Pig> pigs = new Array<>(world.pigs());
        pigs.sort((a, b) -> Float.compare(a.position().x, b.position().x));
        return new Vector2(pigs.get(demoShot % pigs.size).position());
    }

    /** Brute-force aim: the pull whose predicted path passes closest to {@code target}. */
    private Vector2 solvePull(Vector2 target) {
        Vector2 best = new Vector2(-8f, -4f);
        float bestDistance = Float.MAX_VALUE;
        Array<Vector2> path = new Array<>();
        for (float length = 5f; length <= SlingshotController.MAX_DRAG; length += 0.25f) {
            for (float angle = 5f; angle <= 70f; angle += 1f) {
                Vector2 velocity = new Vector2(length * SlingshotController.SPEED_PER_METRE, 0f).rotateDeg(angle);
                TrajectoryPredictor.predict(SlingshotController.REST, velocity, WorldConfig.GRAVITY, PhysicsWorld.STEP,
                    360, WorldConfig.GROUND_Y, null, path);
                for (Vector2 p : path) {
                    float distance = p.dst(target);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        best.set(velocity).nor().scl(-length);
                    }
                }
            }
        }
        com.AngryHomies.AngryBirds.DevLog.event("aim", "targetX", target.x, "targetY", target.y, "miss", bestDistance);
        return best;
    }

    // ---------- end of level ----------

    private void finishLevel() {
        resultShown = true;
        boolean won = session.state() == GameSession.State.WON;
        com.AngryHomies.AngryBirds.DevLog.event("result", "won", won, "scoreBeforeBonus", score, "birdsUnused", world.queue().positions().size);
        if (won) {
            for (float[] p : world.queue().positions()) {
                score += BIRD_BONUS;
                effects.floatingText(p[0], p[1] + 4f, "+" + BIRD_BONUS, SCORE_WHITE, true);
            }
            if (world.bird() != null && world.bird().state() == Bird.State.LOADED) {
                score += BIRD_BONUS;
            }
        }
        int stars = won ? Math.max(1, level.starsFor(score)) : 0;
        int best = game.progress().bestScore(level.id);
        if (won) {
            game.progress().recordWin(level.id, score, stars);
            game.saves().delete(level.id);
        }
        hud.setScore(score);
        hud.showHint("");
        Gdx.input.setInputProcessor(stage);
        results.show(won, game.assets().get(won ? Assets.WIN_ART : Assets.LOSE_ART), score, stars, best,
            level.id < LevelDefinition.LEVEL_COUNT, this);
    }

    // ---------- LevelWorld.Events: feedback for hits and breaks ----------

    @Override
    public void onHit(GameEntity target, float damage, float x, float y, float power) {
        com.AngryHomies.AngryBirds.DevLog.event("hit", "id", target.id, "kind", kindOf(target), "damage", damage,
            "healthAfter", target.health(), "power", power, "x", x, "y", y);
        score += Math.round(damage * 0.4f) * 10;
        if (target instanceof Block) {
            Block block = (Block) target;
            game.audio().play(block.material().hitSound(), 0.25f + power, 0.12f);
            if (power > 0.3f) {
                effects.dust(x, y, 2, block.material().debrisColor());
            }
        } else if (target instanceof Pig && damage > 4f) {
            game.audio().play(Sfx.PIG_HIT, 0.4f + power * 0.6f, 0.15f);
        }
        if (power > 0.6f) {
            camera.addTrauma(power * 0.12f);
        }
    }

    @Override
    public void onDestroyed(GameEntity target, float x, float y, float power, boolean offscreen) {
        com.AngryHomies.AngryBirds.DevLog.event("destroyed", "id", target.id, "kind", kindOf(target), "offscreen", offscreen,
            "x", x, "y", y, "fragmentsBefore", world.fragments().count());
        if (target instanceof Block) {
            Block block = (Block) target;
            score += block.material().score();
            if (!offscreen) {
                game.audio().play(block.material().breakSound(), 0.5f + power * 0.5f, 0.1f);
                effects.debris(block.material(), block.region(), x, y, power);
                effects.floatingText(x, y + 3f, "+" + block.material().score(), SCORE_WHITE, false);
            }
        } else if (target instanceof Pig) {
            Pig pig = (Pig) target;
            score += pig.type().score();
            if (!offscreen) {
                game.audio().play(Sfx.PIG_POP, 1f, 0.08f);
                effects.pigPop(x, y);
                effects.floatingText(x, y + 5f, "+" + pig.type().score(), SCORE_GREEN, true);
                hitStop(0.06f);
                camera.addTrauma(0.2f);
            }
            if (world.pigs().size <= 1) {
                slowMo = SLOW_MO_SECONDS; // the last pig falls in slow motion
            }
        }
    }

    @Override
    public void onBirdImpact(Bird bird, float speed, float x, float y) {
        com.AngryHomies.AngryBirds.DevLog.event("bird_impact", "bird", bird.type().name(), "speed", speed, "x", x, "y", y);
        if (speed > 6f) {
            game.audio().play(Sfx.BIRD_THUD, Math.min(1f, speed / 50f), 0.1f);
            effects.feathers(bird.type().featherColor(), x, y, Math.min(8, (int) (speed / 8f)));
            camera.addTrauma(Math.min(0.25f, speed / 300f));
        }
    }

    private static String kindOf(GameEntity e) {
        if (e instanceof Pig) {
            return ((Pig) e).type().name();
        }
        return e instanceof Block ? ((Block) e).material().name() : e.getClass().getSimpleName();
    }

    // ---------- AbilityContext ----------

    @Override
    public void play(Sfx sfx) {
        game.audio().play(sfx);
    }

    @Override
    public void shake(float trauma) {
        camera.addTrauma(trauma);
    }

    @Override
    public void hitStop(float seconds) {
        hitStop = Math.max(hitStop, seconds);
    }

    @Override
    public Effects effects() {
        return effects;
    }

    @Override
    public void explode(Bird source, Vector2 center, float radius, float impulse, float damage) {
        com.AngryHomies.AngryBirds.DevLog.event("ability_explode", "x", center.x, "y", center.y, "radius", radius,
            "nearest", nearestTarget(center));
        world.explode(source, center, radius, impulse, damage);
    }

    @Override
    public void vortex(Bird source, Vector2 center, float radius, float impulsePerFrame) {
        world.vortex(source, center, radius, impulsePerFrame);
    }

    @Override
    public void bladeStrike(Bird source, Vector2 from, Vector2 to, float damage) {
        com.AngryHomies.AngryBirds.DevLog.event("ability_blade", "x1", from.x, "y1", from.y, "x2", to.x, "y2", to.y,
            "nearest", nearestTarget(from));
        world.bladeStrike(source, from, to, damage);
    }

    /** Distance from a point to the closest pig or block (for logs and the demo). */
    private float nearestTarget(Vector2 point) {
        float best = Float.MAX_VALUE;
        for (Pig pig : world.pigs()) {
            best = Math.min(best, pig.surfaceDistance(point));
        }
        for (Block block : world.blocks()) {
            best = Math.min(best, block.surfaceDistance(point));
        }
        return best;
    }

    @Override
    public void removeBird(Bird bird) {
        if (world.bird() == bird) {
            world.removeBird();
        }
    }

    // ---------- pause and results ----------

    private void togglePause() {
        if (resultShown) {
            return;
        }
        game.audio().play(Sfx.UI_CLICK);
        if (pauseMenu.isOpen()) {
            pauseMenu.hide();
        } else {
            sling.cancel(world.bird());
            pauseMenu.show(stage);
        }
    }

    @Override
    public void onResume() {
        togglePause();
    }

    @Override
    public void onRestart() {
        game.screens().set(new GameplayScreen(game, level, null), new FadeTransition(0.5f));
    }

    @Override
    public void onSave() {
        togglePause();
        if (session.state() == GameSession.State.AIMING && !sling.isDragging()) {
            game.saves().save(world.toSaveData(level.id, score));
            com.AngryHomies.AngryBirds.DevLog.event("save", "level", level.id, "score", score);
            hud.toast("Game saved");
        } else {
            hud.toast("Save once the shot has finished");
        }
    }

    @Override
    public void onLevelSelect() {
        game.screens().set(new LevelSelectScreen(game), new SlideTransition(SlideTransition.Direction.RIGHT));
    }

    @Override
    public void onHome() {
        game.screens().set(new HomeScreen(game), new FadeTransition());
    }

    @Override
    public void onSettingsChanged() {
        // AudioManager reads the volumes every frame; nothing else to refresh.
    }

    @Override
    public void onMenu() {
        onLevelSelect();
    }

    @Override
    public void onRetry() {
        onRestart();
    }

    @Override
    public void onNext() {
        game.screens().set(new GameplayScreen(game, loadLevel(level.id + 1), null), new FadeTransition());
    }

    @Override
    public void pause() {
        if (!resultShown && !pauseMenu.isOpen()) {
            pauseMenu.show(stage); // lost focus: don't let the level run unattended
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        camera.resize(width, height);
    }

    @Override
    public void dispose() {
        world.dispose();
        renderer.dispose();
        super.dispose();
    }

    /** Mouse/touch and keys for the level itself (the stage gets first pick for UI clicks). */
    private final class WorldInput extends InputAdapter {
        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            if (pauseMenu.isOpen() || resultShown) {
                return false;
            }
            if (session.state() == GameSession.State.INTRO || camera.introRunning()) {
                camera.skipIntro();
                session.skipIntro();
                return true;
            }
            Vector2 point = camera.unproject(screenX, screenY);
            Bird bird = world.bird();
            if (session.state() == GameSession.State.AIMING && sling.begin(point, bird)) {
                stretchStage = 0;
                game.audio().play(Sfx.SLING_STRETCH, 0.5f, 0.05f);
                return true;
            }
            return bird != null && bird.tryActivateAbility(GameplayScreen.this);
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            if (!sling.isDragging()) {
                return false;
            }
            sling.drag(camera.unproject(screenX, screenY), world.bird());
            return true;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            if (!sling.isDragging()) {
                return false;
            }
            if (sling.release(world.bird())) {
                session.onLaunch();
                firstShot = false;
                trailTimer = 0f;
                effects.clearTrail();
                game.audio().play(Sfx.SLING_RELEASE, 0.7f + sling.stretch() * 0.3f, 0.05f);
            }
            return true;
        }

        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.P) {
                togglePause();
                return true;
            }
            if (pauseMenu.isOpen() || resultShown) {
                return false;
            }
            if (keycode == Input.Keys.SPACE && world.bird() != null) {
                world.bird().tryActivateAbility(GameplayScreen.this);
                return true;
            }
            if (keycode == Input.Keys.R) {
                onRestart();
                return true;
            }
            return false;
        }
    }
}
