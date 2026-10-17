package com.AngryHomies.AngryBirds.gameplay;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * Predicts a launched bird's path with exactly the integration Box2D uses for a free body
 * (semi-implicit Euler: {@code v += g*dt; p += v*dt} at the world's fixed step). Because launch
 * sets a velocity, the dotted aim line lands on the real flight path, unlike the old preview
 * which used a different formula from the actual launch.
 */
public final class TrajectoryPredictor {
    /** Stops the path where it would first hit something (ground, block, pig). */
    public interface Blocker {
        boolean blocked(Vector2 from, Vector2 to);
    }

    private TrajectoryPredictor() {
    }

    /**
     * Fills {@code out} with the position after each fixed step, until {@code maxSteps}, the
     * ground, or a blocker.
     *
     * @return number of points written
     */
    public static int predict(Vector2 start, Vector2 velocity, float gravityY, float step, int maxSteps,
                              float floorY, Blocker blocker, Array<Vector2> out) {
        out.clear();
        float px = start.x;
        float py = start.y;
        float vx = velocity.x;
        float vy = velocity.y;
        Vector2 from = new Vector2();
        Vector2 to = new Vector2();
        for (int i = 0; i < maxSteps; i++) {
            vy += gravityY * step;
            from.set(px, py);
            px += vx * step;
            py += vy * step;
            to.set(px, py);
            if (py < floorY || (blocker != null && blocker.blocked(from, to))) {
                break;
            }
            out.add(new Vector2(px, py));
        }
        return out.size;
    }
}
