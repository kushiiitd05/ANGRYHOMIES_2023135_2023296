package com.AngryHomies.AngryBirds.screen;

import com.AngryHomies.AngryBirds.screen.transition.ScreenTransition;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.utils.Disposable;

/**
 * Runs one screen at a time and animates the switch between two.
 *
 * <p>During a transition both screens render into their own off-screen {@link FrameBuffer}; the
 * transition then draws the two images (crossfade, slide, ...). Input is off until it finishes,
 * and only then is the old screen disposed. Screens never dispose themselves.
 */
public final class ScreenManager implements Disposable {
    private final SpriteBatch batch;
    private BaseScreen current;
    private BaseScreen next;
    private ScreenTransition transition;
    private float elapsed;
    private FrameBuffer currentFbo;
    private FrameBuffer nextFbo;

    public ScreenManager(SpriteBatch batch) {
        this.batch = batch;
    }

    /** Switches to {@code screen}; {@code transition} may be null for an instant cut. */
    public void set(BaseScreen screen, ScreenTransition transition) {
        if (next != null) {
            finishTransition();
        }
        Gdx.input.setInputProcessor(null);
        screen.show();
        screen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        if (current == null || transition == null) {
            replaceCurrent(screen);
            return;
        }
        next = screen;
        this.transition = transition;
        elapsed = 0f;
        ensureFrameBuffers();
    }

    public void render(float delta) {
        if (next == null) {
            if (current != null) {
                current.render(delta);
            }
            return;
        }
        elapsed += delta;
        float progress = Math.min(1f, elapsed / transition.duration());
        TextureRegion from = renderInto(currentFbo, current, delta);
        TextureRegion to = renderInto(nextFbo, next, delta);

        HdpiUtils.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.getProjectionMatrix().setToOrtho2D(0f, 0f, 1f, 1f);
        batch.begin();
        transition.render(batch, from, to, progress);
        batch.end();
        batch.setColor(1f, 1f, 1f, 1f);
        if (progress >= 1f) {
            finishTransition();
        }
    }

    public boolean isTransitioning() {
        return next != null;
    }

    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        disposeFrameBuffers();
        if (next != null) {
            ensureFrameBuffers();
            next.resize(width, height);
        }
        if (current != null) {
            current.resize(width, height);
        }
    }

    public void pause() {
        if (current != null) {
            current.pause();
        }
    }

    public void resume() {
        if (current != null) {
            current.resume();
        }
    }

    private TextureRegion renderInto(FrameBuffer fbo, BaseScreen screen, float delta) {
        fbo.begin();
        screen.render(delta);
        fbo.end();
        TextureRegion region = new TextureRegion(fbo.getColorBufferTexture());
        region.flip(false, true);
        return region;
    }

    private void finishTransition() {
        BaseScreen incoming = next;
        next = null;
        transition = null;
        replaceCurrent(incoming);
    }

    private void replaceCurrent(BaseScreen screen) {
        if (current != null && current != screen) {
            current.hide();
            current.dispose();
        }
        current = screen;
        Gdx.input.setInputProcessor(current.input());
        current.onTransitionComplete();
    }

    private void ensureFrameBuffers() {
        int w = Math.max(1, Gdx.graphics.getBackBufferWidth());
        int h = Math.max(1, Gdx.graphics.getBackBufferHeight());
        if (currentFbo == null) {
            currentFbo = newFbo(w, h);
            nextFbo = newFbo(w, h);
        }
    }

    private static FrameBuffer newFbo(int w, int h) {
        FrameBuffer fbo = new FrameBuffer(Pixmap.Format.RGBA8888, w, h, false);
        fbo.getColorBufferTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return fbo;
    }

    private void disposeFrameBuffers() {
        if (currentFbo != null) {
            currentFbo.dispose();
            nextFbo.dispose();
            currentFbo = null;
            nextFbo = null;
        }
    }

    @Override
    public void dispose() {
        disposeFrameBuffers();
        if (next != null) {
            next.dispose();
        }
        if (current != null) {
            current.hide();
            current.dispose();
        }
    }
}
