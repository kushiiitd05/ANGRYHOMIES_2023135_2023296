package com.AngryHomies.AngryBirds.gameplay;

import com.AngryHomies.AngryBirds.assets.Assets;
import com.AngryHomies.AngryBirds.entity.Bird;
import com.AngryHomies.AngryBirds.entity.BirdType;
import com.AngryHomies.AngryBirds.entity.Block;
import com.AngryHomies.AngryBirds.entity.Pig;
import com.AngryHomies.AngryBirds.fx.Clouds;
import com.AngryHomies.AngryBirds.fx.Effects;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Draws a level in world space, back to front: sky, background, clouds, slingshot, rear band,
 * birds/pigs/blocks/fragments, aim line, front band, then effects. Splitting the two bands
 * around the bird is what makes the bird sit *in* the pouch.
 */
public final class WorldRenderer implements com.badlogic.gdx.utils.Disposable {
    private static final Color SKY = new Color(0.69f, 0.86f, 0.93f, 1f);
    private static final Color BAND = new Color(0.27f, 0.14f, 0.07f, 1f);
    private static final Vector2 CATAPULT_POS = new Vector2(13.5f, 15.4f);
    private static final float CATAPULT_W = 30f;
    private static final float CATAPULT_H = 20f;

    private final Texture background;
    private final TextureRegion backgroundMirror;
    private final Texture catapult;
    private final TextureRegion dot;
    private final ObjectMap<BirdType, TextureRegion> birdRegions = new ObjectMap<>();
    private final PolygonSpriteBatch polygons = new PolygonSpriteBatch();

    public WorldRenderer(AssetManager assets, TextureRegion dot) {
        background = assets.get(Assets.LEVEL_BG);
        backgroundMirror = new TextureRegion(background);
        backgroundMirror.flip(true, false); // mirrored copy tiles seamlessly on wide screens
        catapult = assets.get(Assets.CATAPULT);
        this.dot = dot;
        for (BirdType type : BirdType.values()) {
            birdRegions.put(type, new TextureRegion(assets.get(type.texture())));
        }
    }

    public void render(SpriteBatch batch, ShapeRenderer shapes, CameraController camera, LevelWorld world,
                       SlingshotController sling, Effects effects, Clouds clouds, Array<Vector2> aim, float time) {
        Gdx.gl.glClearColor(SKY.r, SKY.g, SKY.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.viewport().apply();
        float alpha = world.alpha();
        Bird bird = world.bird();
        Vector2 pouch = sling.pouch(bird);
        float stretch = sling.stretch();

        batch.setProjectionMatrix(camera.camera().combined);
        shapes.setProjectionMatrix(camera.camera().combined);

        batch.begin();
        batch.draw(background, 0f, 0f, WorldConfig.WIDTH, WorldConfig.HEIGHT);
        batch.draw(backgroundMirror, WorldConfig.WIDTH, 0f, WorldConfig.WIDTH, WorldConfig.HEIGHT);
        clouds.render(batch);
        batch.draw(catapult, CATAPULT_POS.x, CATAPULT_POS.y, CATAPULT_W, CATAPULT_H);
        batch.end();

        drawBand(shapes, SlingshotController.RIGHT_FORK, pouch, stretch);

        batch.begin();
        world.queue().render(batch, birdRegions);
        for (Block block : world.blocks()) {
            block.render(batch, alpha);
        }
        batch.end();
        polygons.setProjectionMatrix(camera.camera().combined);
        polygons.begin();
        world.fragments().render(polygons);
        polygons.end();
        batch.begin();
        for (Pig pig : world.pigs()) {
            pig.render(batch, alpha);
        }
        drawAim(batch, aim, time);
        if (bird != null) {
            bird.render(batch, alpha);
        }
        batch.end();

        drawBand(shapes, SlingshotController.LEFT_FORK, pouch, stretch);

        batch.begin();
        effects.render(batch);
        batch.end();
    }

    private static void drawBand(ShapeRenderer shapes, Vector2 fork, Vector2 pouch, float stretch) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(BAND);
        float width = 1.2f - 0.55f * stretch; // a pulled band gets thinner
        shapes.rectLine(fork, pouch, width);
        shapes.circle(pouch.x, pouch.y, width * 0.6f, 12);
        shapes.end();
    }

    /** Marching dots that shrink and fade along the predicted path. */
    private void drawAim(SpriteBatch batch, Array<Vector2> aim, float time) {
        if (aim.isEmpty()) {
            return;
        }
        Color old = batch.getColor().cpy();
        int phase = (int) (time * 18f) % 3;
        for (int i = phase; i < aim.size; i += 3) {
            float t = i / (float) aim.size;
            float size = 1.25f - 0.65f * t;
            batch.setColor(1f, 1f, 1f, 0.95f - 0.6f * t);
            Vector2 p = aim.get(i);
            batch.draw(dot, p.x - size / 2f, p.y - size / 2f, size, size);
        }
        batch.setColor(old);
    }

    @Override
    public void dispose() {
        polygons.dispose();
    }
}
