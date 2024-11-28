
package com.AngryHomies.AngryBirds.Serializables;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.MassData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SerializableBody implements Serializable {
    private static final long serialVersionUID = 1L;
    public Vector2 position;
    public Vector2 linearVelocity;
    public float angularVelocity;
    public float angle;
    public boolean isActive;
    public BodyDef.BodyType bodyType;
    public float gravityScale;
    public float linearDamping;
    public float angularDamping;
    public boolean allowSleep;
    public boolean isAwake;
    public boolean fixedRotation;
    public float mass;
    public Vector2 centerOfMass = new Vector2();
    public float inertia;

    public SerializableBody(Body body) {
        this.position = body.getPosition().cpy();
        this.linearVelocity = body.getLinearVelocity().cpy();
        this.angularVelocity = body.getAngularVelocity();
        this.angle = body.getAngle();
        this.isActive = body.isActive();
        this.bodyType = body.getType();
        this.gravityScale = body.getGravityScale();
        this.linearDamping = body.getLinearDamping();
        this.angularDamping = body.getAngularDamping();
        this.allowSleep = body.isSleepingAllowed();
        this.isAwake = body.isAwake();
        this.fixedRotation = body.isFixedRotation();
        MassData massData = body.getMassData();
        this.mass = massData.mass;
        this.centerOfMass.set(massData.center);
        this.inertia = massData.I;

    }
}
