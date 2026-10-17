package com.AngryHomies.AngryBirds.gameplay;

import com.AngryHomies.AngryBirds.entity.GameEntity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.WorldManifold;
import com.badlogic.gdx.utils.Array;

/**
 * Box2D contact listener that turns real impacts into damage.
 *
 * <p>In {@code preSolve} (before the solver separates the bodies) it measures the closing speed
 * along the contact normal and records an {@link Impact}. Nothing is changed during the step,
 * since Box2D locks the world inside callbacks; {@link #drain} hands the impacts over afterwards.
 * Impacts are tracked per entity object, which fixes the old bug of removing "the first wood
 * block" whenever any wood block broke.
 */
public final class DamageSystem implements ContactListener {
    /** User data for the ground body. */
    public static final Object GROUND = new Object();

    private final Array<Impact> pending = new Array<>();
    private final Vector2 velocityA = new Vector2();
    private final Vector2 velocityB = new Vector2();
    private boolean enabled = true;

    /** One measured collision between two bodies (an entity may be null for ground). */
    public static final class Impact {
        public final GameEntity a;
        public final GameEntity b;
        public final float massA;
        public final float massB;
        public final float speed;
        public final float x;
        public final float y;

        Impact(GameEntity a, GameEntity b, float massA, float massB, float speed, float x, float y) {
            this.a = a;
            this.b = b;
            this.massA = massA;
            this.massB = massB;
            this.speed = speed;
            this.x = x;
            this.y = y;
        }

        public float energy() {
            return DamageModel.impactEnergy(massA, massB, speed);
        }
    }

    /** Disable during the opening settle so pieces nudging into place don't take damage. */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        if (!enabled) {
            return;
        }
        Body bodyA = contact.getFixtureA().getBody();
        Body bodyB = contact.getFixtureB().getBody();
        GameEntity a = entity(bodyA);
        GameEntity b = entity(bodyB);
        boolean groundA = bodyA.getUserData() == GROUND;
        boolean groundB = bodyB.getUserData() == GROUND;
        if ((a == null && !groundA) || (b == null && !groundB) || (a == null && b == null)) {
            return; // fragments and other decoration never cause damage
        }
        WorldManifold manifold = contact.getWorldManifold();
        if (manifold.getNumberOfContactPoints() == 0) {
            return;
        }
        Vector2 point = manifold.getPoints()[0];
        Vector2 normal = manifold.getNormal();
        velocityA.set(bodyA.getLinearVelocityFromWorldPoint(point));
        velocityB.set(bodyB.getLinearVelocityFromWorldPoint(point));
        float closing = velocityA.sub(velocityB).dot(normal); // normal points from A to B
        if (closing >= DamageModel.MIN_IMPACT_SPEED) {
            pending.add(new Impact(a, b, bodyA.getMass(), bodyB.getMass(), closing, point.x, point.y));
        }
    }

    /** Moves all impacts measured since the last call into {@code out}. */
    public void drain(Array<Impact> out) {
        out.addAll(pending);
        pending.clear();
    }

    private static GameEntity entity(Body body) {
        Object data = body.getUserData();
        return data instanceof GameEntity ? (GameEntity) data : null;
    }

    @Override
    public void beginContact(Contact contact) {
    }

    @Override
    public void endContact(Contact contact) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }
}
