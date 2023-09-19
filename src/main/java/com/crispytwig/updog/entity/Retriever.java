package com.crispytwig.updog.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;

import java.util.EnumSet;
import java.util.Objects;

public class Retriever extends Dog {

    private static final EntityDataAccessor<Integer> DATA_COLLAR_COLOR = SynchedEntityData.defineId(Retriever.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_REMAINING_ANGER_TIME = SynchedEntityData.defineId(Retriever.class, EntityDataSerializers.INT);

    public Retriever(EntityType<? extends Retriever> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected float getStandingEyeHeight(Pose pPose, EntityDimensions pSize) {
        return pSize.height * 0.45F;
    }

    public static AttributeSupplier setAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D).build();
    }

    private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> animationState) {
        if (animationState.isMoving() && this.isTame() && this.getHealth() > this.getMaxHealth() * 0.5F) {
            animationState.getController().setAnimation(RawAnimation.begin().then("walk.maxhealth", Animation.LoopType.LOOP));
            animationState.setControllerSpeed(1.4F);
        } else if (animationState.isMoving() && this.isTame() && this.getHealth() < this.getMaxHealth() * 0.5F || !this.isTame() && animationState.isMoving()) {
            animationState.getController().setAnimation(RawAnimation.begin().then("walk.lowhealth", Animation.LoopType.LOOP));
            animationState.setControllerSpeed(1.4F);
        } else if (this.isTame() && this.getHealth() > this.getMaxHealth() * 0.5F) {
            animationState.getController().setAnimation(RawAnimation.begin().then("idle.maxhealth", Animation.LoopType.LOOP));
        } else if (this.isTame() && this.getHealth() < this.getMaxHealth() * 0.5F || !this.isTame()) {
            animationState.getController().setAnimation(RawAnimation.begin().then("idle.lowhealth", Animation.LoopType.LOOP));
        }
        return PlayState.CONTINUE;
    }


}
