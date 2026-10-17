package com.AngryHomies.AngryBirds.fx;

import com.AngryHomies.AngryBirds.entity.Material;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;

import java.util.Random;

/**
 * Cuts a w x h rectangle (centred on 0,0) into convex pieces the way each material breaks.
 *
 * <ul>
 *   <li><b>Glass</b>: many thin shards radiating from the impact point (a "fan" of triangles
 *       from the hit to random points round the edge, like a struck window).</li>
 *   <li><b>Stone</b>: the same fan with only a few points, so it cracks into chunky wedges.</li>
 *   <li><b>Wood</b>: long pieces split along the grain at random cut points, so planks splinter
 *       into uneven lengths; square wood blocks fan into a few wedges.</li>
 * </ul>
 *
 * Pieces are returned as vertex lists (x0,y0,x1,y1,...) in the rectangle's local space and always
 * tile it exactly. Pure maths with an injectable random source, so it is unit-testable.
 */
public final class ShatterPattern {
    private ShatterPattern() {
    }

    public static Array<float[]> pieces(Material material, float w, float h, float hitX, float hitY, Random random) {
        float cx = MathUtils.clamp(hitX, -w * 0.35f, w * 0.35f);
        float cy = MathUtils.clamp(hitY, -h * 0.35f, h * 0.35f);
        float aspect = Math.max(w, h) / Math.min(w, h);
        switch (material) {
            case GLASS:
                return fan(w, h, cx, cy, 9 + random.nextInt(5), random);
            case STONE:
                return aspect > 2.5f ? strips(w, h, 2 + random.nextInt(2), random) : fan(w, h, cx, cy, 2 + random.nextInt(3), random);
            default:
                return aspect > 1.6f ? strips(w, h, 2 + random.nextInt(3), random) : fan(w, h, cx, cy, 2 + random.nextInt(2), random);
        }
    }

    /** Triangles from (cx,cy) to consecutive points on the border (corners always included). */
    static Array<float[]> fan(float w, float h, float cx, float cy, int extraPoints, Random random) {
        float perimeter = 2f * (w + h);
        FloatArray params = new FloatArray();
        params.addAll(0f, w, w + h, 2f * w + h); // the four corners
        for (int i = 0; i < extraPoints; i++) {
            params.add(random.nextFloat() * perimeter);
        }
        params.sort();
        Array<float[]> pieces = new Array<>();
        for (int i = 0; i < params.size; i++) {
            float a = params.get(i);
            float b = i + 1 < params.size ? params.get(i + 1) : perimeter;
            if (b - a < 1e-3f) {
                continue;
            }
            float[] p = borderPoint(a, w, h);
            float[] q = borderPoint(b, w, h);
            // a wedge may turn a corner; include that corner so the pieces still tile the rectangle
            float[] corner = cornerBetween(a, b, w, h);
            pieces.add(corner == null
                ? new float[] {cx, cy, p[0], p[1], q[0], q[1]}
                : new float[] {cx, cy, p[0], p[1], corner[0], corner[1], q[0], q[1]});
        }
        return pieces;
    }

    /** Cuts across the long axis at random positions: uneven splinters along the grain. */
    static Array<float[]> strips(float w, float h, int cuts, Random random) {
        boolean wide = w >= h;
        float length = wide ? w : h;
        FloatArray positions = new FloatArray();
        positions.add(-length / 2f);
        for (int i = 0; i < cuts; i++) {
            positions.add(-length / 2f + length * (0.12f + 0.76f * random.nextFloat()));
        }
        positions.add(length / 2f);
        positions.sort();
        Array<float[]> pieces = new Array<>();
        for (int i = 0; i + 1 < positions.size; i++) {
            float a = positions.get(i);
            float b = positions.get(i + 1);
            if (b - a < length * 0.06f) {
                continue; // too thin to be a believable splinter
            }
            pieces.add(wide
                ? new float[] {a, -h / 2f, b, -h / 2f, b, h / 2f, a, h / 2f}
                : new float[] {-w / 2f, a, w / 2f, a, w / 2f, b, -w / 2f, b});
        }
        return pieces;
    }

    /** Walks the border counter-clockwise from the bottom-left corner. */
    static float[] borderPoint(float t, float w, float h) {
        float x0 = -w / 2f;
        float y0 = -h / 2f;
        if (t <= w) {
            return new float[] {x0 + t, y0};
        }
        if (t <= w + h) {
            return new float[] {w / 2f, y0 + (t - w)};
        }
        if (t <= 2f * w + h) {
            return new float[] {w / 2f - (t - w - h), h / 2f};
        }
        return new float[] {x0, h / 2f - (t - 2f * w - h)};
    }

    private static float[] cornerBetween(float a, float b, float w, float h) {
        float[] corners = {w, w + h, 2f * w + h};
        for (float c : corners) {
            if (a < c - 1e-4f && b > c + 1e-4f) {
                return borderPoint(c, w, h);
            }
        }
        return null;
    }

    /** Shoelace area of a polygon given as x,y pairs. */
    public static float area(float[] vertices) {
        float sum = 0f;
        for (int i = 0; i < vertices.length; i += 2) {
            int j = (i + 2) % vertices.length;
            sum += vertices[i] * vertices[j + 1] - vertices[j] * vertices[i + 1];
        }
        return Math.abs(sum) / 2f;
    }
}
