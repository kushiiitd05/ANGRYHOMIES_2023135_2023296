package com.AngryHomies.AngryBirds.screen;

import com.AngryHomies.AngryBirds.Main;
import com.AngryHomies.AngryBirds.assets.Assets;
import com.AngryHomies.AngryBirds.screen.transition.FadeTransition;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;

/**
 * Real loading screen. The old one loaded everything synchronously before showing anything (a
 * black window) and then sat on an 8-second timer. Now only the backdrop and the red bird load
 * first; everything else streams in while a progress bar fills and the bird hops along it.
 */
public final class LoadingScreen extends BaseScreen {
    private static final float MIN_SECONDS = 1.4f;
    private static final float AUTO_CONTINUE = 3.5f;
    private static final String[] TIPS = {
        "Tip: the aim line shows exactly where your bird will fly",
        "Tip: Yellow tears through wood with a Speed Dash",
        "Tip: Black's bomb goes off by itself after landing",
        "Tip: Red's Whirlpool Blade cuts everything on its line",
        "Tip: unused birds are worth 10,000 points each",
    };

    private final Texture background;
    private final TextureRegion bird;
    private final BitmapFont font = new BitmapFont();
    private final GlyphLayout layout = new GlyphLayout();
    private float shown;
    private boolean ready;
    private float readyTime;
    private boolean leaving;

    public LoadingScreen(Main game) {
        super(game);
        Assets.loadBootstrap(game.assets());
        background = game.assets().get(Assets.LOADING_BG);
        bird = new TextureRegion(game.assets().get(Assets.RED_BIRD));
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        Assets.queueAll(game.assets());
    }

    @Override
    public void render(float delta) {
        time += delta;
        if (!ready && game.assets().update(17)) {
            ready = true;
            game.onAssetsLoaded();
        }
        float target = game.assets().getProgress();
        shown += (target - shown) * Math.min(1f, delta * 6f);
        if (ready && time > MIN_SECONDS) {
            readyTime += delta;
            boolean clicked = Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ANY_KEY);
            if (!leaving && (clicked || readyTime > AUTO_CONTINUE)) {
                leaving = true;
                game.screens().set(new HomeScreen(game), new FadeTransition(0.8f));
            }
        }
        draw();
    }

    private void draw() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.getViewport().apply();
        float w = stage.getViewport().getWorldWidth();
        Batch batch = game.batch();
        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.begin();
        batch.setColor(Color.WHITE);
        drawCover(batch, background, 1f + 0.06f * Math.min(1f, time / 6f)); // slow Ken Burns push-in
        batch.end();

        float barW = Math.min(900f, w * 0.6f);
        float barX = (w - barW) / 2f;
        float barY = 110f;
        ShapeRenderer shapes = game.shapes();
        shapes.setProjectionMatrix(stage.getCamera().combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        roundedBar(shapes, barX - 6f, barY - 6f, barW + 12f, 36f, new Color(0f, 0f, 0f, 0.45f));
        roundedBar(shapes, barX, barY, Math.max(24f, barW * shown), 24f, new Color(1f, 0.78f, 0.15f, 1f));
        shapes.end();

        batch.begin();
        float hop = Math.abs(MathUtils.sin(time * 6f)) * 26f;
        float squash = 1f + (hop < 4f ? 0.15f : 0f);
        float size = 78f;
        float birdX = barX + barW * shown - size / 2f;
        batch.draw(bird, birdX, barY + 20f + hop, size / 2f, 0f, size, size, squash, 1f / squash, 0f);
        String message = ready ? "Click to continue" : "Loading " + Math.round(shown * 100f) + "%";
        float pulse = ready ? 0.6f + 0.4f * MathUtils.sin(time * 5f) : 1f;
        font.getData().setScale(2.2f);
        font.setColor(1f, 1f, 1f, pulse);
        layout.setText(font, message);
        font.draw(batch, message, (w - layout.width) / 2f, barY - 24f);
        font.getData().setScale(1.5f);
        font.setColor(1f, 1f, 1f, 0.85f);
        String tip = TIPS[(int) (time / 2.5f) % TIPS.length];
        layout.setText(font, tip);
        font.draw(batch, tip, (w - layout.width) / 2f, barY - 72f);
        batch.end();
    }

    private static void roundedBar(ShapeRenderer shapes, float x, float y, float w, float h, Color color) {
        float r = h / 2f;
        shapes.setColor(color);
        shapes.rect(x + r, y, Math.max(0f, w - 2f * r), h);
        shapes.circle(x + r, y + r, r, 24);
        shapes.circle(x + w - r, y + r, r, 24);
    }

    @Override
    public void dispose() {
        font.dispose();
        super.dispose();
    }
}
