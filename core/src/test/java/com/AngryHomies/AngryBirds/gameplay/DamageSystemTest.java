package com.AngryHomies.AngryBirds.gameplay;

import com.AngryHomies.AngryBirds.entity.BodyFactory;
import com.AngryHomies.AngryBirds.entity.GameEntity;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DamageSystemTest {
    @BeforeAll
    static void loadNatives() {
        Box2D.init();
    }

    private static GameEntity entity(Body body) {
        return new GameEntity(body, new TextureRegion(), 4f, 4f, 100f) {
            @Override
            public float toughness() {
                return 1f;
            }
        };
    }

    private static World world(DamageSystem damage) {
        World world = new World(new Vector2(0f, WorldConfig.GRAVITY), true);
        world.setContactListener(damage);
        BodyFactory.ground(world, 0f, -100f, 100f).setUserData(DamageSystem.GROUND);
        return world;
    }

    private static Array<DamageSystem.Impact> simulate(World world, DamageSystem damage, float seconds) {
        Array<DamageSystem.Impact> impacts = new Array<>();
        for (int i = 0; i < Math.round(seconds / PhysicsWorld.STEP); i++) {
            world.step(PhysicsWorld.STEP, 8, 3);
            damage.drain(impacts);
        }
        return impacts;
    }

    @Test
    void aRestingStackRecordsNoImpacts() {
        DamageSystem damage = new DamageSystem();
        World world = world(damage);
        entity(BodyFactory.box(world, 0f, 2f, 0f, 4f, 4f, 2f, 0.7f, 0.1f));
        entity(BodyFactory.box(world, 0f, 6f, 0f, 4f, 4f, 2f, 0.7f, 0.1f));
        entity(BodyFactory.box(world, 0f, 10f, 0f, 4f, 4f, 2f, 0.7f, 0.1f));
        assertEquals(0, simulate(world, damage, 2f).size, "stacked blocks must not wear each other down");
        world.dispose();
    }

    @Test
    void aFallRecordsTheImpactAgainstTheRightEntity() {
        DamageSystem damage = new DamageSystem();
        World world = world(damage);
        GameEntity falling = entity(BodyFactory.box(world, 0f, 22f, 0f, 4f, 4f, 2f, 0.7f, 0.1f));
        Array<DamageSystem.Impact> impacts = simulate(world, damage, 2f);
        assertTrue(impacts.size >= 1);
        DamageSystem.Impact first = impacts.first();
        assertSame(falling, first.a != null ? first.a : first.b);
        float expected = (float) Math.sqrt(2 * Math.abs(WorldConfig.GRAVITY) * 20f);
        assertEquals(expected, first.speed, expected * 0.1f);
        world.dispose();
    }

    @Test
    void disabledSystemIgnoresImpacts() {
        DamageSystem damage = new DamageSystem();
        damage.setEnabled(false);
        World world = world(damage);
        entity(BodyFactory.box(world, 0f, 22f, 0f, 4f, 4f, 2f, 0.7f, 0.1f));
        assertEquals(0, simulate(world, damage, 2f).size);
        world.dispose();
    }
}
