package com.AngryHomies.AngryBirds.screen;

import com.AngryHomies.AngryBirds.Main;
import com.AngryHomies.AngryBirds.assets.Assets;
import com.AngryHomies.AngryBirds.audio.Sfx;
import com.AngryHomies.AngryBirds.screen.transition.SlideTransition;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;

/**
 * Main menu: a slowly drifting backdrop, the logo dropping in with a bounce, and buttons sliding
 * in one after another. Mouse hover and arrow keys both highlight a button.
 */
public final class HomeScreen extends BaseScreen {
    private final Texture background;
    private final Array<TextButton> buttons = new Array<>();
    private final Array<Runnable> actions = new Array<>();
    private int selected = -1;

    public HomeScreen(Main game) {
        super(game);
        background = game.assets().get(Assets.HOME_BG);

        Image logo = new Image(game.assets().get(Assets.LOGO));
        logo.setScaling(Scaling.fit);
        Table layout = new Table();
        layout.setFillParent(true);
        layout.add(logo).size(760f, 475f).padBottom(10f).row();

        addButton(layout, "Play", () -> go(new LevelSelectScreen(game), new SlideTransition(SlideTransition.Direction.LEFT)));
        addButton(layout, "Load Game", () -> go(new LoadScreen(game), new SlideTransition(SlideTransition.Direction.LEFT)));
        addButton(layout, "Settings", () -> go(new SettingsScreen(game), new SlideTransition(SlideTransition.Direction.LEFT)));
        addButton(layout, "Quit", Gdx.app::exit);
        stage.addActor(layout);

        layout.validate();
        logo.setOrigin(Align.center);
        logo.addAction(Actions.sequence(Actions.moveBy(0f, 500f), Actions.moveBy(0f, -500f, 0.9f, Interpolation.bounceOut)));
        for (int i = 0; i < buttons.size; i++) {
            TextButton button = buttons.get(i);
            button.getColor().a = 0f;
            button.addAction(Actions.sequence(Actions.moveBy(700f, 0f), Actions.delay(0.35f + i * 0.09f),
                Actions.parallel(Actions.fadeIn(0.3f), Actions.moveBy(-700f, 0f, 0.45f, Interpolation.pow3Out))));
        }
        input.addProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                return handleKey(keycode);
            }
        });
    }

    private void addButton(Table layout, String text, Runnable action) {
        TextButton button = juicyButton(new TextButton(text, game.skin()), action);
        buttons.add(button);
        actions.add(action);
        layout.add(button).width(420f).height(78f).pad(8f).row();
    }

    private boolean handleKey(int keycode) {
        if (keycode == Input.Keys.DOWN || keycode == Input.Keys.UP) {
            int step = keycode == Input.Keys.DOWN ? 1 : -1;
            selected = (selected + step + buttons.size) % buttons.size;
            for (int i = 0; i < buttons.size; i++) {
                buttons.get(i).setColor(i == selected ? 1f : 1f, 1f, i == selected ? 0.4f : 1f, 1f);
            }
            game.audio().play(Sfx.UI_HOVER);
            return true;
        }
        if (keycode == Input.Keys.ENTER && selected >= 0) {
            game.audio().play(Sfx.UI_CLICK);
            actions.get(selected).run();
            return true;
        }
        return false;
    }

    private void go(BaseScreen screen, com.AngryHomies.AngryBirds.screen.transition.ScreenTransition transition) {
        if (!game.screens().isTransitioning()) {
            game.audio().play(Sfx.WHOOSH);
            game.screens().set(screen, transition);
        }
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        if (com.AngryHomies.AngryBirds.Demo.enabled() && time > 1.6f && !game.screens().isTransitioning()) {
            go(new LevelSelectScreen(game), new SlideTransition(SlideTransition.Direction.LEFT));
        }
    }

    @Override
    public void show() {
        game.audio().playMusic(Assets.MENU_MUSIC, 0.8f);
    }

    @Override
    protected void drawBackground(Batch batch, float delta) {
        drawCover(batch, background, 1.05f + 0.04f * MathUtils.sin(time * 0.25f));
    }
}
