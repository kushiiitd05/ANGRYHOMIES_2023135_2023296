package com.AngryHomies.AngryBirds.ui;

import com.AngryHomies.AngryBirds.audio.AudioManager;
import com.AngryHomies.AngryBirds.audio.Sfx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

/**
 * End-of-level card over the frozen level: "Level Cleared"/"Level Failed" art, a score that
 * counts up, then stars that stamp in one by one (each a little higher-pitched), then buttons.
 */
public final class ResultOverlay {
    /** Result buttons; implemented by the gameplay screen. */
    public interface Listener {
        void onMenu();

        void onRetry();

        void onNext();
    }

    private static final float COUNT_SECONDS = 1.2f;

    private final Stage stage;
    private final Skin skin;
    private final AudioManager audio;
    private final Texture star;
    private final Texture starEmpty;
    private final Texture pixel;

    public ResultOverlay(Stage stage, Skin skin, AudioManager audio, Texture star, Texture starEmpty, Texture pixel) {
        this.stage = stage;
        this.skin = skin;
        this.audio = audio;
        this.star = star;
        this.starEmpty = starEmpty;
        this.pixel = pixel;
    }

    public void show(boolean won, Texture art, int score, int stars, int best, boolean hasNext, Listener listener) {
        Image dim = new Image(pixel);
        dim.setFillParent(true);
        dim.setColor(0f, 0f, 0f, 0f);
        dim.addAction(Actions.alpha(0.7f, 0.4f));
        stage.addActor(dim);

        Table card = new Table();
        card.setFillParent(true);
        Image banner = new Image(new TextureRegionDrawable(art), Scaling.fit);
        card.add(banner).size(860f, 484f).colspan(3).row();

        Table starRow = new Table();
        Image[] filled = new Image[3];
        for (int i = 0; i < 3; i++) {
            com.badlogic.gdx.scenes.scene2d.ui.Stack slot = new com.badlogic.gdx.scenes.scene2d.ui.Stack();
            slot.add(new Image(starEmpty));
            filled[i] = new Image(star);
            filled[i].getColor().a = 0f;
            slot.add(filled[i]);
            starRow.add(slot).size(i == 1 ? 130f : 105f).pad(6f).bottom();
        }
        if (won) {
            card.add(starRow).colspan(3).padTop(-40f).row();
        }

        Label scoreLabel = new Label("0", skin, "title");
        scoreLabel.setFontScale(2.6f);
        scoreLabel.setAlignment(Align.center);
        card.add(scoreLabel).colspan(3).padTop(6f).row();
        Label bestLabel = new Label(score > best && won ? "New best!" : "Best " + Math.max(best, won ? score : 0), skin);
        card.add(bestLabel).colspan(3).padBottom(16f).row();

        Table buttons = new Table();
        buttons.defaults().width(260f).height(72f).pad(10f);
        buttons.add(button("Menu", listener::onMenu));
        buttons.add(button("Retry", listener::onRetry));
        if (won && hasNext) {
            buttons.add(button("Next", listener::onNext));
        }
        card.add(buttons).colspan(3);
        buttons.getColor().a = 0f;
        stage.addActor(card);

        card.getColor().a = 0f;
        card.setTransform(true);
        card.setOrigin(stage.getWidth() / 2f, stage.getHeight() / 2f);
        card.setScale(0.8f);
        card.addAction(Actions.parallel(Actions.fadeIn(0.35f), Actions.scaleTo(1f, 1f, 0.45f, Interpolation.swingOut)));

        audio.play(won ? Sfx.WIN : Sfx.LOSE, 1f, 0f);
        scoreLabel.addAction(Actions.sequence(Actions.delay(0.5f), countUp(scoreLabel, score)));

        float starStart = 0.5f + COUNT_SECONDS + 0.15f;
        for (int i = 0; i < stars && won; i++) {
            final float pitch = 1f + i * 0.12f;
            Image image = filled[i];
            image.setOrigin(Align.center);
            image.addAction(Actions.sequence(Actions.delay(starStart + i * 0.4f),
                Actions.run(() -> audio.playPitched(Sfx.STAR, 1f, pitch)),
                Actions.parallel(Actions.fadeIn(0.1f), Actions.sequence(Actions.scaleTo(2.4f, 2.4f),
                    Actions.scaleTo(1f, 1f, 0.3f, Interpolation.bounceOut)))));
        }
        buttons.addAction(Actions.sequence(Actions.delay(starStart + (won ? stars : 0) * 0.4f + 0.2f),
            Actions.fadeIn(0.3f)));
    }

    private Action countUp(Label label, int goal) {
        return new TemporalAction(COUNT_SECONDS, Interpolation.pow2Out) {
            private int last = -1;

            @Override
            protected void update(float percent) {
                int value = Math.round(goal * percent);
                label.setText(String.valueOf(value));
                if (value / 1000 != last / 1000) {
                    audio.play(Sfx.SCORE_TICK, 0.8f, 0.1f);
                }
                last = value;
            }
        };
    }

    private TextButton button(String text, Runnable action) {
        TextButton button = new TextButton(text, skin);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audio.play(Sfx.UI_CLICK);
                action.run();
            }
        });
        return button;
    }
}
