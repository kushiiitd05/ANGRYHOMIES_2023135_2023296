package com.AngryHomies.AngryBirds.screen;

import com.AngryHomies.AngryBirds.Main;
import com.AngryHomies.AngryBirds.assets.Assets;
import com.AngryHomies.AngryBirds.audio.Sfx;
import com.AngryHomies.AngryBirds.save.Settings;
import com.AngryHomies.AngryBirds.screen.transition.SlideTransition;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

/** Volumes, aim line, fullscreen and progress reset. Every change applies and saves at once. */
public final class SettingsScreen extends BaseScreen {
    private final Texture background;
    private boolean confirmReset;

    public SettingsScreen(Main game) {
        super(game);
        background = game.assets().get(Assets.LOAD_BG);
        Settings settings = game.progress().settings();

        Window window = new Window("  Settings  ", game.skin());
        window.getTitleLabel().setAlignment(Align.center);
        window.setMovable(false);
        window.pad(80f, 50f, 40f, 50f);
        window.defaults().pad(10f);
        addSlider(window, "Master", settings.masterVolume(), settings::setMasterVolume);
        addSlider(window, "Music", settings.musicVolume(), settings::setMusicVolume);
        addSlider(window, "Effects", settings.sfxVolume(), v -> {
            settings.setSfxVolume(v);
            game.audio().play(Sfx.UI_HOVER);
        });
        CheckBox aim = check(" Show aim line", settings.showTrajectory(), settings::setShowTrajectory);
        window.add(aim).colspan(2).left().row();
        CheckBox fullscreen = check(" Fullscreen (F11)", settings.isFullscreen(), value -> {
            settings.setFullscreen(value);
            game.applyDisplayMode();
        });
        window.add(fullscreen).colspan(2).left().row();
        TextButton reset = new TextButton("Reset progress", game.skin());
        juicyButton(reset, () -> {
            if (confirmReset) {
                game.progress().reset();
                reset.setText("Progress reset");
                confirmReset = false;
            } else {
                reset.setText("Click again to confirm");
                confirmReset = true;
            }
        });
        window.add(reset).colspan(2).width(440f).height(70f).padTop(20f).row();
        window.add(juicyButton(new TextButton("Back", game.skin()), this::back)).colspan(2).width(440f).height(70f);
        window.pack();

        Table layout = new Table();
        layout.setFillParent(true);
        layout.add(window);
        stage.addActor(layout);
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

    private interface FloatSink {
        void accept(float value);
    }

    private interface BoolSink {
        void accept(boolean value);
    }

    private void addSlider(Window window, String name, float value, FloatSink sink) {
        Slider slider = new Slider(0f, 1f, 0.05f, false, game.skin(), "default-horizontal");
        slider.setValue(value);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                sink.accept(slider.getValue());
            }
        });
        window.add(new Label(name, game.skin())).left();
        window.add(slider).width(380f).row();
    }

    private CheckBox check(String text, boolean value, BoolSink sink) {
        CheckBox box = new CheckBox(text, game.skin());
        box.setChecked(value);
        box.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.audio().play(Sfx.UI_CLICK);
                sink.accept(box.isChecked());
            }
        });
        return box;
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
