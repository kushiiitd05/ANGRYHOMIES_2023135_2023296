package com.AngryHomies.AngryBirds.gameplay;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameSessionTest {
    private static void run(GameSession session, float seconds, boolean birdActive, boolean settled, int pigs, int birds) {
        for (float t = 0f; t < seconds; t += 0.05f) {
            session.update(0.05f, birdActive, settled, pigs, birds);
        }
    }

    @Test
    void introLeadsToAimingAndAsksForABird() {
        GameSession session = new GameSession();
        run(session, GameSession.INTRO_SECONDS + 0.1f, false, true, 3, 3);
        assertEquals(GameSession.State.AIMING, session.state());
        assertTrue(session.consumeEnteredAiming());
        assertFalse(session.consumeEnteredAiming());
    }

    @Test
    void doesNotLoseWhileThingsAreStillMoving() {
        GameSession session = new GameSession();
        session.skipIntro();
        session.onLaunch();
        run(session, 0.2f, false, false, 1, 0); // bird done, last pig still falling
        assertEquals(GameSession.State.SETTLING, session.state());
        run(session, 2f, false, false, 1, 0);
        assertEquals(GameSession.State.SETTLING, session.state());
    }

    @Test
    void winsWhenTheLastPigDiesDuringSettling() {
        GameSession session = new GameSession();
        session.skipIntro();
        session.onLaunch();
        run(session, 0.2f, false, false, 1, 0);
        run(session, 1f, false, true, 0, 0);
        assertEquals(GameSession.State.WON, session.state());
    }

    @Test
    void losesOnlyAfterSettlingWithPigsLeftAndNoBirds() {
        GameSession session = new GameSession();
        session.skipIntro();
        session.onLaunch();
        run(session, 1.5f, false, true, 2, 0);
        assertEquals(GameSession.State.LOST, session.state());
    }

    @Test
    void returnsToAimingWhenBirdsRemain() {
        GameSession session = new GameSession();
        session.skipIntro();
        session.consumeEnteredAiming();
        session.onLaunch();
        run(session, 1.5f, false, true, 2, 1);
        assertEquals(GameSession.State.AIMING, session.state());
        assertTrue(session.consumeEnteredAiming());
    }

    @Test
    void timesOutIfTheWorldNeverQuiteSettles() {
        GameSession session = new GameSession();
        session.skipIntro();
        session.onLaunch();
        run(session, GameSession.MAX_SETTLE + 0.5f, false, false, 2, 1);
        assertEquals(GameSession.State.AIMING, session.state());
    }
}
