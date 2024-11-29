package com.AngryHomies.AngryBirds.logic.level;

import com.AngryHomies.AngryBirds.Serializables.LevelState;
import com.AngryHomies.AngryBirds.Texture.TextureFactory;
import com.AngryHomies.AngryBirds.assetManager.Main;
import com.AngryHomies.AngryBirds.collision.CollisionDetector;
import com.AngryHomies.AngryBirds.entities.bird.Bird;
import com.AngryHomies.AngryBirds.entities.block.Block;
import com.AngryHomies.AngryBirds.entities.pig.Pig;
import com.AngryHomies.AngryBirds.logic.Slingshot;
import com.AngryHomies.AngryBirds.logic.level.levels.LevelOne;
import com.AngryHomies.AngryBirds.logic.level.levels.LevelThree;
import com.AngryHomies.AngryBirds.logic.level.levels.LevelTwo;
import com.AngryHomies.AngryBirds.screens.HomeScreen;
import com.AngryHomies.AngryBirds.screens.LoseScreen;
import com.AngryHomies.AngryBirds.screens.SaveSuccessScreen;
import com.AngryHomies.AngryBirds.screens.WinScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.*;

public abstract class Level implements Screen {
    protected Main game;
    public Stage stage;
    protected Skin skin;
    protected SpriteBatch batch;
    protected Texture pauseIconTexture;

    protected Bird currentBird;

    public boolean isPaused;
    protected HashMap<String, Screen> levelScreens;
    protected HashMap<String, String> savedScreens;
    protected String currentLevel;
    protected MouseJoint mouseJoint;
    protected Slingshot slingshot;
    protected Texture groundTexture, catapultTexture;
    protected List<Bird> birds;
    protected List<Pig> pigs;
    protected List<Block> blocks;
    private List<BodyState> pausedBodies = new ArrayList<>();
    private Array<Body> worldBodies = new Array<>();

    protected World world;
    protected static final float PIXELS_TO_METERS = 10f;
//    private List<Vector2> trajectoryDots;
    protected Viewport viewport;
    protected Box2DDebugRenderer debugRenderer;

    public Level() {

    }

    public static OrthographicCamera getCamera() {
        return camera;
    }

    protected static OrthographicCamera camera;
    protected float slingposbird_x = 28.0f;
    protected float slingposbird_y = 29.9f;
    protected boolean isLevelCompleted = false;
    protected boolean isLevelLost = false;
    private float winScreenDelayTimer = 0f;
    private float loseScreenDelayTimer = 0f;


    protected InputProcessor previousInputProcessor;

    public static String DEFAULT_SAVE_PATH = System.getProperty("user.home") +  "/OneDrive/Desktop/AngryHomies/saves/default_save";

    public Level(Main game,HashMap<String, Screen> levelScreens,World world) {
        this.game = game;
        this.levelScreens = levelScreens != null ? levelScreens : new HashMap<>();
        this.world = world;
        this.world.setContactListener(new CollisionDetector());
        this.debugRenderer = new Box2DDebugRenderer();
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        // Trajectory dots setup
        skin = new Skin(Gdx.files.internal("uiskin/uiskin2/freezing-ui.json"));
        batch = new SpriteBatch();
        isPaused = false;
        initializeCamera();

        pauseIconTexture = game.getAssetManager().get("images/pause_icon.png");
    }
    protected Vector2 calculateLaunchVelocity(Vector2 birdPosition) {
        if (slingshot == null || slingshot.getAnchorBody() == null) return new Vector2(0, 0);

        Vector2 launchDirection = slingshot.getAnchorBody().getPosition().sub(birdPosition);
        float launchPower = Math.min(launchDirection.len() * 500f, Slingshot.getMaxLaunchPower()); // Scale power
        return launchDirection.setLength(launchPower);
    }
    public  void setIsLevelWon(boolean isLevelCompleted) {
        this.isLevelCompleted = isLevelCompleted;
    }
    public boolean  checkWinCondition() {
        if (isLevelCompleted && pigs.isEmpty()){
        return isLevelCompleted;
        }else{
            return false;
        }
    }
    public void setpigs(List <Pig> pigs ){
        this.pigs = pigs;
    }



    protected void checkGameState(float delta) {
        if (pigs.isEmpty() && !isLevelCompleted) {
            isLevelCompleted = true;
        }
        if (isLevelCompleted) {
            winScreenDelayTimer += delta;
            if (winScreenDelayTimer >= 5f) {
                transitionToWinScreen();
            }
            return;
        }
        if (birds.isEmpty() && !pigs.isEmpty() && !isLevelLost) {
            isLevelLost = true;
        }

        if (isLevelLost) {
            loseScreenDelayTimer += delta;
            if (loseScreenDelayTimer >= 2f) {
                transitionToLoseScreen();
            }
            return;
        }
    }
    protected void transitionToWinScreen() {
        isPaused = true;
        game.setScreen(new WinScreen(game,levelScreens));
    }
    protected void transitionToLoseScreen() {
        isPaused = true;
        game.setScreen(new LoseScreen(game,levelScreens));
    }

    private static class BodyState {
        Body body;
        Vector2 linearVelocity;
        float angularVelocity;

        BodyState(Body body, Vector2 linearVelocity, float angularVelocity) {
            this.body = body;
            this.linearVelocity = linearVelocity;
            this.angularVelocity = angularVelocity;
        }
    }
    protected void checkPigsOutOfBounds() {
        for (int i = 0; i < pigs.size(); i++) {
            Pig pig = pigs.get(i);
            Vector2 pigPosition = pig.getBody().getPosition();
            if (pigPosition.x < 0 || pigPosition.x > Gdx.graphics.getWidth()+20 / PIXELS_TO_METERS ||
                pigPosition.y < 0 || pigPosition.y > Gdx.graphics.getHeight()+20 / PIXELS_TO_METERS) {
                System.out.println("Pig went out of bounds and is defeated.");
                world.destroyBody(pig.getBody());
                pigs.remove(i);
                i--;
            }
        }
    }

    protected void checkBirdLaunchState(float delta) {
        if (currentBird != null && currentBird.isLaunched()) {
            currentBird.launchTime += delta;
            Vector2 velocity = currentBird.getBody().getLinearVelocity();
            float speed = velocity.len();
            if (speed < 0.1f || currentBird.launchTime > 6f) {
                int currentIndex = birds.indexOf(currentBird);
                removeBird(currentBird);
                if (currentIndex >= 0 && currentIndex < birds.size()) {
                    currentBird = birds.get(currentIndex);
                    resetBirdPosition(currentBird);
                } else {
                    currentBird = null;
                }
            }
        }
    }
    protected void removeBird(Bird bird) {
        if (bird != null) {
            int indexToRemove = birds.indexOf(bird);
            if (indexToRemove != -1) {
                birds.remove(indexToRemove);
                world.destroyBody(bird.getBody());
                System.out.println("Bird removed: " + bird.getId());
            }
        }
    }
    protected void resetBirdPosition(Bird bird) {
        if (bird != null) {
            bird.getBody().setTransform(slingposbird_x, slingposbird_y, 0);
            bird.getBody().setAngularDamping(1f);
            bird.getBody().setFixedRotation(false);
            bird.getBody().setGravityScale(8f);

            bird.getBody().setLinearVelocity(0, 0);
            bird.getBody().setAngularVelocity(0);
            bird.setLaunched(false);
            bird.resetLaunchTime();
            bird.getBody().setGravityScale(0);
        }
    }

    private void initializeCamera() {
        float cameraWidth = Gdx.graphics.getWidth() / PIXELS_TO_METERS;
        float cameraHeight = Gdx.graphics.getHeight() / PIXELS_TO_METERS;
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(cameraWidth, cameraHeight, camera);
        camera.position.set(cameraWidth / 2f, cameraHeight / 2f, 0);
        camera.update();
    }

    @Override
    public void render(float delta) {
        while (isPaused) {
            Gdx.input.setInputProcessor(stage);
            stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
            stage.draw();
            handleKeyInput();
            return;
        }

        batch.begin();
        if (currentBird != null && currentBird.isLaunched() && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            currentBird.onSpecialAbility();
        }
        world.step(1 / 60f, 6, 2);
        camera.update();


        CollisionDetector.processRemovals(world, blocks, pigs);
        checkPigsOutOfBounds();
        checkGameState(delta);
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        renderGame(delta);

        if (batch == null) {
            System.err.println("SpriteBatch is null!");
            return;
        }
        batch.draw(pauseIconTexture, 20, Gdx.graphics.getHeight() - 150, 135, 135);
        batch.end();

       //debugRenderer.render(world, camera.combined);

        if (Gdx.input.isTouched()) {
            int x = Gdx.input.getX();
            int y = Gdx.graphics.getHeight() - Gdx.input.getY();

            if (x >= 20 && x <= 20 + 135 && y >= Gdx.graphics.getHeight() - 150 && y <= Gdx.graphics.getHeight() - 150 + 135) {
                showPauseMenu();
            }
        }

        stage.act(delta);
        stage.draw();
    }




    protected abstract void initializeLevel();


    protected abstract void renderGame(float delta);

    protected void showPauseMenu() {
        isPaused = true;
        previousInputProcessor = Gdx.input.getInputProcessor();
        Gdx.input.setInputProcessor(stage);
        pausedBodies.clear();
        world.getBodies(worldBodies);
        for (Body body : worldBodies) {
            if (body.getType() == BodyDef.BodyType.DynamicBody) {
                pausedBodies.add(new BodyState(body, body.getLinearVelocity(), body.getAngularVelocity()));
                body.setLinearVelocity(0, 0);
                body.setAngularVelocity(0);
            }
        }

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        TextButton resumeButton = new TextButton("Resume", skin);
        TextButton restartButton = new TextButton("Restart", skin);
        TextButton saveButton = new TextButton("SaveGame", skin);
        TextButton exitButton = new TextButton("Exit to Home", skin);
        table.add(resumeButton).pad(10).row();
        table.add(restartButton).pad(10).row();
        table.add(saveButton).pad(10).row();
        table.add(exitButton).pad(10).row();
        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hidePauseMenu();
            }
        });
        restartButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                restartLevel();
            }
        });
        saveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                saveButton();
            }
        });
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                exitToHomeScreen();
            }
        });
    }
    protected void saveButton() {
        try {
            LevelState levelState = new LevelState(birds, pigs, blocks, currentBird);
            FileOutputStream fileOut = new FileOutputStream(DEFAULT_SAVE_PATH + "_" + currentLevel + ".sav");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(levelState);
            out.close();
            fileOut.close();
            System.out.println("Game state saved");
        } catch (Exception e) {
            e.printStackTrace();
        }
        transitionToSaveSuccessScreen();
    }

    protected void resetMouseJoint() {
        if (mouseJoint != null) {
            world.destroyJoint(mouseJoint);
            mouseJoint = null;
        }
    }

    private void transitionToSaveSuccessScreen() {
        Texture successTexture = game.getAssetManager().get("images/success_screen.png", Texture.class);
        SaveSuccessScreen saveSuccessScreen = new SaveSuccessScreen(game, levelScreens,successTexture, 600, 500,750,300);
        game.setScreen(saveSuccessScreen);
    }

    protected void createGround() {
        BodyDef groundBodyDef = new BodyDef();
        groundBodyDef.position.set(0,0);
        Body groundBody = world.createBody(groundBodyDef);
        groundBody.setUserData("Ground");
        EdgeShape groundShape = new EdgeShape();
        float groundHeightPixels = 155;
        float groundY = groundHeightPixels / 10f;
        groundShape.set(new Vector2(0, groundY), new Vector2(Gdx.graphics.getWidth() / 10f, groundY));
        FixtureDef groundFixtureDef = new FixtureDef();
        groundFixtureDef.shape = groundShape;
        groundFixtureDef.friction = 100f;
        groundBody.createFixture(groundFixtureDef);
        groundShape.dispose();
    }

    protected void hidePauseMenu() {
        isPaused = false;
        stage.clear();
        Gdx.input.setInputProcessor(previousInputProcessor);
        for (BodyState state : pausedBodies) {
            state.body.setLinearVelocity(state.linearVelocity);
            state.body.setAngularVelocity(state.angularVelocity);
        }
        pausedBodies.clear();
    }

    protected void exitToHomeScreen() {
        Screen homeScreen = levelScreens.get("HomeScreen");
        if (homeScreen != null) {
            game.setScreen(homeScreen);
        } else {
            levelScreens.put("HomeScreen", new HomeScreen(game, levelScreens));
            game.setScreen(levelScreens.get("HomeScreen"));
        }
    }
    protected void handleKeyInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            hidePauseMenu();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            restartLevel();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            exitToHomeScreen();
        }
    }

    private void restartLevel() {
        //resetMousejoint();
        if (levelScreens != null) {
            for (Map.Entry<String, Screen> entry : levelScreens.entrySet()) {
                System.out.println(entry.getKey());
            }
            if (levelScreens.containsKey("LevelOne") && Objects.equals(currentLevel, "LevelOne")) {
                Screen newLevelOne = new LevelOne(game, levelScreens);
                levelScreens.put("LevelOne", newLevelOne);
                game.setScreen(newLevelOne);

            } else if (levelScreens.containsKey("LevelTwo") && Objects.equals(currentLevel, "LevelTwo")) {
                Screen newLevelTwo = new LevelTwo(game, levelScreens);
                levelScreens.put("LevelTwo", newLevelTwo);
                game.setScreen(newLevelTwo);

            } else if (levelScreens.containsKey("LevelThree") && Objects.equals(currentLevel, "LevelThree")) {
                Screen newLevelThree = new LevelThree(game, levelScreens);
                levelScreens.put("LevelThree", newLevelThree);
                game.setScreen(newLevelThree);

            } else {
                System.out.println("Level not found");
                return;
            }
        }
    }
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}
    @Override
    public void dispose() {

        if (stage != null) {
            stage.dispose();
            stage = null;
        }
        if (batch != null) {
            batch.dispose();
            batch = null;
        }
        if (debugRenderer != null) {
            debugRenderer.dispose();
        }
        if (world != null) {
            world.dispose();
        }

    }

}

