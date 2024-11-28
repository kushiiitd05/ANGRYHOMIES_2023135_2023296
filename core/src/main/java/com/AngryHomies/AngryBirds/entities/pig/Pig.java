package com.AngryHomies.AngryBirds.entities.pig;

import com.AngryHomies.AngryBirds.Serializables.SerializableBody;
import com.AngryHomies.AngryBirds.Serializables.SerializablePig;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.*;

public abstract class Pig {

    protected TextureRegion textureRegion;
    protected Texture texture;
    protected float x, y, width, height;
    protected World world;
    protected float density;
    protected float diameter;
    protected Body body;
    protected String id;
    protected float health;
    private boolean isDefeated = false;

    public Pig(Texture texture, String id, float x, float y, float width, float height, float density, float diameter, float hth, World world) {
        this.textureRegion = new TextureRegion(texture);
        this.texture = texture;
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.world = world;
        this.health = hth;
        this.density = density;
        this.diameter = diameter;
        createBody();
    }

    public Pig(){
    }

    public Pig(Texture texture, SerializablePig serializablePig, World world) {
        this.textureRegion = new TextureRegion(texture);
        this.texture = texture;
        this.x = serializablePig.getX();
        this.y = serializablePig.getY();
        this.width = serializablePig.getWidth();
        this.health = serializablePig.getHealth();
        this.world = world;
        this.height = serializablePig.getHeight();
        this.density = serializablePig.getDensity();
        this.diameter = serializablePig.getDiameter();
        createBody(serializablePig.getBodyState());
    }

    private void createBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);
        this.body = world.createBody(bodyDef);
        CircleShape circle = new CircleShape();
        circle.setRadius(diameter / 2);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = density;
        fixtureDef.friction = 0.8f;
        fixtureDef.restitution = 0.2f;
        this.body.createFixture(fixtureDef);
        this.body.setAngularDamping(1.5f);
        circle.dispose();
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
        circle.setRadius(diameter / 2);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = density;
        fixtureDef.friction = 3.8f;
        fixtureDef.restitution = 0.4f;
        this.body.createFixture(fixtureDef);
        circle.dispose();
    }

    public Body getBody() {
        return body;
    }

    public Texture getTexture() {
        return texture;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getDensity() {
        return density;
    }

    public float getDiameter() {
        return diameter;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public TextureRegion getTextureRegion() {
        return textureRegion;
    }

    public void takeDamage(float damage) {
        if (!isDefeated) {
            health -= damage;
            if (health <= 0) {
                isDefeated = true;
            }
        }
    }

    public void setHealth(int health){
        this.health = health;
    }

    public void setDefeated(boolean defeated){
        this.isDefeated = defeated;
    }

    public float getHealth() {
        return health;
    }

    public boolean isDefeated() {
        return isDefeated;
    }

    public String getId() {
        return id;
    }
}

