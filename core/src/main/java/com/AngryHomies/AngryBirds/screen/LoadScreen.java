package com.AngryHomies.AngryBirds.screen;

import com.AngryHomies.AngryBirds.Main;
import com.AngryHomies.AngryBirds.assets.Assets;
import com.AngryHomies.AngryBirds.audio.Sfx;
import com.AngryHomies.AngryBirds.level.LevelDefinition;
import com.AngryHomies.AngryBirds.save.SaveData;
import com.AngryHomies.AngryBirds.screen.transition.FadeTransition;
import com.AngryHomies.AngryBirds.screen.transition.SlideTransition;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

import java.text.SimpleDateFormat;
import java.util.Date;

/** One save slot per level: resume where you left off, or clear the slot. */
public final class LoadScreen extends BaseScreen {
    private final Texture background;

    public LoadScreen(Main game) {
        super(game);
        background = game.assets().get(Assets.LOAD_BG);
        Table layout = new Table();
        layout.setFillParent(true);
        layout.add(new Label("Load Game", game.skin(), "title")).colspan(3).padBottom(50f).row();
        for (int id = 1; id <= LevelDefinition.LEVEL_COUNT; id++) {
            Table slot = slot(id);
            layout.add(slot).pad(36f).top();
            slot.setTransform(true);
            slot.setOrigin(Align.center);
            slot.getColor().a = 0f;
            slot.addAction(Actions.sequence(Actions.delay(0.1f * id), Actions.parallel(Actions.fadeIn(0.3f),
                Actions.sequence(Actions.moveBy(0f, -60f), Actions.moveBy(0f, 60f, 0.4f, Interpolation.swingOut)))));
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
                if (keycode == Input.Keys.ESCAPE) {
                    back();
                    return true;
                }
                return false;
            }
        });
    }

    private Table slot(int id) {
        SaveData save = game.saves().load(id);
        ImageButton thumb = new ImageButton(new TextureRegionDrawable(game.assets().get(Assets.levelThumb(id))));
        thumb.getImageCell().size(170f);
        Table slot = new Table();
        slot.add(thumb).row();
        slot.add(new Label("Level " + id, game.skin(), "title")).row();
        if (save == null) {
            thumb.setColor(0.4f, 0.4f, 0.4f, 1f);
            slot.add(new Label("Empty slot", game.skin())).row();
            return slot;
        }
        String when = new SimpleDateFormat("d MMM, HH:mm").format(new Date(save.savedAt));
        slot.add(new Label("Saved " + when, game.skin())).row();
        slot.add(new Label("Score " + save.score + "   Birds " + save.birds.size, game.skin())).row();
        juicyButton(thumb, () -> resume(id, save));
        TextButton delete = juicyButton(new TextButton("Delete", game.skin()), () -> {
            game.saves().delete(id);
            game.screens().set(new LoadScreen(game), null);
        });
        slot.add(delete).width(200f).height(60f).padTop(10f);
        return slot;
    }

    private void resume(int id, SaveData save) {
        if (!game.screens().isTransitioning()) {
            game.screens().set(new GameplayScreen(game, GameplayScreen.loadLevel(id), save), new FadeTransition());
        }
    }

    private void back() {
        if (!game.screens().isTransitioning()) {
            game.audio().play(Sfx.WHOOSH);
            game.screens().set(new HomeScreen(game), new SlideTransition(SlideTransition.Direction.RIGHT));
        }
    }

    @Override
    protected void drawBackground(Batch batch, float delta) {
        drawCover(batch, background, 1.02f);
    }
}
