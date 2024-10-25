package com.AngryHomies.AngryBirds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.HashMap;

public class LevelOne extends Level {

    public LevelOne(Main game, HashMap<String, Screen> levelScreens, HashMap<String, String> savedScreens) {
        super(game, levelScreens, savedScreens);
        this.currentLevel = "LevelOne";
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        initializeLevel();
    }

    @Override
    protected void initializeLevel() {

        groundTexture = game.getAssetManager().get("images/ground.png", Texture.class);
        catapultTexture = game.getAssetManager().get("images/catapult.png", Texture.class);
        slingshot = new Slingshot(catapultTexture, 260 - 125, 1079 - 925, 300, 200);


        birds = new Bird[]{
            new RedBird(game.getAssetManager().get("images/red_bird.png", Texture.class), 260, 1079 - 820, 75, 75),
            new BlackBird(game.getAssetManager().get("images/black_bird.png", Texture.class), 180, 1079 - 925, 95, 95),
            new YellowBird(game.getAssetManager().get("images/yellow_bird.png", Texture.class), 100, 1079 - 925, 85, 85)
        };

        pigs = new Pig[]{
            new SmallPig(game.getAssetManager().get("images/small_pig.png", Texture.class), 1516 - 70, 1079 - 925 + 38 + 235 + 26 + 25 + 187 + 18, 160, 110),
            new BigPig(game.getAssetManager().get("images/big_pig.png", Texture.class), 1516 - 70, 1079 - 925 + 38 + 235 + 26 + 25, 125, 100),
            new KingPig(game.getAssetManager().get("images/king_pig.png", Texture.class), 1516 - 70, 1079 - 869, 145, 155)
        };

        blocks = new Block[]{
            new WoodBlock(new Texture(Gdx.files.internal("images/block.png")), 1377, 1079 - 925 + 38, 313, 25),
            new WoodBlock(new Texture(Gdx.files.internal("images/block.png")), 1377, 1079 - 925, 40, 40),
            new GlassBlock(new Texture(Gdx.files.internal("images/glass_block.png")), 1377, 1079 - 925 + 38 + 22, 40, 250),
            new GlassBlock(new Texture(Gdx.files.internal("images/glass_block.png")), 1650, 1079 - 925 + 38 + 22, 40, 250),
            new GlassBlock(new Texture(Gdx.files.internal("images/glass_block.png")), 1377, 1079 - 925 + 38 + 235 + 26 + 25, 40, 190),
            new GlassBlock(new Texture(Gdx.files.internal("images/glass_block.png")), 1650, 1079 - 925 + 38 + 235 + 26 + 25, 40, 190),
            new WoodBlock(new Texture(Gdx.files.internal("images/block.png")), 1377, 1079 - 925 + 38 + 235 + 26 + 25 + 187, 313, 25),
            new StoneBlock(new Texture(Gdx.files.internal("images/stone_block.png")), 1355 - 38, 1079 - 925 + 38 + 235 + 26, 460, 35),
            new StoneBlock(new Texture(Gdx.files.internal("images/stone_block.png")), 1377, 1079 - 925 + 38 + 235 + 26 + 25 + 187 + 18, 40, 40),
            new StoneBlock(new Texture(Gdx.files.internal("images/stone_block.png")), 1650, 1079 - 925 + 38 + 235 + 26 + 25 + 187 + 18, 40, 40),
            new WoodBlock(new Texture(Gdx.files.internal("images/block.png")), 1650, 1079 - 928, 40, 40)
        };


        createWinLoseButtons();
    }

    private void createWinLoseButtons() {
        winButton = new TextButton("Won", skin);
        loseButton = new TextButton("Lost", skin);


        stage.addActor(winButton);
        stage.addActor(loseButton);


        winButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!levelScreens.containsKey("WinScreen")) {
                    levelScreens.put("WinScreen", new WinScreen(game, levelScreens));
                }
                game.setScreen(levelScreens.get("WinScreen"));
            }
        });

        loseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!levelScreens.containsKey("LoseScreen")) {
                    levelScreens.put("LoseScreen", new LoseScreen(game, levelScreens));
                }
                game.setScreen(levelScreens.get("LoseScreen"));
            }
        });
    }

    @Override
    protected void renderGame(float delta) {

        batch.draw(groundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.draw(slingshot.getSlingshot_texture(), slingshot.getx_pos(), slingshot.gety_pos(), slingshot.getWidth(), slingshot.getHeight());

        winButton.setPosition(1750, 950);
        loseButton.setPosition(1575, 950);


        for (Bird bird : birds) {
            batch.draw(bird.getTexture(), bird.getX(), bird.getY(), bird.getWidth(), bird.getHeight());
        }


        for (Pig pig : pigs) {
            batch.draw(pig.getTexture(), pig.getX(), pig.getY(), pig.getWidth(), pig.getHeight());
        }


        for (Block block : blocks) {
            batch.draw(block.getTexture(), block.getX(), block.getY(), block.getWidth(), block.getHeight());
        }


        stage.act(delta);
        stage.draw();
    }

    @Override
    public void show() {

        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void dispose() {

        stage.dispose();

    }
}











