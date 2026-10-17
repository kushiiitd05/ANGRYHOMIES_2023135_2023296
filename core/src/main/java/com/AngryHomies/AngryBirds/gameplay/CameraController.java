package com.AngryHomies.AngryBirds.gameplay;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * World camera: opening fly-over from the pig fortress to the slingshot, zoom-out to keep a
 * high-flying bird on screen, and trauma-based screen shake.
 *
 * <p>Smoothing uses {@code 1 - e^(-k*dt)}, so the camera eases the same way at any frame rate.
 * Shake is {@code trauma^2} so small knocks are subtle and big blasts are dramatic; trauma decays
 * on its own.
 */
public final class CameraController {
    private static final float INTRO_SECONDS = 1.8f;
    private static final float MAX_ZOOM = 1.7f;
    private static final float SMOOTHING = 5f;
    private static final float MAX_SHAKE = 2.2f;
    private static final float TRAUMA_DECAY = 1.4f;

    private final OrthographicCamera camera = new OrthographicCamera();
    private final ExtendViewport viewport = new ExtendViewport(WorldConfig.WIDTH, WorldConfig.HEIGHT, camera);
    private final Vector2 introFocus = new Vector2(150f, 45f);
    private float zoom = 1f;
    private float targetZoom = 1f;
    private float introTime = -1f;
    private float trauma;
    private float shakeSeed;

    public void startIntro(float focusX, float focusY) {
        introFocus.set(focusX, focusY);
        introTime = 0f;
    }

    public boolean introRunning() {
        return introTime >= 0f && introTime < INTRO_SECONDS;
    }

    public void skipIntro() {
        introTime = -1f;
    }

    /** Ask for enough zoom to keep this point (a flying bird) in view. */
    public void keepInView(float x, float y) {
        float needY = (y + 14f) / viewport.getWorldHeight();
        float needX = (x + 10f) / viewport.getWorldWidth();
        targetZoom = MathUtils.clamp(Math.max(1f, Math.max(needX, needY)), 1f, MAX_ZOOM);
    }

    public void relax() {
        targetZoom = 1f;
    }

    public void addTrauma(float amount) {
        trauma = Math.min(1f, trauma + amount);
    }

    public void update(float delta) {
        shakeSeed += delta * 37f;
        trauma = Math.max(0f, trauma - TRAUMA_DECAY * delta);
        zoom += (targetZoom - zoom) * (1f - (float) Math.exp(-SMOOTHING * delta));

        // Home view keeps the ground on the bottom edge and the slingshot on the left.
        float homeX = viewport.getWorldWidth() * zoom / 2f;
        float homeY = viewport.getWorldHeight() * zoom / 2f;
        float x = homeX;
        float y = homeY;
        float z = zoom;
        if (introTime >= 0f) {
            introTime += delta;
            float t = Interpolation.smooth2.apply(MathUtils.clamp(introTime / INTRO_SECONDS, 0f, 1f));
            float startZoom = 0.6f;
            float startX = MathUtils.clamp(introFocus.x, viewport.getWorldWidth() * startZoom / 2f,
                viewport.getWorldWidth() * (1f - startZoom / 2f));
            float startY = viewport.getWorldHeight() * startZoom / 2f;
            x = MathUtils.lerp(startX, homeX, t);
            y = MathUtils.lerp(Math.max(startY, introFocus.y), homeY, t);
            z = MathUtils.lerp(startZoom, zoom, t);
            if (introTime >= INTRO_SECONDS) {
                introTime = -1f;
            }
        }
        float shake = trauma * trauma * MAX_SHAKE;
        camera.zoom = z;
        camera.position.set(
            x + shake * MathUtils.sin(shakeSeed * 1.3f) * MathUtils.cos(shakeSeed * 0.7f),
            y + shake * MathUtils.sin(shakeSeed * 1.9f + 1.7f), 0f);
        camera.update();
    }

    public Vector2 unproject(int screenX, int screenY) {
        Vector3 v = viewport.unproject(new Vector3(screenX, screenY, 0f));
        return new Vector2(v.x, v.y);
    }

    public void resize(int width, int height) {
        viewport.update(width, height, false);
    }

    public OrthographicCamera camera() {
        return camera;
    }

    public Viewport viewport() {
        return viewport;
    }

    /** Visible world rectangle width/height at the current zoom. */
    public float visibleWidth() {
        return viewport.getWorldWidth() * camera.zoom;
    }

    public float visibleHeight() {
        return viewport.getWorldHeight() * camera.zoom;
    }
}
