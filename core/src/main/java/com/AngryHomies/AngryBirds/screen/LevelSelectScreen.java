package com.AngryHomies.AngryBirds.screen;

import com.AngryHomies.AngryBirds.Main;
import com.AngryHomies.AngryBirds.assets.Assets;
import com.AngryHomies.AngryBirds.audio.Sfx;
import com.AngryHomies.AngryBirds.level.LevelDefinition;
import com.AngryHomies.AngryBirds.screen.transition.FadeTransition;
import com.AngryHomies.AngryBirds.screen.transition.SlideTransition;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

/** Level cards with stars and best score; locked levels are greyed out until the previous is won. */
public final class LevelSelectScreen extends BaseScreen {
    private final Texture background;

    public LevelSelectScreen(Main game) {
        super(game);
        background = game.assets().get(Assets.LEVEL_SELECT_BG);

        Table layout = new Table();
        layout.setFillParent(true);
        layout.add(new Image(game.assets().get(Assets.LEVEL_SELECT_TITLE))).size(804f, 166f).colspan(3).padBottom(60f).row();
        for (int id = 1; id <= LevelDefinition.LEVEL_COUNT; id++) {
            Table card = card(id);
            layout.add(card).pad(40f);
            card.setTransform(true);
            card.setOrigin(Align.center);
            card.setScale(0f);
            card.addAction(Actions.sequence(Actions.delay(0.15f + id * 0.12f),
                Actions.scaleTo(1f, 1f, 0.45f, Interpolation.swingOut)));
        }
        stage.addActor(layout);

        ImageButton back = juicyButton(new ImageButton(new TextureRegionDrawable(game.assets().get(Assets.BACK_BUTTON))),
            this::back);
        back.getImageCell().size(130f, 112f);
        Table corner = new Table();
        corner.setFillParent(true);
        corner.bottom().left().pad(30f);
        corner.add(back);
        stage.addActor(corner);

        input.addProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACKSPACE) {
                    back();
                    return true;
                }
                return false;
            }
        });
    }

    private Table card(int id) {
        boolean unlocked = game.progress().isUnlocked(id);
        ImageButton thumb = new ImageButton(new TextureRegionDrawable(game.assets().get(Assets.levelThumb(id))));
        thumb.getImageCell().size(190f);
        Table card = new Table();
        card.add(thumb).row();
        card.add(new Label("Level " + id, game.skin(), "title")).padTop(6f).row();
        Table stars = new Table();
        int earned = game.progress().stars(id);
        for (int i = 0; i < 3; i++) {
            Texture star = i < earned ? game.textures().star() : game.textures().starEmpty();
            stars.add(new Image(star)).size(56f).pad(2f);
        }
        card.add(stars).row();
        int best = game.progress().bestScore(id);
        card.add(new Label(unlocked ? (best > 0 ? "Best " + best : "Not played") : "Locked", game.skin())).row();
        if (unlocked) {
            juicyButton(thumb, () -> play(id));
        } else {
            thumb.setColor(0.35f, 0.35f, 0.35f, 1f);
            thumb.setDisabled(true);
        }
        return card;
    }

    private void play(int id) {
        if (!game.screens().isTransitioning()) {
            game.screens().set(new GameplayScreen(game, GameplayScreen.loadLevel(id), null), new FadeTransition());
        }
    }

    private void back() {
        if (!game.screens().isTransitioning()) {
            game.audio().play(Sfx.WHOOSH);
            game.screens().set(new HomeScreen(game), new SlideTransition(SlideTransition.Direction.RIGHT));
        }
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        if (com.AngryHomies.AngryBirds.Demo.enabled() && time > 1.4f) {
            play(Integer.parseInt(System.getenv().getOrDefault("ANGRYHOMIES_DEMO_LEVEL", "1")));
        }
    }

    @Override
    public void show() {
        game.audio().playMusic(Assets.MENU_MUSIC, 0.8f);
    }

    @Override
    protected void drawBackground(Batch batch, float delta) {
        drawCover(batch, background, 1.02f);
    }
}
