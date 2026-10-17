package com.AngryHomies.AngryBirds.entity;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;

/** Creates Box2D bodies so entity classes don't repeat fixture boilerplate. */
public final class BodyFactory {
    private BodyFactory() {
    }

    public static Body circle(World world, BodyDef.BodyType type, float x, float y, float radius,
                              float density, float friction, float restitution) {
        CircleShape shape = new CircleShape();
        shape.setRadius(radius);
        return create(world, type, x, y, 0f, shape, density, friction, restitution);
    }

    public static Body box(World world, float x, float y, float angle, float width, float height,
                           float density, float friction, float restitution) {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2f, height / 2f);
        return create(world, BodyDef.BodyType.DynamicBody, x, y, angle, shape, density, friction, restitution);
    }

    public static Body ground(World world, float y, float fromX, float toX) {
        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.StaticBody;
        Body body = world.createBody(def);
        EdgeShape edge = new EdgeShape();
        edge.set(fromX, y, toX, y);
        FixtureDef fixture = new FixtureDef();
        fixture.shape = edge;
        fixture.friction = 0.9f;
        body.createFixture(fixture);
        edge.dispose();
        return body;
    }

    private static Body create(World world, BodyDef.BodyType type, float x, float y, float angle, Shape shape,
                               float density, float friction, float restitution) {
        BodyDef def = new BodyDef();
        def.type = type;
        def.position.set(x, y);
        def.angle = angle;
        Body body = world.createBody(def);
        FixtureDef fixture = new FixtureDef();
        fixture.shape = shape;
        fixture.density = density;
        fixture.friction = friction;
        fixture.restitution = restitution;
        body.createFixture(fixture);
        shape.dispose();
        return body;
    }
}
