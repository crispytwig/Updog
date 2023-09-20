package com.crispytwig.updog.entity;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
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

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

public class Pug extends TamableAnimal implements GeoEntity {
    private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    private static final EntityDataAccessor<Boolean> SCARED = SynchedEntityData.defineId(Pug.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_COLLAR_COLOR = SynchedEntityData.defineId(Pug.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_VARIANT;

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
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F, false));
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
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemstack = player.getItemInHand(interactionHand);
        Item item = itemstack.getItem();
        if (this.level().isClientSide) {
            boolean flag = this.isOwnedBy(player) || this.isTame() || itemstack.is(Items.BONE) && !this.isTame();
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
        } else if (itemstack.is(Items.BONE)) {
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
        this.entityData.define(SCARED, false);
        this.entityData.define(DATA_VARIANT, 0);
        this.entityData.define(DATA_COLLAR_COLOR, DyeColor.RED.getId());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("Variant", getVariant().getId());
        compoundTag.putByte("CollarColor", (byte)this.getCollarColor().getId());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.setVariant(Pug.Variant.BY_ID[compoundTag.getInt("Variant")]);

        if (compoundTag.contains("CollarColor", 99)) {
            this.setCollarColor(DyeColor.byId(compoundTag.getInt("CollarColor")));
        }
    }

    public Pug.Variant getVariant() {
        return Pug.Variant.BY_ID[this.entityData.get(DATA_VARIANT)];
    }

    private void setVariant(Pug.Variant variant) {
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
            if (spawnData instanceof Pug.PugGroupData) {
                if (((Pug.PugGroupData)spawnData).getGroupSize() >= 2) {
                    bl = true;
                }
            } else {
                spawnData = new Pug.PugGroupData(Pug.Variant.getCommonSpawnVariant(randomSource), Pug.Variant.getCommonSpawnVariant(randomSource));
            }

            this.setVariant(((Pug.PugGroupData)spawnData).getVariant(randomSource));
            if (bl) {
                this.setAge(-24000);
            }

            return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
        }
    }

    public DyeColor getCollarColor() {
        return DyeColor.byId(this.entityData.get(DATA_COLLAR_COLOR));
    }

    public void setCollarColor(DyeColor dyeColor) {
        this.entityData.set(DATA_COLLAR_COLOR, dyeColor.getId());
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
    public boolean isFood(ItemStack itemStack) {
        Item item = itemStack.getItem();
        return item.isEdible() && Objects.requireNonNull(itemStack.getFoodProperties(this)).isMeat();
    }

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
        if (this.isInSittingPose()) {
            animationState.getController().setAnimation(RawAnimation.begin().then("sit", Animation.LoopType.LOOP));
            return PlayState.CONTINUE;
        } else if (animationState.isMoving()) {
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

    public enum Variant {
        FAWN(0, "golden", true),
        BLACK(1, "chocolate", true);

        public static final Pug.Variant[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(Pug.Variant::getId)).toArray(Pug.Variant[]::new);
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

        public static Pug.Variant getTypeById(int id) {
            for (Pug.Variant type : values()) {
                if (type.id == id) return type;
            }
            return Variant.FAWN;
        }

        public static Pug.Variant getCommonSpawnVariant(RandomSource random) {
            return getSpawnVariant(random, true);
        }

        private static Pug.Variant getSpawnVariant(RandomSource random, boolean common) {
            Pug.Variant[] variants = Arrays.stream(BY_ID).filter((variant) -> {
                return variant.common == common;
            }).toArray(Pug.Variant[]::new);
            return Util.getRandom(variants, random);
        }
    }

    public static class PugGroupData extends AgeableMob.AgeableMobGroupData {
        public final Pug.Variant[] types;

        public PugGroupData(Pug.Variant... variants) {
            super(false);
            this.types = variants;
        }

        public Pug.Variant getVariant(RandomSource random) {
            return this.types[random.nextInt(this.types.length)];
        }
    }

    static {
        DATA_VARIANT = SynchedEntityData.defineId(Pug.class, EntityDataSerializers.INT);
    }
}
