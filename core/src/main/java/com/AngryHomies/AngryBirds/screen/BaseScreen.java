package com.AngryHomies.AngryBirds.screen;

import com.AngryHomies.AngryBirds.Main;
import com.AngryHomies.AngryBirds.audio.Sfx;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Shared plumbing for every screen: a UI stage on a 1920x1080 virtual canvas that stretches to any
 * window shape, an input multiplexer, background drawing and button feedback.
 */
public abstract class BaseScreen implements Screen {
    public static final float UI_WIDTH = 1920f;
    public static final float UI_HEIGHT = 1080f;

    protected final Main game;
    protected final Stage stage;
    protected final InputMultiplexer input = new InputMultiplexer();
    protected float time;

    protected BaseScreen(Main game) {
        this.game = game;
        this.stage = new Stage(new ExtendViewport(UI_WIDTH, UI_HEIGHT), game.batch());
        input.addProcessor(stage);
    }

    public InputProcessor input() {
        return input;
    }

    /** Called once the screen is fully visible and receiving input. */
    public void onTransitionComplete() {
    }

    @Override
    public void render(float delta) {
        time += delta;
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.getViewport().apply();
        Batch batch = stage.getBatch();
        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.begin();
        drawBackground(batch, delta);
        batch.end();
        stage.act(delta);
        stage.draw();
    }

    /** Override to paint behind the stage; coordinates are the UI canvas. */
    protected void drawBackground(Batch batch, float delta) {
    }

    /**
     * Draws {@code texture} to cover the whole visible canvas without distortion (cropping the
     * overflow), with an optional slow zoom for a living "Ken Burns" look.
     */
    protected void drawCover(Batch batch, Texture texture, float zoom) {
        Viewport viewport = stage.getViewport();
        float viewW = viewport.getWorldWidth();
        float viewH = viewport.getWorldHeight();
        float scale = Math.max(viewW / texture.getWidth(), viewH / texture.getHeight()) * zoom;
        float w = texture.getWidth() * scale;
        float h = texture.getHeight() * scale;
        batch.draw(texture, (viewW - w) / 2f, (viewH - h) / 2f, w, h);
    }

    /** Hover grows the actor slightly, click plays a sound and runs {@code onClick}. */
    protected <T extends Actor> T juicyButton(T actor, Runnable onClick) {
        setTransformIfGroup(actor);
        actor.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1 && (fromActor == null || !fromActor.isDescendantOf(actor))) {
                    actor.setOrigin(actor.getWidth() / 2f, actor.getHeight() / 2f);
                    actor.addAction(Actions.scaleTo(1.07f, 1.07f, 0.12f, Interpolation.pow2Out));
                    game.audio().play(Sfx.UI_HOVER);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1 && (toActor == null || !toActor.isDescendantOf(actor))) {
                    actor.addAction(Actions.scaleTo(1f, 1f, 0.12f, Interpolation.pow2Out));
                }
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.audio().play(Sfx.UI_CLICK);
                actor.setOrigin(actor.getWidth() / 2f, actor.getHeight() / 2f);
                actor.addAction(Actions.sequence(
                    Actions.scaleTo(0.93f, 0.93f, 0.06f), Actions.scaleTo(1.07f, 1.07f, 0.1f, Interpolation.swingOut)));
                onClick.run();
            }
        });
        return actor;
    }

    private static void setTransformIfGroup(Actor actor) {
        if (actor instanceof com.badlogic.gdx.scenes.scene2d.Group) {
            ((com.badlogic.gdx.scenes.scene2d.Group) actor).setTransform(true);
        }
    }

    @Override
    public void show() {
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
