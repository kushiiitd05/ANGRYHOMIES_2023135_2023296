package com.AngryHomies.AngryBirds.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

/**
 * In-game overlay: pause button, rolling score, best score, level title and contextual hints.
 * The pause button is a real scene2d button, replacing the old "is the mouse inside this
 * rectangle every frame" check that opened the menu repeatedly while held.
 */
public final class Hud {
    private final Label score;
    private final Label best;
    private final Label title;
    private final Label hint;
    private final Label toast;
    private int shownScore;
    private int targetScore;
    private String currentHint = "";

    public Hud(Stage stage, Skin skin, Texture pauseIcon, String levelName, int bestScore, Runnable onPause) {
        ImageButton pause = new ImageButton(new TextureRegionDrawable(pauseIcon));
        pause.getImageCell().size(96f);
        pause.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                onPause.run();
            }
        });

        score = new Label("0", skin, "title");
        score.setFontScale(2f);
        best = new Label("Best " + bestScore, skin, "default");
        Table right = new Table();
        right.add(new Label("SCORE", skin, "button")).right().row();
        right.add(score).right().row();
        right.add(best).right();

        Table top = new Table();
        top.setFillParent(true);
        top.top().pad(24f);
        top.add(pause).left().top();
        top.add().expandX();
        top.add(right).right().top();
        stage.addActor(top);

        title = new Label(levelName, skin, "title");
        title.setFontScale(2.4f);
        title.setAlignment(Align.center);
        Table centre = new Table();
        centre.setFillParent(true);
        centre.top().padTop(70f);
        centre.add(title);
        stage.addActor(centre);
        title.getColor().a = 0f;
        title.addAction(Actions.sequence(Actions.fadeIn(0.4f), Actions.delay(1.4f), Actions.fadeOut(0.6f)));

        hint = new Label("", skin, "button");
        toast = new Label("", skin, "title");
        Table bottom = new Table();
        bottom.setFillParent(true);
        bottom.bottom().padBottom(40f);
        bottom.add(toast).row();
        bottom.add(hint);
        stage.addActor(bottom);
        hint.getColor().a = 0f;
        toast.getColor().a = 0f;
    }

    public void setScore(int value) {
        targetScore = value;
    }

    /** @return true when the rolling counter moved this frame (for a soft tick sound) */
    public boolean update(float delta) {
        if (shownScore == targetScore) {
            return false;
        }
        int step = Math.max(10, (int) ((targetScore - shownScore) * Math.min(1f, delta * 8f)));
        shownScore = Math.min(targetScore, shownScore + step);
        score.setText(String.valueOf(shownScore));
        return true;
    }

    public void showHint(String text) {
        if (text.equals(currentHint)) {
            return;
        }
        currentHint = text;
        hint.clearActions();
        if (text.isEmpty()) {
            hint.addAction(Actions.fadeOut(0.25f));
            return;
        }
        hint.setText(text);
        hint.addAction(Actions.sequence(Actions.fadeIn(0.3f),
            Actions.forever(Actions.sequence(Actions.alpha(0.55f, 0.7f), Actions.alpha(1f, 0.7f)))));
    }

    public void toast(String text) {
        toast.setText(text);
        toast.clearActions();
        toast.setOrigin(Align.center);
        toast.addAction(Actions.sequence(Actions.alpha(0f), Actions.fadeIn(0.2f, Interpolation.pow2Out),
            Actions.delay(1.6f), Actions.fadeOut(0.4f)));
    }
}
