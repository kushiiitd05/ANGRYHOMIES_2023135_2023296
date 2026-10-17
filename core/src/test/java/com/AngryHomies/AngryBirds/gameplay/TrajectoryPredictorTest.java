package com.AngryHomies.AngryBirds.gameplay;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrajectoryPredictorTest {
    @BeforeAll
    static void loadNatives() {
        Box2D.init();
    }

    @Test
    void predictedPathMatchesRealBox2dFlight() {
        Vector2 start = new Vector2(28.4f, 32.2f);
        Vector2 velocity = new Vector2(55f, 40f);
        Array<Vector2> predicted = new Array<>();
        TrajectoryPredictor.predict(start, velocity, WorldConfig.GRAVITY, PhysicsWorld.STEP, 120, -1000f, null, predicted);

        World world = new World(new Vector2(0f, WorldConfig.GRAVITY), true);
        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.position.set(start);
        Body body = world.createBody(def);
        CircleShape shape = new CircleShape();
        shape.setRadius(2.67f);
        body.createFixture(shape, 3.9f);
        shape.dispose();
        body.setLinearVelocity(velocity);

        for (int i = 0; i < predicted.size; i++) {
            world.step(PhysicsWorld.STEP, 8, 3);
            assertEquals(predicted.get(i).x, body.getPosition().x, 1e-3f, "x at step " + i);
            assertEquals(predicted.get(i).y, body.getPosition().y, 1e-3f, "y at step " + i);
        }
        world.dispose();
    }

    @Test
    void stopsAtTheFloor() {
        Array<Vector2> out = new Array<>();
        TrajectoryPredictor.predict(new Vector2(0f, 20f), new Vector2(10f, 0f), -30f, PhysicsWorld.STEP, 1000, 15.5f, null, out);
        assertTrue(out.size > 0 && out.size < 1000);
        assertTrue(out.peek().y >= 15.5f);
    }

    @Test
    void stopsAtABlocker() {
        Array<Vector2> out = new Array<>();
        TrajectoryPredictor.predict(new Vector2(0f, 50f), new Vector2(30f, 0f), 0f, PhysicsWorld.STEP, 1000, -100f,
            (from, to) -> to.x > 10f, out);
        assertTrue(out.peek().x <= 10f);
    }
}
