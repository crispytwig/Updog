package com.crispytwig.updog.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

public class Pug extends TamableAnimal implements GeoEntity {
    private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    private static final EntityDataAccessor<Boolean> SCARED = SynchedEntityData.defineId(Pug.class, EntityDataSerializers.BOOLEAN);

    public Pug(EntityType<? extends TamableAnimal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected float getStandingEyeHeight(Pose pPose, EntityDimensions pSize) {
        return pSize.height * 0.45F;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new FloatGoal(this));
        // this.goalSelector.addGoal(2, new Pug.PugScaredGoal(this));
        // this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        // this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F, false));
        this.goalSelector.addGoal(4, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier setAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D).build();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SCARED, false);
    }

    @Override
    public int getAmbientSoundInterval() {
        return 20;
    }

    // @Override
    // protected SoundEvent getAmbientSound() { return ModSoundEvents.PUG_AMBIENT.get(); }



    public boolean isScaredOfDarkness() {
        return this.entityData.get(SCARED);
    }

    public void setScaredOfDarkness(boolean scaredOfDarkness) {
        this.entityData.set(SCARED, scaredOfDarkness);
    }

    public static void scream(Level level, Entity pug) {
        if (pug.isAlive() && !pug.isSilent() && level.random.nextInt(40) == 0) {
            // level.playSound(null, pug.getX(), pug.getY(), pug.getZ(), ModSoundEvents.PUG_SCREAM.get(), pug.getSoundSource(), 1.0F, (level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F);
        }
    }


    @Override
    public boolean isFood(ItemStack pStack) {
        return pStack.is(Items.APPLE);
    }

    // region OLD GECKOLIB
    /* private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6) {
                event.getController().setAnimation(new AnimationBuilder().loop("walk"));
                event.getController().setAnimationSpeed(1.5F);
                return PlayState.CONTINUE;
        } else {
            event.getController().setAnimation(new AnimationBuilder().loop("idle"));
        }
        event.getController().markNeedsReload();
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState scaredPredicate(AnimationEvent<E> event) {
        if (this.isScaredOfDarkness()) {
            event.getController().setAnimation(new AnimationBuilder().loop("scared"));
            return PlayState.CONTINUE;
        }
        event.getController().markNeedsReload();
        return PlayState.STOP;
    }

     */
    //endregion

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel pLevel, AgeableMob pOtherParent) {
        return null;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<GeoAnimatable>(this, "controller", 0, this::predicate));
    }

    private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> animationState) {
        if(animationState.isMoving()) {
            animationState.getController().setAnimation(RawAnimation.begin().then("walk", Animation.LoopType.LOOP));
            return PlayState.CONTINUE;
        }

        animationState.getController().setAnimation(RawAnimation.begin().then("idle", Animation.LoopType.LOOP));
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
