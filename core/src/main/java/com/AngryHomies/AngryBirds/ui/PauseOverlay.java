package com.AngryHomies.AngryBirds.ui;

import com.AngryHomies.AngryBirds.save.Settings;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

/**
 * Pause menu over a dimmed, frozen level. The game simply stops stepping physics while it is
 * open (the old version zeroed every body's velocity and restored shared vectors, which could
 * resume with wrong motion). Volume and aim-line options apply live.
 */
public final class PauseOverlay {
    /** What the pause buttons do; implemented by the gameplay screen. */
    public interface Listener {
        void onResume();

        void onRestart();

        void onSave();

        void onLevelSelect();

        void onHome();

        void onSettingsChanged();
    }

    private final Group root = new Group();
    private final Window window;
    private boolean open;

    public PauseOverlay(Stage stage, Skin skin, Texture pixel, Settings settings, Listener listener) {
        Image dim = new Image(pixel);
        dim.setColor(0f, 0f, 0f, 0.6f);
        dim.setFillParent(true);
        root.addActor(dim);

        window = new Window("  Paused  ", skin);
        window.getTitleLabel().setAlignment(Align.center);
        window.setMovable(false);
        window.pad(70f, 50f, 40f, 50f);
        window.defaults().pad(8f).width(420f).height(70f);
        window.add(button(skin, "Resume", listener::onResume)).row();
        window.add(button(skin, "Restart", listener::onRestart)).row();
        window.add(button(skin, "Save Game", listener::onSave)).row();
        window.add(button(skin, "Level Select", listener::onLevelSelect)).row();
        window.add(button(skin, "Home", listener::onHome)).row();

        Table options = new Table();
        options.defaults().pad(4f);
        options.add(new Label("Music", skin)).left();
        options.add(slider(skin, settings.musicVolume(), v -> {
            settings.setMusicVolume(v);
            listener.onSettingsChanged();
        })).width(240f).row();
        options.add(new Label("Effects", skin)).left();
        options.add(slider(skin, settings.sfxVolume(), v -> {
            settings.setSfxVolume(v);
            listener.onSettingsChanged();
        })).width(240f).row();
        CheckBox aim = new CheckBox(" Aim line", skin);
        aim.setChecked(settings.showTrajectory());
        aim.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                settings.setShowTrajectory(aim.isChecked());
            }
        });
        options.add(aim).colspan(2).left();
        window.add(options).height(Math.max(160f, options.getPrefHeight())).padTop(18f);
        window.pack();
        root.addActor(window);

        root.setVisible(false);
        root.setTouchable(Touchable.disabled);
        stage.addActor(root);
    }

    public void show(Stage stage) {
        open = true;
        root.toFront();
        root.setSize(stage.getWidth(), stage.getHeight());
        root.setVisible(true);
        root.setTouchable(Touchable.enabled);
        root.getColor().a = 0f;
        root.clearActions();
        root.addAction(Actions.fadeIn(0.2f));
        window.setPosition((stage.getWidth() - window.getWidth()) / 2f, (stage.getHeight() - window.getHeight()) / 2f);
        window.setOrigin(Align.center);
        window.setTransform(true);
        window.setScale(0.85f);
        window.addAction(Actions.scaleTo(1f, 1f, 0.3f, Interpolation.swingOut));
    }

    public void hide() {
        open = false;
        root.setTouchable(Touchable.disabled);
        root.clearActions();
        root.addAction(Actions.sequence(Actions.fadeOut(0.15f), Actions.visible(false)));
    }

    public boolean isOpen() {
        return open;
    }

    private static TextButton button(Skin skin, String text, Runnable action) {
        TextButton button = new TextButton(text, skin);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                action.run();
            }
        });
        return button;
    }

    private interface FloatSink {
        void accept(float value);
    }

    private static Slider slider(Skin skin, float value, FloatSink sink) {
        Slider slider = new Slider(0f, 1f, 0.05f, false, skin, "default-horizontal");
        slider.setValue(value);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                sink.accept(slider.getValue());
            }
        });
        return slider;
    }
}
