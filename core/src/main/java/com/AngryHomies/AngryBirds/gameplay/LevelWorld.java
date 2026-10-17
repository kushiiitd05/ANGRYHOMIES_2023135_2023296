package com.AngryHomies.AngryBirds.gameplay;

import com.AngryHomies.AngryBirds.assets.Assets;
import com.AngryHomies.AngryBirds.assets.ProceduralTextures;
import com.AngryHomies.AngryBirds.entity.Bird;
import com.AngryHomies.AngryBirds.entity.BirdType;
import com.AngryHomies.AngryBirds.entity.Block;
import com.AngryHomies.AngryBirds.entity.BodyFactory;
import com.AngryHomies.AngryBirds.entity.GameEntity;
import com.AngryHomies.AngryBirds.entity.Material;
import com.AngryHomies.AngryBirds.entity.Pig;
import com.AngryHomies.AngryBirds.entity.PigType;
import com.AngryHomies.AngryBirds.entity.ability.AbilityContext;
import com.AngryHomies.AngryBirds.fx.FragmentSystem;
import com.AngryHomies.AngryBirds.level.LevelDefinition;
import com.AngryHomies.AngryBirds.save.SaveData;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectSet;

/**
 * Everything physical in a level: the Box2D world, pigs, blocks, the active bird, the waiting
 * birds and fragments. Turns measured impacts into damage, removes what broke, and implements
 * the physics side of the special moves (blast, vortex, blade).
 */
public final class LevelWorld implements Disposable {
    private static final float GRACE_SECONDS = 1.2f;
    private static final float ENERGY_FOR_FULL_POWER = 40000f;

    /** Gameplay feedback hooks (sound, particles, score) implemented by the screen. */
    public interface Events {
        void onHit(GameEntity target, float damage, float x, float y, float power);

        void onDestroyed(GameEntity target, float x, float y, float power, boolean offscreen);

        void onBirdImpact(Bird bird, float speed, float x, float y);
    }

    private final AssetManager assets;
    private final ProceduralTextures fxTextures;
    private final Events events;
    private final PhysicsWorld physics = new PhysicsWorld();
    private final DamageSystem damage = new DamageSystem();
    private final FragmentSystem fragments = new FragmentSystem(physics);
    private final Array<Pig> pigs = new Array<>();
    private final Array<Block> blocks = new Array<>();
    private final Array<DamageSystem.Impact> impacts = new Array<>();
    private BirdQueue queue;
    private Bird bird;
    private float grace = GRACE_SECONDS;

    private final PhysicsWorld.StepListener stepListener = new PhysicsWorld.StepListener() {
        @Override
        public void beforeStep() {
            for (Pig pig : pigs) {
                pig.snapshot();
            }
            for (Block block : blocks) {
                block.snapshot();
            }
            if (bird != null) {
                bird.snapshot();
            }
        }

        @Override
        public void afterStep() {
            resolveImpacts();
        }
    };

    public LevelWorld(AssetManager assets, ProceduralTextures fxTextures, Events events) {
        this.assets = assets;
        this.fxTextures = fxTextures;
        this.events = events;
        physics.world().setContactListener(damage);
        Body ground = BodyFactory.ground(physics.world(), WorldConfig.GROUND_Y, -200f, 600f);
        ground.setUserData(DamageSystem.GROUND);
        damage.setEnabled(false);
    }

    // ---------- building ----------

    public void build(LevelDefinition level) {
        for (LevelDefinition.BlockDef b : level.blocks) {
            Material material = Material.valueOf(b.material);
            blocks.add(new Block(physics.world(), material, Assets.block(assets, b.texture), b.texture,
                b.x, b.y, b.angle, b.w, b.h, b.density, fxTextures));
        }
        for (LevelDefinition.PigDef p : level.pigs) {
            PigType type = PigType.valueOf(p.type);
            pigs.add(new Pig(physics.world(), type, assets.get(type.texture()), p.x, p.y, p.w, p.h, p.radius, p.density));
        }
        Array<BirdType> types = new Array<>();
        for (String name : level.birds) {
            types.add(BirdType.valueOf(name));
        }
        queue = new BirdQueue(types);
    }

    public void restore(SaveData save) {
        for (SaveData.BodyData b : save.blocks) {
            Block block = new Block(physics.world(), Material.valueOf(b.kind), Assets.block(assets, b.texture), b.texture,
                b.x, b.y, b.angle, b.w, b.h, b.density, fxTextures).withHealth(b.health);
            restoreMotion(block.body(), b);
            blocks.add(block);
        }
        for (SaveData.BodyData p : save.pigs) {
            PigType type = PigType.valueOf(p.kind);
            Pig pig = new Pig(physics.world(), type, assets.get(type.texture()), p.x, p.y, p.w, p.h, p.radius, p.density)
                .withHealth(p.health);
            pig.body().setTransform(p.x, p.y, p.angle);
            restoreMotion(pig.body(), p);
            pigs.add(pig);
        }
        Array<BirdType> types = new Array<>();
        for (String name : save.birds) {
            types.add(BirdType.valueOf(name));
        }
        queue = new BirdQueue(types);
    }

    private static void restoreMotion(Body body, SaveData.BodyData data) {
        body.setLinearVelocity(data.vx, data.vy);
        body.setAngularVelocity(data.spin);
    }

    public SaveData toSaveData(int levelId, int score) {
        SaveData save = new SaveData();
        save.levelId = levelId;
        save.score = score;
        save.savedAt = System.currentTimeMillis();
        if (bird != null && bird.state() == Bird.State.LOADED) {
            save.birds.add(bird.type().name());
        }
        for (BirdType type : queue.remainingTypes()) {
            save.birds.add(type.name());
        }
        for (Block block : blocks) {
            SaveData.BodyData data = bodyData(block, block.material().name());
            data.texture = block.textureName();
            data.density = block.density();
            save.blocks.add(data);
        }
        for (Pig pig : pigs) {
            SaveData.BodyData data = bodyData(pig, pig.type().name());
            data.radius = pig.radius();
            data.density = pig.density();
            save.pigs.add(data);
        }
        return save;
    }

    private static SaveData.BodyData bodyData(GameEntity entity, String kind) {
        SaveData.BodyData data = new SaveData.BodyData();
        Body body = entity.body();
        data.kind = kind;
        data.x = body.getPosition().x;
        data.y = body.getPosition().y;
        data.angle = body.getAngle();
        data.vx = body.getLinearVelocity().x;
        data.vy = body.getLinearVelocity().y;
        data.spin = body.getAngularVelocity();
        data.w = entity.width();
        data.h = entity.height();
        data.health = entity.health();
        return data;
    }

    // ---------- frame ----------

    /** @param physicsDelta game time for physics (0 during hit-stop, scaled in slow motion) */
    public void update(float delta, float physicsDelta, AbilityContext context) {
        if (grace > 0f) {
            grace -= physicsDelta;
            damage.setEnabled(grace <= 0f);
        }
        physics.update(physicsDelta, stepListener);
        queue.update(delta);
        fragments.update(physicsDelta);
        for (Pig pig : pigs) {
            pig.update(delta);
        }
        for (Block block : blocks) {
            block.update(delta);
        }
        if (bird != null) {
            bird.tick(physicsDelta, context);
        }
        removeOutOfBounds(pigs);
        removeOutOfBounds(blocks);
        removeDead(pigs);
        removeDead(blocks);
    }

    private void resolveImpacts() {
        impacts.clear();
        damage.drain(impacts);
        for (DamageSystem.Impact impact : impacts) {
            float energy = impact.energy();
            float power = Math.min(1f, energy / ENERGY_FOR_FULL_POWER);
            hitSide(impact.a, impact.b, energy, power, impact);
            hitSide(impact.b, impact.a, energy, power, impact);
        }
    }

    private void hitSide(GameEntity target, GameEntity other, float energy, float power, DamageSystem.Impact impact) {
        if (target == null || target.isDead()) {
            return;
        }
        if (target instanceof Bird) {
            ((Bird) target).onImpact(impact.speed);
            events.onBirdImpact((Bird) target, impact.speed, impact.x, impact.y);
            return;
        }
        float multiplier = other instanceof Bird ? ((Bird) other).attackMultiplier(target) : 1f;
        applyDamage(target, DamageModel.damage(energy, target.toughness(), multiplier), impact.x, impact.y, power);
    }

    /** Single entry point for all damage (impacts, blasts, blades). */
    public void applyDamage(GameEntity target, float amount, float x, float y, float power) {
        if (amount < 0.5f || target.isDead()) {
            return;
        }
        target.markHit(x, y);
        target.applyDamage(amount);
        events.onHit(target, amount, x, y, power);
    }

    private <T extends GameEntity> void removeDead(Array<T> entities) {
        for (int i = entities.size - 1; i >= 0; i--) {
            T entity = entities.get(i);
            if (!entity.isDead()) {
                continue;
            }
            Vector2 p = entity.position();
            float power = Math.min(1f, entity.body().getLinearVelocity().len() / 30f + 0.4f);
            if (entity instanceof Block) {
                fragments.shatter((Block) entity, power, entity.lastHit().x, entity.lastHit().y);
            }
            events.onDestroyed(entity, p.x, p.y, power, false);
            physics.destroyLater(entity.body());
            entities.removeIndex(i);
        }
    }

    private <T extends GameEntity> void removeOutOfBounds(Array<T> entities) {
        for (int i = entities.size - 1; i >= 0; i--) {
            T entity = entities.get(i);
            Vector2 p = entity.position();
            if (WorldConfig.outOfBounds(p.x, p.y)) {
                entity.kill();
                events.onDestroyed(entity, p.x, p.y, 0f, true);
                physics.destroyLater(entity.body());
                entities.removeIndex(i);
            }
        }
    }

    // ---------- birds ----------

    public Bird loadBird(BirdType type) {
        removeBird();
        bird = new Bird(physics.world(), type, assets.get(type.texture()), assets.get(type.abilityTexture()),
            SlingshotController.REST.x, SlingshotController.REST.y);
        return bird;
    }

    public void removeBird() {
        if (bird != null) {
            bird.kill();
            physics.destroyLater(bird.body());
            bird = null;
        }
    }

    // ---------- special-move physics ----------

    public void explode(Bird source, Vector2 center, float radius, float impulse, float maxDamage) {
        Array<Body> bodies = new Array<>();
        physics.world().getBodies(bodies);
        for (Body body : bodies) {
            if (body.getType() != BodyDef.BodyType.DynamicBody || (source != null && body == source.body())) {
                continue;
            }
            Vector2 offset = new Vector2(body.getWorldCenter()).sub(center);
            Object data = body.getUserData();
            float distance = data instanceof GameEntity ? ((GameEntity) data).surfaceDistance(center) : offset.len();
            float strength = DamageModel.falloff(distance, radius);
            if (strength <= 0f) {
                continue;
            }
            Vector2 direction = offset.isZero(0.01f) ? new Vector2(0f, 1f) : offset.nor();
            direction.y += 0.35f; // blasts throw things up and out, which reads better
            body.applyLinearImpulse(direction.nor().scl(impulse * strength), body.getWorldCenter(), true);
            if (data instanceof GameEntity && !(data instanceof Bird)) {
                GameEntity target = (GameEntity) data;
                float multiplier = source != null ? source.attackMultiplier(target) : 1f;
                applyDamage(target, maxDamage * strength * multiplier / target.toughness(), center.x, center.y, strength);
            }
        }
    }

    public void vortex(Bird source, Vector2 center, float radius, float impulse) {
        Array<Body> bodies = new Array<>();
        physics.world().getBodies(bodies);
        for (Body body : bodies) {
            if (body.getType() != BodyDef.BodyType.DynamicBody || (source != null && body == source.body())) {
                continue;
            }
            Vector2 toCenter = new Vector2(center).sub(body.getWorldCenter());
            float strength = DamageModel.falloff(toCenter.len(), radius);
            if (strength <= 0f) {
                continue;
            }
            Vector2 pull = toCenter.nor();
            Vector2 swirl = new Vector2(-pull.y, pull.x).scl(0.6f); // tangential part makes it spiral
            body.applyLinearImpulse(pull.add(swirl).scl(impulse * strength), body.getWorldCenter(), true);
        }
    }

    public void bladeStrike(Bird source, Vector2 from, Vector2 to, float maxDamage) {
        if (from.dst2(to) < 1e-4f) {
            return;
        }
        ObjectSet<Body> cut = new ObjectSet<>();
        physics.world().rayCast((fixture, point, normal, fraction) -> {
            Body body = fixture.getBody();
            if (body.getType() == BodyDef.BodyType.DynamicBody && (source == null || body != source.body())) {
                cut.add(body);
            }
            return 1f; // keep going: the blade passes through everything on the line
        }, from, to);
        Vector2 direction = new Vector2(to).sub(from).nor();
        Vector2 side = new Vector2(-direction.y, direction.x);
        com.AngryHomies.AngryBirds.DevLog.event("blade_hits", "bodies", cut.size);
        for (Body body : cut) {
            Vector2 rel = new Vector2(body.getWorldCenter()).sub(from);
            float sign = rel.dot(side) >= 0f ? 1f : -1f;
            Vector2 knock = new Vector2(side).scl(sign * 14f).mulAdd(direction, 6f).scl(body.getMass());
            body.applyLinearImpulse(knock, body.getWorldCenter(), true);
            Object data = body.getUserData();
            if (data instanceof GameEntity && !(data instanceof Bird)) {
                GameEntity target = (GameEntity) data;
                float multiplier = source != null ? source.attackMultiplier(target) : 1f;
                // a blade cuts rather than bludgeons: toughness counts only as its square root
                float sliceDamage = maxDamage * multiplier / (float) Math.sqrt(target.toughness());
                Vector2 onLine = new Vector2(direction).scl(rel.dot(direction)).add(from); // where the blade crossed
                applyDamage(target, sliceDamage, onLine.x, onLine.y, 1f);
            }
        }
    }

    /** Aim-line blocker: true when the segment touches anything solid except the loaded bird. */
    public boolean pathBlocked(Vector2 from, Vector2 to) {
        if (from.dst2(to) < 1e-6f) {
            return false;
        }
        boolean[] hit = {false};
        physics.world().rayCast((fixture, point, normal, fraction) -> {
            if (bird != null && fixture.getBody() == bird.body()) {
                return -1f;
            }
            hit[0] = true;
            return 0f;
        }, from, to);
        return hit[0];
    }

    // ---------- queries ----------

    public boolean settled() {
        return physics.isSettled(0.35f, 0.35f);
    }

    public float alpha() {
        return physics.alpha();
    }

    public Array<Pig> pigs() {
        return pigs;
    }

    public Array<Block> blocks() {
        return blocks;
    }

    public Bird bird() {
        return bird;
    }

    public BirdQueue queue() {
        return queue;
    }

    public FragmentSystem fragments() {
        return fragments;
    }

    /** Birds that can still be launched, counting one sitting on the slingshot. */
    public int birdsLeft() {
        int loaded = bird != null && bird.state() == Bird.State.LOADED ? 1 : 0;
        return queue.remaining() + loaded;
    }

    /** Centre of the pig fortress, for the opening camera fly-over. */
    public Vector2 fortressCenter() {
        Vector2 sum = new Vector2();
        for (Pig pig : pigs) {
            sum.add(pig.position());
        }
        return pigs.isEmpty() ? new Vector2(150f, 40f) : sum.scl(1f / pigs.size);
    }

    @Override
    public void dispose() {
        physics.dispose();
    }
}
