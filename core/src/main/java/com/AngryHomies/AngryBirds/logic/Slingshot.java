package com.AngryHomies.AngryBirds.logic;

import com.AngryHomies.AngryBirds.Texture.TextureFactory;
import com.AngryHomies.AngryBirds.entities.bird.Bird;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;

import java.util.ArrayList;
import java.util.List;

public class Slingshot {
    private Texture slingshot_texture;
    private int x_pos;
    private int y_pos;
    private int width;
    private int height;

    private Body anchorBody;
    private MouseJoint mouseJoint;
    private static final float MAX_DRAG_RADIUS = 10f;
    private static final float MAX_LAUNCH_POWER = 4500f;
    private static final float PIXELS_TO_METERS = 10f;
    private static final float slingposbird_x = (260 + 20) / 10f;
    private static final float slingposbird_y = (1079 - 780) / 10f;


    public List<Vector2> getTrajectoryDots() {
        return trajectoryDots;
    }

    private List<Vector2> trajectoryDots;
    private Texture dotTexture;

    public Slingshot(Texture slingshot_texture, int x_pos, int y_pos, int width, int height, TextureFactory textureFactory) {
        this.slingshot_texture = slingshot_texture;
        this.x_pos = x_pos;
        this.y_pos = y_pos;
        this.width = width;
        this.height = height;
        trajectoryDots = new ArrayList<>();
        dotTexture = textureFactory.createSlingShotTexture();
    }

    public Texture getSlingshot_texture() {
        return slingshot_texture;
    }

    public int getx_pos() {
        return x_pos;
    }

    public int gety_pos() {
        return y_pos;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean touchDown(int screenX, int screenY, Bird currentBird, World world) {
        if (currentBird == null || currentBird.isLaunched()) return false;
        Vector2 touchPos = new Vector2(screenX / PIXELS_TO_METERS, (Gdx.graphics.getHeight() - screenY) / PIXELS_TO_METERS);

        if (touchPos.dst(currentBird.getBody().getPosition()) <= MAX_DRAG_RADIUS) {
            if (mouseJoint == null) {
                if (anchorBody == null) {
                    BodyDef anchorBodyDef = new BodyDef();
                    anchorBodyDef.type = BodyDef.BodyType.StaticBody;
                    anchorBodyDef.position.set(slingposbird_x, slingposbird_y);
                    anchorBody = world.createBody(anchorBodyDef);
                }

                MouseJointDef mouseJointDef = new MouseJointDef();
                mouseJointDef.bodyA = anchorBody;
                mouseJointDef.bodyB = currentBird.getBody();
                mouseJointDef.target.set(currentBird.getBody().getPosition());
                mouseJointDef.maxForce = 1000f * currentBird.getBody().getMass();
                mouseJointDef.dampingRatio = 0.5f;
                mouseJointDef.frequencyHz = 5.0f;

                mouseJoint = (MouseJoint) world.createJoint(mouseJointDef);
            }
            return true;
        }
        return false;
    }

    public boolean touchDragged(int screenX, int screenY) {
        if (mouseJoint != null) {
            Vector2 touchPos = new Vector2(screenX / PIXELS_TO_METERS, (Gdx.graphics.getHeight() - screenY) / PIXELS_TO_METERS);
            Vector2 anchorPos = anchorBody.getPosition();

            if (touchPos.dst(anchorPos) > MAX_DRAG_RADIUS) {
                touchPos.sub(anchorPos).setLength(MAX_DRAG_RADIUS).add(anchorPos);
            }

            mouseJoint.setTarget(touchPos);

            Vector2 launchVelocity = anchorPos.sub(touchPos).scl(5f);
            calculateTrajectory(touchPos, launchVelocity, -8f, 0.1f, 100);
            return true;
        }
        return false;
    }

    public void calculateTrajectory(Vector2 initialPosition, Vector2 initialVelocity, float gravity, float timeStep, int maxSteps) {
        trajectoryDots.clear();
        for (int i = 0; i < maxSteps; i++) {
            float time = i * timeStep;
            float x = initialPosition.x + initialVelocity.x * time;
            float y = initialPosition.y + initialVelocity.y * time + 0.5f * gravity * time * time;

            if (y < 0) break;
            trajectoryDots.add(new Vector2(x * PIXELS_TO_METERS, y * PIXELS_TO_METERS));
        }
    }

    public boolean touchUp(Bird currentBird, World world) {
        if (mouseJoint != null) {
            world.destroyJoint(mouseJoint);
            mouseJoint = null;

            if (currentBird != null) {
                currentBird.getBody().setGravityScale(1);
                Vector2 launchDirection = anchorBody.getPosition().sub(currentBird.getBody().getPosition());
                float launchPower = Math.min(launchDirection.len() * 500f, MAX_LAUNCH_POWER);
                launchDirection.setLength(launchPower);

                currentBird.getBody().applyLinearImpulse(launchDirection, currentBird.getBody().getWorldCenter(), true);
                currentBird.setLaunched(true);
                trajectoryDots.clear();
            }
            return true;
        }
        return false;
    }

    public void renderTrajectory(SpriteBatch batch) {
        for (Vector2 dotPosition : trajectoryDots) {
            batch.draw(dotTexture, dotPosition.x - 4, dotPosition.y - 4, 8, 8);
        }
    }

    public Body getAnchorBody() {
        return anchorBody;
    }

    public static float getMaxLaunchPower() {
        return MAX_LAUNCH_POWER;
    }
}

