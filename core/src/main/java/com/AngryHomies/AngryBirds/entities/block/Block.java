package com.AngryHomies.AngryBirds.entities.block;

import com.AngryHomies.AngryBirds.Serializables.SerializableBlock;
import com.AngryHomies.AngryBirds.Serializables.SerializableBody;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Block {
    protected TextureRegion textureRegion;
    protected Texture texture;
    protected float x, y, width, height;
    protected World world;
    protected float density;
    protected Body body;
    protected float PIXELS_TO_METERS = 10f;
    protected float health;
    protected String id;
    private boolean isDestroyed = false;
    private boolean onGround = false;

    public Block(Texture texture, String id, float x, float y, float width, float height, float density, float hth, World world) {
        this.textureRegion = new TextureRegion(texture);
        this.id = id;
        this.texture = texture;
        this.x = x;
        this.y = y;
        this.width = width;
        this.health = hth;
        this.height = height;
        this.density = density;
        this.world = world;
        createBody();
    }

    public Block(Texture texture, SerializableBlock serializableBlock, World world) {
        this.textureRegion = new TextureRegion(texture);
        this.texture = texture;
        this.x = serializableBlock.getX();
        this.y = serializableBlock.getY();
        this.width = serializableBlock.getWidth();
        this.health = serializableBlock.getHealth();
        this.world = world;
        this.height = serializableBlock.getHeight();
        this.density = serializableBlock.getDensity();
        this.isDestroyed = serializableBlock.isDestroyed();
        this.onGround = serializableBlock.isOnGround();
        createBody(serializableBlock.getBodyState());
    }

    public Block(){

    }

    private void createBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);
        this.body = world.createBody(bodyDef);
        PolygonShape rectangle = new PolygonShape();
        rectangle.setAsBox(width / 2, height / 2);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = rectangle;
        fixtureDef.density = density;
        fixtureDef.friction = 2.8f;
        fixtureDef.restitution = 0.3f;
        this.body.createFixture(fixtureDef);
        rectangle.dispose();
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
        PolygonShape rectangle = new PolygonShape();
        rectangle.setAsBox(width / 2, height / 2);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = rectangle;
        fixtureDef.density = density;
        fixtureDef.friction = 2.8f;
        fixtureDef.restitution = 0.3f;
        this.body.createFixture(fixtureDef);
        rectangle.dispose();
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

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getRotation() {
        return (float) Math.toDegrees(body.getAngle());
    }

    public TextureRegion getTextureRegion() {
        return textureRegion;
    }

    public void takeDamage(float damage) {
        if (!isDestroyed) {
            health -= damage;
            if (health <= 0) {
                isDestroyed = true;
            }
        }
    }

    public String getId() {
        return id;
    }

    public float getDensity() {
        return density;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(int i) {
        this.health = i;
    }

    public void setDefeated(boolean b) {
        this.isDestroyed = b;
    }
}

