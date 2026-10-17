package com.AngryHomies.AngryBirds.gameplay;

/**
 * The level's rules as a small state machine, kept free of rendering so it can be unit-tested.
 *
 * <pre>
 * INTRO    --camera fly-over done-->                         AIMING
 * AIMING   --bird released-->                                FLYING
 * FLYING   --bird spent or all pigs gone-->                  SETTLING
 * SETTLING --world still (or timeout), no pigs-->            WON
 * SETTLING --world still (or timeout), pigs, birds left-->   AIMING
 * SETTLING --world still (or timeout), pigs, no birds-->     LOST
 * </pre>
 *
 * Waiting for the world to settle fixes the old "level failed while the last pig was still
 * falling to its death" bug.
 */
public final class GameSession {
    public enum State { INTRO, AIMING, FLYING, SETTLING, WON, LOST }

    static final float INTRO_SECONDS = 1.8f;
    static final float MIN_SETTLE = 0.8f;
    static final float MAX_SETTLE = 4.5f;

    private State state = State.INTRO;
    private float timer;
    private boolean enteredAiming;

    /** Start straight in AIMING (e.g. a loaded save). */
    public void skipIntro() {
        if (state == State.INTRO) {
            enter(State.AIMING);
        }
    }

    public void onLaunch() {
        if (state == State.AIMING) {
            enter(State.FLYING);
        }
    }

    /**
     * @param birdActive  a launched bird is still in play
     * @param settled     every dynamic body has (almost) stopped
     * @param pigsAlive   pigs remaining
     * @param birdsLeft   birds not yet launched (including one waiting on the slingshot)
     */
    public void update(float delta, boolean birdActive, boolean settled, int pigsAlive, int birdsLeft) {
        timer += delta;
        switch (state) {
            case INTRO:
                if (timer >= INTRO_SECONDS) {
                    enter(State.AIMING);
                }
                break;
            case AIMING:
                if (pigsAlive == 0) {
                    enter(State.SETTLING);
                }
                break;
            case FLYING:
                if (!birdActive || pigsAlive == 0) {
                    enter(State.SETTLING);
                }
                break;
            case SETTLING:
                boolean calm = (settled && timer >= MIN_SETTLE) || timer >= MAX_SETTLE;
                if (calm && !birdActive) {
                    if (pigsAlive == 0) {
                        enter(State.WON);
                    } else if (birdsLeft > 0) {
                        enter(State.AIMING);
                    } else {
                        enter(State.LOST);
                    }
                }
                break;
            default:
                break;
        }
    }

    private void enter(State next) {
        state = next;
        timer = 0f;
        if (next == State.AIMING) {
            enteredAiming = true;
        }
    }

    /** True once after each entry into AIMING: time to put the next bird on the slingshot. */
    public boolean consumeEnteredAiming() {
        boolean value = enteredAiming;
        enteredAiming = false;
        return value;
    }

    public State state() {
        return state;
    }

    public boolean isOver() {
        return state == State.WON || state == State.LOST;
    }
}
