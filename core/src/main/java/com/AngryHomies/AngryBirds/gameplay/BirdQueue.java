package com.AngryHomies.AngryBirds.gameplay;

import com.AngryHomies.AngryBirds.entity.BirdType;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Birds waiting on the ground beside the slingshot. They hop now and then while idle; when it is
 * their turn the front bird jumps in an arc onto the pouch and the rest shuffle forward.
 * These are drawings only; a physics body exists once the bird lands on the slingshot.
 */
public final class BirdQueue {
    private static final float SLOT_X = 20.5f;
    private static final float SLOT_GAP = 6.3f;
    private static final float SCALE = 0.85f;
    private static final float HOP_SECONDS = 0.5f;

    private final Array<Waiting> waiting = new Array<>();
    private Waiting hopping;
    private float hopTime;
    private BirdType landed;

    private static final class Waiting {
        final BirdType type;
        float x;
        float idleTimer = MathUtils.random(0.5f, 2.5f);
        float hopPhase = -1f;

        Waiting(BirdType type, float x) {
            this.type = type;
            this.x = x;
        }
    }

    public BirdQueue(Array<BirdType> types) {
        for (int i = 0; i < types.size; i++) {
            waiting.add(new Waiting(types.get(i), slotX(i)));
        }
    }

    /** Starts the next bird's jump to the slingshot; false if none are left. */
    public boolean sendNext() {
        if (waiting.isEmpty() || hopping != null) {
            return false;
        }
        hopping = waiting.removeIndex(0);
        hopTime = 0f;
        return true;
    }

    /** The bird that just finished its jump onto the pouch, once; otherwise null. */
    public BirdType pollLanded() {
        BirdType type = landed;
        landed = null;
        return type;
    }

    public void update(float delta) {
        for (int i = 0; i < waiting.size; i++) {
            Waiting bird = waiting.get(i);
            bird.x += (slotX(i) - bird.x) * (1f - (float) Math.exp(-6f * delta));
            bird.idleTimer -= delta;
            if (bird.idleTimer <= 0f && bird.hopPhase < 0f) {
                bird.hopPhase = 0f;
                bird.idleTimer = MathUtils.random(1.2f, 3.2f);
            }
            if (bird.hopPhase >= 0f) {
                bird.hopPhase += delta * 3.5f;
                if (bird.hopPhase >= 1f) {
                    bird.hopPhase = -1f;
                }
            }
        }
        if (hopping != null) {
            hopTime += delta;
            if (hopTime >= HOP_SECONDS) {
                landed = hopping.type;
                hopping = null;
            }
        }
    }

    public void render(SpriteBatch batch, ObjectMap<BirdType, TextureRegion> regions) {
        for (Waiting bird : waiting) {
            float w = bird.type.width() * SCALE;
            float h = bird.type.height() * SCALE;
            float hop = bird.hopPhase >= 0f ? MathUtils.sin(bird.hopPhase * MathUtils.PI) * 2.4f : 0f;
            float squash = bird.hopPhase >= 0f && bird.hopPhase > 0.85f ? 0.9f : 1f;
            float y = WorldConfig.GROUND_Y + h / 2f + hop;
            batch.draw(regions.get(bird.type), bird.x - w / 2f, y - h / 2f, w / 2f, h / 2f, w, h,
                1f / squash, squash, 0f);
        }
        if (hopping != null) {
            float t = Interpolation.pow2Out.apply(Math.min(1f, hopTime / HOP_SECONDS));
            float startY = WorldConfig.GROUND_Y + hopping.type.height() * SCALE / 2f;
            float x = MathUtils.lerp(hopping.x, SlingshotController.REST.x, t);
            float y = MathUtils.lerp(startY, SlingshotController.REST.y, t) + MathUtils.sin(t * MathUtils.PI) * 10f;
            float w = hopping.type.width();
            float h = hopping.type.height();
            batch.draw(regions.get(hopping.type), x - w / 2f, y - h / 2f, w / 2f, h / 2f, w, h, 1f, 1f, -360f * t);
        }
    }

    /** Birds still to launch (waiting and mid-jump), in order. */
    public Array<BirdType> remainingTypes() {
        Array<BirdType> types = new Array<>();
        if (hopping != null) {
            types.add(hopping.type);
        }
        for (Waiting bird : waiting) {
            types.add(bird.type);
        }
        return types;
    }

    public int remaining() {
        return waiting.size + (hopping != null ? 1 : 0) + (landed != null ? 1 : 0);
    }

    public boolean isHopping() {
        return hopping != null;
    }

    /** Ground positions of the waiting birds, for the end-of-level bonus popups. */
    public Array<float[]> positions() {
        Array<float[]> out = new Array<>();
        for (Waiting bird : waiting) {
            out.add(new float[] {bird.x, WorldConfig.GROUND_Y + bird.type.height() * SCALE});
        }
        return out;
    }

    private static float slotX(int index) {
        return SLOT_X - index * SLOT_GAP;
    }
}
