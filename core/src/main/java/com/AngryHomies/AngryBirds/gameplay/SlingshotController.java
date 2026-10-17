package com.AngryHomies.AngryBirds.gameplay;

import com.AngryHomies.AngryBirds.entity.Bird;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * Pull-and-release aiming.
 *
 * <p>Input arrives in world metres (the screen unprojects it), so aiming is resolution
 * independent — the old code divided raw screen pixels by 10. Launch speed is proportional to
 * the pull distance and the bird's velocity is set directly, which keeps the aim line exact.
 * After release the rubber band recoils on a damped spring.
 */
public final class SlingshotController {
    public static final Vector2 REST = new Vector2(28.4f, 32.2f);
    public static final Vector2 LEFT_FORK = new Vector2(26.4f, 33.6f);
    public static final Vector2 RIGHT_FORK = new Vector2(30.5f, 33.4f);
    public static final float MAX_DRAG = 9f;
    public static final float SPEED_PER_METRE = 8.4f;
    private static final float MIN_LAUNCH_DRAG = 1.2f;
    private static final float GRAB_RADIUS = 7f;
    private static final float RECOIL_SECONDS = 0.45f;

    private final Vector2 offset = new Vector2();
    private final Vector2 recoil = new Vector2();
    private boolean dragging;
    private float recoilTime = -1f;

    public boolean begin(Vector2 worldPoint, Bird bird) {
        if (bird == null || bird.state() != Bird.State.LOADED || worldPoint.dst(bird.position()) > GRAB_RADIUS) {
            return false;
        }
        dragging = true;
        recoilTime = -1f;
        return true;
    }

    /** Moves the bird with the pointer, clamped to the band's reach and above the ground. */
    public void drag(Vector2 worldPoint, Bird bird) {
        if (!dragging || bird == null) {
            return;
        }
        offset.set(worldPoint).sub(REST).limit(MAX_DRAG);
        float lowest = WorldConfig.GROUND_Y + bird.radius() * 0.8f - REST.y;
        offset.y = Math.max(offset.y, lowest);
        bird.moveTo(REST.x + offset.x, REST.y + offset.y);
    }

    /** @return true if the bird was launched; a tiny pull just snaps it back. */
    public boolean release(Bird bird) {
        if (!dragging) {
            return false;
        }
        dragging = false;
        if (bird == null) {
            return false;
        }
        if (offset.len() < MIN_LAUNCH_DRAG) {
            offset.setZero();
            bird.moveTo(REST.x, REST.y);
            return false;
        }
        bird.launch(launchVelocity());
        recoil.set(offset);
        recoilTime = 0f;
        offset.setZero();
        return true;
    }

    /** Abandons a pull (e.g. the game was paused mid-drag) and puts the bird back on the pouch. */
    public void cancel(Bird bird) {
        if (dragging && bird != null) {
            bird.moveTo(REST.x, REST.y);
        }
        dragging = false;
        offset.setZero();
    }

    public void update(float delta) {
        if (recoilTime >= 0f) {
            recoilTime += delta;
            if (recoilTime > RECOIL_SECONDS) {
                recoilTime = -1f;
            }
        }
    }

    /** Velocity the bird would get if released now (a new vector; callers may keep it). */
    public Vector2 launchVelocity() {
        return new Vector2(offset).scl(-SPEED_PER_METRE);
    }

    /** Where the band meets the pouch: behind the loaded bird, or springing back after release. */
    public Vector2 pouch(Bird bird) {
        if (bird != null && bird.state() == Bird.State.LOADED) {
            Vector2 back = new Vector2(offset).nor().scl(bird.radius() * 0.7f);
            return new Vector2(bird.position()).add(back);
        }
        if (recoilTime >= 0f) {
            float t = recoilTime / RECOIL_SECONDS;
            float spring = MathUtils.cos(t * MathUtils.PI * 3f) * (1f - t) * (1f - t);
            return new Vector2(recoil).scl(-spring * 0.6f).add(REST);
        }
        return new Vector2(REST);
    }

    public boolean isDragging() {
        return dragging;
    }

    /** 0 = slack, 1 = fully pulled. */
    public float stretch() {
        return offset.len() / MAX_DRAG;
    }
}
