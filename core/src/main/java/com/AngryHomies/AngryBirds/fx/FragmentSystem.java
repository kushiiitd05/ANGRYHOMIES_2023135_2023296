package com.AngryHomies.AngryBirds.fx;

import com.AngryHomies.AngryBirds.entity.Block;
import com.AngryHomies.AngryBirds.gameplay.PhysicsWorld;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;

import java.util.Random;

/**
 * Breaks a destroyed block into real physics pieces cut by {@link ShatterPattern}: glass into
 * radial shards from the hit, wood into uneven splinters, stone into chunky wedges. Every piece
 * is a convex Box2D polygon textured with exactly its part of the block (a {@link PolygonRegion}),
 * keeps the block's momentum plus a kick away from the hit, tumbles, piles up, then fades.
 */
public final class FragmentSystem {
    private static final int MAX_FRAGMENTS = 140;
    private static final float FADE_SECONDS = 0.6f;
    /** Pieces smaller than this become dust particles instead (and keep Box2D away from slivers). */
    private static final float MIN_PIECE_AREA = 0.35f;
    private static final float MIN_EDGE = 0.3f;
    private static final float MIN_THICKNESS = 0.25f;
    private static final short[] TRIANGLE = {0, 1, 2};
    private static final short[] QUAD = {0, 1, 2, 0, 2, 3};

    private final PhysicsWorld physics;
    private final Array<Fragment> fragments = new Array<>();
    private final Random random = new Random();

    public FragmentSystem(PhysicsWorld physics) {
        this.physics = physics;
    }

    /** @param hitX,hitY world point where the block was struck (shards radiate from here) */
    public void shatter(Block block, float power, float hitX, float hitY) {
        Body source = block.body();
        float angle = source.getAngle();
        float cos = MathUtils.cos(angle);
        float sin = MathUtils.sin(angle);
        Vector2 center = new Vector2(source.getPosition());
        // impact point in the block's own (unrotated) frame
        float dx = hitX - center.x;
        float dy = hitY - center.y;
        float localHitX = dx * cos + dy * sin;
        float localHitY = -dx * sin + dy * cos;
        Vector2 velocity = new Vector2(source.getLinearVelocity());
        float w = block.width();
        float h = block.height();

        for (float[] piece : ShatterPattern.pieces(block.material(), w, h, localHitX, localHitY, random)) {
            if (ShatterPattern.area(piece) < MIN_PIECE_AREA || !wellShaped(piece)) {
                continue;
            }
            float[] centroid = centroid(piece);
            float worldX = center.x + centroid[0] * cos - centroid[1] * sin;
            float worldY = center.y + centroid[0] * sin + centroid[1] * cos;
            Body body = createBody(piece, centroid, worldX, worldY, angle, block.density(), block.material().friction());
            if (body == null) {
                continue;
            }
            Vector2 kick = new Vector2(centroid[0] - localHitX, centroid[1] - localHitY).rotateRad(angle).nor()
                .scl(MathUtils.random(3f, 10f) * (0.5f + power));
            body.setLinearVelocity(velocity.x + kick.x, velocity.y + kick.y + MathUtils.random(1f, 5f));
            body.setAngularVelocity(MathUtils.random(-7f, 7f));
            add(new Fragment(body, region(block.region(), piece, w, h), centroid[0] + w / 2f, centroid[1] + h / 2f,
                w, h, MathUtils.random(1.8f, 2.8f)));
        }
    }

    private Body createBody(float[] piece, float[] centroid, float x, float y, float angle, float density, float friction) {
        float[] local = new float[piece.length];
        for (int i = 0; i < piece.length; i += 2) {
            local[i] = piece[i] - centroid[0];
            local[i + 1] = piece[i + 1] - centroid[1];
        }
        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.position.set(x, y);
        def.angle = angle;
        Body body = physics.world().createBody(def);
        PolygonShape shape = new PolygonShape();
        shape.set(local);
        FixtureDef fixture = new FixtureDef();
        fixture.shape = shape;
        fixture.density = density;
        fixture.friction = friction;
        fixture.restitution = 0.15f;
        body.createFixture(fixture);
        shape.dispose();
        return body;
    }

    /** The piece's outline mapped onto the block texture (region pixels, origin bottom-left). */
    private static PolygonRegion region(TextureRegion source, float[] piece, float w, float h) {
        float[] uv = new float[piece.length];
        for (int i = 0; i < piece.length; i += 2) {
            uv[i] = (piece[i] / w + 0.5f) * source.getRegionWidth();
            uv[i + 1] = (piece[i + 1] / h + 0.5f) * source.getRegionHeight();
        }
        return new PolygonRegion(source, uv, piece.length == 6 ? TRIANGLE : QUAD);
    }

    /** Every edge long enough and the piece thick enough that Box2D builds a proper hull. */
    static boolean wellShaped(float[] piece) {
        float longest = 0f;
        for (int i = 0; i < piece.length; i += 2) {
            int j = (i + 2) % piece.length;
            float edge = (float) Math.hypot(piece[j] - piece[i], piece[j + 1] - piece[i + 1]);
            if (edge < MIN_EDGE) {
                return false;
            }
            longest = Math.max(longest, edge);
        }
        return 2f * ShatterPattern.area(piece) / longest >= MIN_THICKNESS;
    }

    private static float[] centroid(float[] piece) {
        float x = 0f;
        float y = 0f;
        int n = piece.length / 2;
        for (int i = 0; i < piece.length; i += 2) {
            x += piece[i];
            y += piece[i + 1];
        }
        return new float[] {x / n, y / n};
    }

    private void add(Fragment fragment) {
        if (fragments.size >= MAX_FRAGMENTS) {
            physics.destroyLater(fragments.removeIndex(0).body);
        }
        fragments.add(fragment);
    }

    public void update(float delta) {
        for (int i = fragments.size - 1; i >= 0; i--) {
            Fragment f = fragments.get(i);
            f.life -= delta;
            if (f.life <= 0f || f.body.getPosition().y < -20f) {
                physics.destroyLater(f.body);
                fragments.removeIndex(i);
            }
        }
    }

    public void render(PolygonSpriteBatch batch) {
        Color old = batch.getColor().cpy();
        for (Fragment f : fragments) {
            batch.setColor(1f, 1f, 1f, Math.min(1f, f.life / FADE_SECONDS));
            Vector2 p = f.body.getPosition();
            batch.draw(f.region, p.x - f.originX, p.y - f.originY, f.originX, f.originY, f.width, f.height, 1f, 1f,
                f.body.getAngle() * MathUtils.radiansToDegrees);
        }
        batch.setColor(old);
    }

    public int count() {
        return fragments.size;
    }

    private static final class Fragment {
        final Body body;
        final PolygonRegion region;
        /** The piece's centroid inside the full block region, which is where the body sits. */
        final float originX;
        final float originY;
        final float width;
        final float height;
        float life;

        Fragment(Body body, PolygonRegion region, float originX, float originY, float width, float height, float life) {
            this.body = body;
            this.region = region;
            this.originX = originX;
            this.originY = originY;
            this.width = width;
            this.height = height;
            this.life = life;
        }
    }
}
