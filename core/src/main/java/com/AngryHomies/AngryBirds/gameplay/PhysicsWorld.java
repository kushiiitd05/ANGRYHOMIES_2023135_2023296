package com.AngryHomies.AngryBirds.gameplay;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Box2D world stepped on a fixed clock ("Fix Your Timestep", Glenn Fiedler).
 *
 * <p>Frame time accumulates and physics advances in exact 1/60 s steps, so the simulation is the
 * same at 60, 120 or 144 Hz. The leftover fraction ({@link #alpha()}) lets renderers interpolate
 * between the previous and current step: {@code prev + (cur - prev) * alpha}.
 */
public final class PhysicsWorld implements Disposable {
    public static final float STEP = 1f / 60f;
    private static final int VELOCITY_ITERATIONS = 8;
    private static final int POSITION_ITERATIONS = 3;
    private static final int MAX_STEPS_PER_FRAME = 5;

    /** Hook run around every fixed step (snapshot transforms before, resolve damage after). */
    public interface StepListener {
        void beforeStep();

        void afterStep();
    }

    private final World world;
    private final Array<Body> destroyQueue = new Array<>();
    private float accumulator;
    private float alpha;

    public PhysicsWorld() {
        world = new World(new Vector2(0f, WorldConfig.GRAVITY), true);
    }

    public World world() {
        return world;
    }

    /**
     * Advances the simulation by {@code delta} seconds of game time.
     *
     * @return the number of fixed steps taken (0 when the frame was shorter than one step)
     */
    public int update(float delta, StepListener listener) {
        accumulator += Math.max(0f, delta);
        int steps = 0;
        while (accumulator >= STEP && steps < MAX_STEPS_PER_FRAME) {
            listener.beforeStep();
            world.step(STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
            listener.afterStep();
            flushDestroyed();
            accumulator -= STEP;
            steps++;
        }
        if (steps == MAX_STEPS_PER_FRAME) {
            accumulator = Math.min(accumulator, STEP); // drop backlog instead of spiralling
        }
        alpha = accumulator / STEP;
        return steps;
    }

    public float alpha() {
        return alpha;
    }

    /** Bodies can't be destroyed while Box2D is stepping; queue them for after the step. */
    public void destroyLater(Body body) {
        if (body != null && !destroyQueue.contains(body, true)) {
            destroyQueue.add(body);
        }
    }

    private void flushDestroyed() {
        for (Body body : destroyQueue) {
            world.destroyBody(body);
        }
        destroyQueue.clear();
    }

    /** True when every dynamic body has (almost) stopped moving. */
    public boolean isSettled(float linearTolerance, float angularTolerance) {
        Array<Body> bodies = new Array<>();
        world.getBodies(bodies);
        for (Body body : bodies) {
            if (body.getType() != com.badlogic.gdx.physics.box2d.BodyDef.BodyType.DynamicBody || !body.isAwake()) {
                continue;
            }
            if (body.getLinearVelocity().len2() > linearTolerance * linearTolerance
                || Math.abs(body.getAngularVelocity()) > angularTolerance) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void dispose() {
        world.dispose();
    }
}
