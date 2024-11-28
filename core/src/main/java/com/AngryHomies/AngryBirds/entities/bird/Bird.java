package com.AngryHomies.AngryBirds.entities.bird;

import com.AngryHomies.AngryBirds.Serializables.SerializableBird;
import com.AngryHomies.AngryBirds.Serializables.SerializableBody;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public abstract class Bird {

    protected String id;
    protected TextureRegion textureRegion;
    protected Texture texture;
    protected float x, y, width, height;
    protected World world;
    protected float density;
    protected float diameter;
    protected Body body;
    protected boolean isLaunched = false;
    protected boolean abilityUsed = false;
    public float launchTime = 0f;
    protected float attackPower;

    public Bird(Texture texture, String id, float x, float y, float width, float height, float density, float diameter, float atk, World world) {
        this.textureRegion = new TextureRegion(texture);
        this.texture = texture;
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.world = world;
        this.attackPower = atk;
        this.abilityUsed = false;
        this.density = density;
        this.diameter = diameter;
        createBody();
    }

    public Bird(Texture texture, SerializableBird serializableBird, World world) {
        this.textureRegion = new TextureRegion(texture);
        this.texture = texture;
        this.x = serializableBird.getX();
        this.y = serializableBird.getY();
        this.width = serializableBird.getWidth();
        this.world = world;
        this.height = serializableBird.getHeight();
        this.density = serializableBird.getDensity();
        this.diameter = serializableBird.getDiameter();
        this.attackPower = serializableBird.getAttackPower();
        createBody(serializableBird.getBodyState());
    }

    public Bird() {

    }
    public abstract void onSpecialAbility();
    public boolean isAbilityUsed() {
    return abilityUsed;
    }
    public void setLaunched(boolean launched) {
        isLaunched = launched;
    }

    private void createBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);
        this.body = world.createBody(bodyDef);
        CircleShape circle = new CircleShape();
        circle.setRadius(diameter / 1.5f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = density;
        fixtureDef.friction = 3.89f;
        fixtureDef.restitution = 0.4f;
        this.body.createFixture(fixtureDef);
        this.body.setAngularDamping(4.5f);
        this.body.setGravityScale(100.5f);
        this.body.setFixedRotation(true);
        circle.dispose();
    }

    public String getId() {
        return id;
    }

    private void createBody(SerializableBody bodyState) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(bodyState.position);
        bodyDef.angle = bodyState.angle;
        bodyDef.linearVelocity.set(bodyState.linearVelocity);
        bodyDef.angularVelocity = bodyState.angularVelocity;
        bodyDef.allowSleep = bodyState.allowSleep;
        bodyDef.awake = bodyState.isAwake;
        bodyDef.fixedRotation = bodyState.fixedRotation;
        this.body = world.createBody(bodyDef);
        CircleShape circle = new CircleShape();
        circle.setRadius(diameter / 1.5f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = density;
        fixtureDef.friction = 3.89f;
        fixtureDef.restitution = 0.4f;
        this.body.createFixture(fixtureDef);
        circle.dispose();
    }

    public void setPosition(float x, float y) {
        body.setTransform(x, y, body.getAngle());
    }

    public float getAttackPower() {
        return attackPower;
    }

    public float getDiameter() {
        return diameter;
    }

    public float getDensity() {
        return density;
    }

    public void setAttackPower(float attackPower) {
        this.attackPower = attackPower;
    }

    public Texture getTexture() {
        return texture;
    }

    public float getX() {
        return x;
    }

    public Body getBody() {
        return body;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public boolean isLaunched() {
        return isLaunched;
    }

    public TextureRegion getTextureRegion() {
        return textureRegion;
    }
    public float getLaunchTime() {
        return launchTime;
    }

    public void resetLaunchTime() {
        launchTime = 0f;
    }
}

