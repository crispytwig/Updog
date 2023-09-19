package com.crispytwig.updog.entity;

import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.*;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

public class Retriever extends TamableAnimal implements GeoEntity, NeutralMob {
    private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    private static final EntityDataAccessor<Integer> DATA_COLLAR_COLOR = SynchedEntityData.defineId(Retriever.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_REMAINING_ANGER_TIME = SynchedEntityData.defineId(Retriever.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_VARIANT;

    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    @Nullable
    private UUID persistentAngerTarget;

    public Retriever(EntityType<? extends Retriever> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);

        this.setTame(false);
    }

    @Override
    protected float getStandingEyeHeight(Pose pPose, EntityDimensions pSize) {
        return pSize.height * 0.45F;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.5D) {
            @Override
            protected boolean shouldPanic() {
                return this.mob.isFreezing() || this.mob.isOnFire();
            }
        });
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(5, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F, false));
        this.goalSelector.addGoal(6, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, (new HurtByTargetGoal(this)).setAlertOthers());
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
        this.targetSelector.addGoal(7, new NearestAttackableTargetGoal<>(this, AbstractSkeleton.class, false));
        this.targetSelector.addGoal(8, new ResetUniversalAngerTargetGoal<>(this, true));
    }

    public static AttributeSupplier setAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.3F).add(Attributes.MAX_HEALTH, 8.0D).add(Attributes.ATTACK_DAMAGE, 2.0D).build();
    }

    @Override
    public void setTame(boolean isTame) {
        super.setTame(isTame);
        if (isTame) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0D);
            this.setHealth(20.0F);
        } else {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(8.0D);
        }

        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4.0D);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemstack = player.getItemInHand(interactionHand);
        Item item = itemstack.getItem();
        if (this.level().isClientSide) {
            boolean flag = this.isOwnedBy(player) || this.isTame() || itemstack.is(Items.BONE) && !this.isTame() && !this.isAngry();
            return flag ? InteractionResult.CONSUME : InteractionResult.PASS;
        } else if (this.isTame()) {
            if (this.isFood(itemstack) && this.getHealth() < this.getMaxHealth()) {
                this.heal((float) Objects.requireNonNull(itemstack.getFoodProperties(this)).getNutrition());
                if (!player.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }

                this.gameEvent(GameEvent.EAT, this);
                return InteractionResult.SUCCESS;
            } else {
                if (item instanceof DyeItem) {
                    DyeItem dyeitem = (DyeItem)item;
                    if (this.isOwnedBy(player)) {
                        DyeColor dyecolor = dyeitem.getDyeColor();
                        if (dyecolor != this.getCollarColor()) {
                            this.setCollarColor(dyecolor);
                            if (!player.getAbilities().instabuild) {
                                itemstack.shrink(1);
                            }

                            return InteractionResult.SUCCESS;
                        }

                        return super.mobInteract(player, interactionHand);
                    }
                }

                InteractionResult interactionresult = super.mobInteract(player, interactionHand);
                if ((!interactionresult.consumesAction() || this.isBaby()) && this.isOwnedBy(player)) {
                    this.setOrderedToSit(!this.isOrderedToSit());
                    this.jumping = false;
                    this.navigation.stop();
                    this.setTarget(null);
                    return InteractionResult.SUCCESS;
                } else {
                    return interactionresult;
                }
            }
        } else if (itemstack.is(Items.BONE) && !this.isAngry()) {
            if (!player.getAbilities().instabuild) {
                itemstack.shrink(1);
            }

            if (this.random.nextInt(3) == 0 && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, player)) {
                this.tame(player);
                this.navigation.stop();
                this.setTarget(null);
                this.setOrderedToSit(true);
                this.level().broadcastEntityEvent(this, (byte)7);
            } else {
                this.level().broadcastEntityEvent(this, (byte)6);
            }

            return InteractionResult.SUCCESS;
        } else {
            return super.mobInteract(player, interactionHand);
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_VARIANT, 0);
        this.entityData.define(DATA_COLLAR_COLOR, DyeColor.RED.getId());
        this.entityData.define(DATA_REMAINING_ANGER_TIME, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("Variant", getVariant().getId());
        compoundTag.putByte("CollarColor", (byte)this.getCollarColor().getId());
        this.addPersistentAngerSaveData(compoundTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.setVariant(Retriever.Variant.BY_ID[compoundTag.getInt("Variant")]);

        if (compoundTag.contains("CollarColor", 99)) {
            this.setCollarColor(DyeColor.byId(compoundTag.getInt("CollarColor")));
        }

        this.readPersistentAngerSaveData(this.level(), compoundTag);
    }

    public Retriever.Variant getVariant() {
        return Retriever.Variant.BY_ID[this.entityData.get(DATA_VARIANT)];
    }

    private void setVariant(Retriever.Variant variant) {
        this.entityData.set(DATA_VARIANT, variant.getId());
    }

    @Override
    @javax.annotation.Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @org.jetbrains.annotations.Nullable SpawnGroupData spawnData, @org.jetbrains.annotations.Nullable CompoundTag dataTag) {
        boolean bl = false;
        if (reason == MobSpawnType.BUCKET) {
            return spawnData;
        } else {
            RandomSource randomSource = level.getRandom();
            if (spawnData instanceof Retriever.RetrieverGroupData) {
                if (((Retriever.RetrieverGroupData)spawnData).getGroupSize() >= 2) {
                    bl = true;
                }
            } else {
                spawnData = new Retriever.RetrieverGroupData(Variant.getCommonSpawnVariant(randomSource), Variant.getCommonSpawnVariant(randomSource));
            }

            this.setVariant(((Retriever.RetrieverGroupData)spawnData).getVariant(randomSource));
            if (bl) {
                this.setAge(-24000);
            }

            return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
        }
    }


    @Override
    public boolean isFood(ItemStack itemStack) {
        Item item = itemStack.getItem();
        return item.isEdible() && Objects.requireNonNull(itemStack.getFoodProperties(this)).isMeat();
    }

    public int getRemainingPersistentAngerTime() {
        return this.entityData.get(DATA_REMAINING_ANGER_TIME);
    }

    public void setRemainingPersistentAngerTime(int angerTime) {
        this.entityData.set(DATA_REMAINING_ANGER_TIME, angerTime);
    }

    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    @Nullable
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    public void setPersistentAngerTarget(@Nullable UUID userID) {
        this.persistentAngerTarget = userID;
    }

    public DyeColor getCollarColor() {
        return DyeColor.byId(this.entityData.get(DATA_COLLAR_COLOR));
    }

    public void setCollarColor(DyeColor dyeColor) {
        this.entityData.set(DATA_COLLAR_COLOR, dyeColor.getId());
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel pLevel, AgeableMob pOtherParent) {
        return null;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<GeoAnimatable>(this, "controller", 5, this::predicate));
    }

    private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> animationState) {
        if(animationState.isMoving()) {
            animationState.getController().setAnimation(RawAnimation.begin().then("walk.maxhealth", Animation.LoopType.LOOP));
        } else {
            animationState.getController().setAnimation(RawAnimation.begin().then("idle.maxhealth", Animation.LoopType.LOOP));
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public enum Variant {
        GOLDEN(0, "golden", true),
        CHOCOLATE(1, "chocolate", true),
        BLACK(2, "black", true);

        public static final Retriever.Variant[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(Variant::getId)).toArray(Variant[]::new);
        private final int id;
        private final String name;
        private final boolean common;

        private Variant(int j, String string2, boolean bl) {
            this.id = j;
            this.name = string2;
            this.common = bl;
        }

        public int getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public static Variant getTypeById(int id) {
            for (Variant type : values()) {
                if (type.id == id) return type;
            }
            return Variant.GOLDEN;
        }

        public static Retriever.Variant getCommonSpawnVariant(RandomSource random) {
            return getSpawnVariant(random, true);
        }

        private static Retriever.Variant getSpawnVariant(RandomSource random, boolean common) {
            Retriever.Variant[] variants = Arrays.stream(BY_ID).filter((variant) -> {
                return variant.common == common;
            }).toArray(Variant[]::new);
            return Util.getRandom(variants, random);
        }
    }

    public static class RetrieverGroupData extends AgeableMob.AgeableMobGroupData {
        public final Retriever.Variant[] types;

        public RetrieverGroupData(Retriever.Variant... variants) {
            super(false);
            this.types = variants;
        }

        public Retriever.Variant getVariant(RandomSource random) {
            return this.types[random.nextInt(this.types.length)];
        }
    }

    static {
        DATA_VARIANT = SynchedEntityData.defineId(Retriever.class, EntityDataSerializers.INT);
    }
}
