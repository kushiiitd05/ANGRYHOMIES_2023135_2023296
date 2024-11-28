package com.AngryHomies.AngryBirds.logic.level.levels;

import com.AngryHomies.AngryBirds.Serializables.LevelState;
import com.AngryHomies.AngryBirds.Serializables.SerializableBird;
import com.AngryHomies.AngryBirds.Serializables.SerializableBlock;
import com.AngryHomies.AngryBirds.Serializables.SerializablePig;
import com.AngryHomies.AngryBirds.Texture.DotTextureFactory;
import com.AngryHomies.AngryBirds.assetManager.Main;
import com.AngryHomies.AngryBirds.entities.pig.BigPig;
import com.AngryHomies.AngryBirds.entities.bird.Bird;
import com.AngryHomies.AngryBirds.entities.block.Block;
import com.AngryHomies.AngryBirds.entities.pig.KingPig;
import com.AngryHomies.AngryBirds.entities.pig.Pig;
import com.AngryHomies.AngryBirds.entities.bird.BlackBird;
import com.AngryHomies.AngryBirds.entities.bird.RedBird;
import com.AngryHomies.AngryBirds.entities.bird.YellowBird;
import com.AngryHomies.AngryBirds.entities.block.GlassBlock;
import com.AngryHomies.AngryBirds.entities.block.StoneBlock;
import com.AngryHomies.AngryBirds.entities.block.WoodBlock;
import com.AngryHomies.AngryBirds.entities.pig.SmallPig;
import com.AngryHomies.AngryBirds.logic.Slingshot;
import com.AngryHomies.AngryBirds.logic.level.Level;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.ArrayList;
import java.util.HashMap;

public class LevelTwo extends Level {

    Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer();
    public float PIXELS_TO_METERS = 10f;
    private static final float slingposbird_x = (260 + 20) / 10f;
    private static final float slingposbird_y = (1079 - 780) / 10f;

    public LevelTwo(Main game, HashMap<String, Screen> levelScreens) {
        super(game, levelScreens, new World(new Vector2(0, -8f), true));
        debugRenderer = new Box2DDebugRenderer();
        this.currentLevel = "LevelTwo";
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        initializeLevel();
        initializeEntities();
    }

    public LevelTwo(Main game, LevelState levelState, HashMap<String, Screen> levelScreens) {
        super(game, levelScreens, new World(new Vector2(0, -8f), true));
        this.currentLevel = "LevelTwo";
        initializeLevel();
        loadEntities(levelState);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                return slingshot.touchDown(screenX, screenY, currentBird, world);
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                return slingshot.touchDragged(screenX, screenY);
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                return slingshot.touchUp(currentBird, world);
            }
        });
        if (!birds.isEmpty()) {
            currentBird = birds.get(0);
            resetBirdPosition(currentBird);
            currentBird.setLaunched(false);
        }
    }

    @Override
    protected void initializeLevel() {
        groundTexture = game.getAssetManager().get("images/ground.png", Texture.class);
        catapultTexture = game.getAssetManager().get("images/catapult.png", Texture.class);
        slingshot = new Slingshot(catapultTexture, 260 - 125, 1079 - 925, 300, 200, new DotTextureFactory());
        createGround();
    }

    private void initializeEntities() {
        birds = new ArrayList<>();
        birds.add(new RedBird(game.getAssetManager().get("images/red_bird.png", Texture.class), RedBird.id, slingposbird_x, slingposbird_y, 75 / 10f, 75 / 10f, 3.9f, (80 / 2) / 10f, 6, world));
        birds.add(new BlackBird(game.getAssetManager().get("images/black_bird.png", Texture.class), BlackBird.id, 180 / 10f, (1079 - 820) / 10f, 95 / 10f, 90 / 10f, 1.7f, (125 / 2) / 10f, 10, world));
        birds.add(new YellowBird(game.getAssetManager().get("images/yellow_bird.png", Texture.class), YellowBird.id, 100 / 10f, (1079 - 820) / 10f, 75 / 10f, 75 / 10f, 2.5f, (100 / 2) / 10f, 7, world));
        birds.add(new RedBird(game.getAssetManager().get("images/red_bird.png", Texture.class), RedBird.id, 20 / 10f, (1079 - 820) / 10f, 75 / 10f, 75 / 10f, 3.9f, (80 / 2) / 10f, 6, world));

        pigs = new ArrayList<>();
        pigs.add(new SmallPig(game.getAssetManager().get("images/small_pig.png", Texture.class), SmallPig.id, 1280 / 10f, (1079 - 925 + 25) / 10f, 160 / 10f, 110 / 10f, 1f, (120 / 2) / 10f, 10, world));
        pigs.add(new SmallPig(game.getAssetManager().get("images/small_pig.png", Texture.class), SmallPig.id, 1600 / 10f, (1079 - 925 + 25) / 10f, 160 / 10f, 110 / 10f, 1f, (120 / 2) / 10f, 10, world));
        pigs.add(new KingPig(game.getAssetManager().get("images/king_pig.png", Texture.class), KingPig.id, (1516 - 70) / 10f, (1079 - 925 + 38 + 235 + 45) / 10f, 135 / 10f, 120 / 10f, 2f, (230 / 2) / 10f, 11, world));
        pigs.add(new BigPig(game.getAssetManager().get("images/big_pig.png", Texture.class), BigPig.id, (1516 - 70) / 10f, (1079 - 925 + 38 + 235 + 100 + 190) / 10f, 125 / 10f, 100 / 10f, 2.5f, (190 / 2) / 10f, 15, world));

        blocks = new ArrayList<>();
        blocks.add(new StoneBlock(new Texture(Gdx.files.internal("images/st_stoneblock.png")), StoneBlock.id, 1175 / 10f, (1079 - 925 + 25) / 10f, 60 / 10f, 250 / 10f, 1f, 12, world));
        blocks.add(new WoodBlock(new Texture(Gdx.files.internal("images/standing_woodblock.png")), WoodBlock.id, 1400 / 10f, (1079 - 925 + 20) / 10f, 40 / 10f, 250 / 10f, 3f, 10, world));
        blocks.add(new WoodBlock(new Texture(Gdx.files.internal("images/standing_woodblock.png")), WoodBlock.id, 1500 / 10f, (1079 - 925 + 20) / 10f, 40 / 10f, 250 / 10f, 3f, 10, world));
        blocks.add(new StoneBlock(new Texture(Gdx.files.internal("images/st_stoneblock.png")), StoneBlock.id, 1700 / 10f, (1079 - 925 + 20) / 10f, 60 / 10f, 250 / 10f, 1f, 12, world));
        blocks.add(new GlassBlock(new Texture(Gdx.files.internal("images/sp_glassblock.png")), GlassBlock.id, 1300 / 10f, (1079 - 925 + 38 + 235 + 16) / 10f, 300 / 10f, 40 / 10f, 1f, 7, world));
        blocks.add(new GlassBlock(new Texture(Gdx.files.internal("images/sp_glassblock.png")), GlassBlock.id, 1590 / 10f, (1079 - 925 + 38 + 235) / 10f, 300 / 10f, 40 / 10f, 1f, 7, world));
        blocks.add(new WoodBlock(new Texture(Gdx.files.internal("images/standing_woodblock.png")), WoodBlock.id, 1245 / 10f, (1079 - 925 + 38 + 235 + 100) / 10f, 40 / 10f, 250 / 10f, 2f, 10, world));
        blocks.add(new GlassBlock(new Texture(Gdx.files.internal("images/glass_block.png")), GlassBlock.id, 1335 / 10f, (1079 - 925 + 38 + 235 + 100) / 10f, 40 / 10f, 250 / 10f, 1f, 7, world));
        blocks.add(new GlassBlock(new Texture(Gdx.files.internal("images/glass_block.png")), GlassBlock.id, 1570 / 10f, (1079 - 925 + 38 + 235 + 100) / 10f, 40 / 10f, 250 / 10f, 1f, 7, world));
        blocks.add(new WoodBlock(new Texture(Gdx.files.internal("images/standing_woodblock.png")), WoodBlock.id, 1640 / 10f, (1079 - 925 + 38 + 235 + 100) / 10f, 40 / 10f, 250 / 10f, 2f, 10, world));
        blocks.add(new GlassBlock(new Texture(Gdx.files.internal("images/sp_glassblock.png")), GlassBlock.id, (1516 - 70) / 10f, (1079 - 925 + 38 + 235 + 100 + 185) / 10f, 500 / 10f, 40 / 10f, 1f, 7, world));

        currentBird = birds.get(0);

        for (Bird bird : birds) {
            bird.getBody().setUserData(bird);
        }
        for (Block block : blocks) {
            block.getBody().setUserData(block);
        }
        for (Pig pig : pigs) {
            pig.getBody().setUserData(pig);
        }
    }

    private void loadEntities(LevelState levelState) {
        birds = new ArrayList<>();
        for (SerializableBird bird : levelState.getBirds()) {
            switch (bird.getId()) {
                case RedBird.id:
                    birds.add(new RedBird(game.getAssetManager().get("images/red_bird.png", Texture.class), bird, world));
                    continue;
                case BlackBird.id:
                    birds.add(new BlackBird(game.getAssetManager().get("images/black_bird.png", Texture.class), bird, world));
                    continue;
                case YellowBird.id:
                    birds.add(new YellowBird(game.getAssetManager().get("images/yellow_bird.png", Texture.class), bird, world));
                    continue;
            }
        }

        pigs = new ArrayList<>();

        for (SerializablePig pig : levelState.getPigs()) {
            switch (pig.getId()) {
                case SmallPig.id:
                    pigs.add(new SmallPig(game.getAssetManager().get("images/small_pig.png", Texture.class), pig, world));
                    continue;
                case BigPig.id:
                    pigs.add(new BigPig(game.getAssetManager().get("images/big_pig.png", Texture.class), pig, world));
                    continue;
                case KingPig.id:
                    pigs.add(new KingPig(game.getAssetManager().get("images/king_pig.png", Texture.class), pig, world));
                    continue;
            }
        }

        blocks = new ArrayList<>();
        for (SerializableBlock block : levelState.getBlocks()) {
            switch (block.getId()) {
                case GlassBlock.id:
                    blocks.add(new GlassBlock(null, block, world));
                    continue;
                case WoodBlock.id:
                    blocks.add(new WoodBlock(null, block, world));
                    continue;
                case StoneBlock.id:
                    blocks.add(new StoneBlock(null, block, world));
                    continue;
            }
        }

        currentBird = birds.get(0);

        for (Bird bird : birds) {
            bird.getBody().setUserData(bird);
        }
        for (Block block : blocks) {
            block.getBody().setUserData(block);
        }
        for (Pig pig : pigs) {
            pig.getBody().setUserData(pig);
        }
    }

    @Override
    protected void renderGame(float delta) {
        if (batch == null) {
            System.err.println("SpriteBatch is null!");
            return;
        }
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.draw(groundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.draw(slingshot.getSlingshot_texture(), slingshot.getx_pos(), slingshot.gety_pos(), slingshot.getWidth(), slingshot.getHeight());
        if (currentBird != null && !currentBird.isLaunched()) {
            Vector2 birdPosition = currentBird.getBody().getPosition();
            Vector2 launchVelocity = calculateLaunchVelocity(birdPosition);

            if (currentBird != null && !currentBird.isLaunched()) {
                slingshot.renderTrajectory(batch);
            }
        }

        for (Bird bird : birds) {
            batch.draw(
                bird.getTextureRegion(),
                bird.getBody().getPosition().x * PIXELS_TO_METERS - bird.getWidth() / 2,
                bird.getBody().getPosition().y * PIXELS_TO_METERS - bird.getHeight() / 2,
                bird.getWidth() / 2,
                bird.getHeight() / 2,
                bird.getWidth(),
                bird.getHeight(),
                10f,
                10f,
                (float) Math.toDegrees(bird.getBody().getAngle())
            );
        }

        for (Pig pig : pigs) {
            batch.draw(
                pig.getTextureRegion(),
                pig.getBody().getPosition().x * PIXELS_TO_METERS - pig.getWidth() / 2,
                pig.getBody().getPosition().y * PIXELS_TO_METERS - pig.getHeight() / 2,
                pig.getWidth() / 2,
                pig.getHeight() / 2,
                pig.getWidth(),
                pig.getHeight(),
                10f,
                10f,
                (float) Math.toDegrees(pig.getBody().getAngle())
            );
        }

        for (Block block : blocks) {
            batch.draw(
                block.getTextureRegion(),
                block.getBody().getPosition().x * PIXELS_TO_METERS - block.getWidth() / 2,
                block.getBody().getPosition().y * PIXELS_TO_METERS - block.getHeight() / 2,
                block.getWidth() / 2,
                block.getHeight() / 2,
                block.getWidth(),
                block.getHeight(),
                10f,
                10f,
                (float) Math.toDegrees(block.getBody().getAngle())
            );
        }

        batch.draw(pauseIconTexture, 20, Gdx.graphics.getHeight() - 150, 135, 135);

//        batch.end();
        checkBirdLaunchState(delta);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {}
}

