package com.AngryHomies.AngryBirds.gameplay;

/**
 * Pure maths for how much a collision hurts, kept free of Box2D so it can be unit-tested.
 *
 * <p>Damage follows impact energy {@code E = 1/2 * mu * v^2}, where {@code v} is the closing
 * speed along the contact normal and {@code mu} the reduced mass of the pair (the full mass when
 * hitting static ground). Resting contacts have almost no closing speed, so stacked pieces don't
 * wear each other down — the old version damaged on every touch.
 */
public final class DamageModel {
    /** Closing speeds below this are nudges, not hits. */
    public static final float MIN_IMPACT_SPEED = 3f;
    public static final float DAMAGE_PER_JOULE = 0.0022f;

    private DamageModel() {
    }

    /** Reduced mass; a mass of 0 means static (infinitely heavy). */
    public static float effectiveMass(float massA, float massB) {
        if (massA <= 0f) {
            return massB;
        }
        if (massB <= 0f) {
            return massA;
        }
        return massA * massB / (massA + massB);
    }

    public static float impactEnergy(float massA, float massB, float closingSpeed) {
        if (closingSpeed < MIN_IMPACT_SPEED) {
            return 0f;
        }
        return 0.5f * effectiveMass(massA, massB) * closingSpeed * closingSpeed;
    }

    public static float damage(float energy, float toughness, float multiplier) {
        if (toughness <= 0f || Float.isInfinite(toughness) || toughness == Float.MAX_VALUE) {
            return 0f;
        }
        return energy * DAMAGE_PER_JOULE * multiplier / toughness;
    }

    /** Blast falloff: full strength at the centre, zero at the edge, quadratic in between. */
    public static float falloff(float distance, float radius) {
        if (distance >= radius || radius <= 0f) {
            return 0f;
        }
        float k = 1f - distance / radius;
        return k * k;
    }
}
