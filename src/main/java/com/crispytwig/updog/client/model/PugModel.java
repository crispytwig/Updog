package com.crispytwig.updog.client.model;

import com.crispytwig.updog.Updog;
import com.crispytwig.updog.entity.Pug;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class PugModel extends GeoModel<Pug> {

	@Override
	public ResourceLocation getModelResource(Pug entity) {
		return new ResourceLocation(Updog.MOD_ID, "geo/pug.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(Pug entity) {
		return new ResourceLocation(Updog.MOD_ID, "textures/entity/pug.png");
	}

	@Override
	public ResourceLocation getAnimationResource(Pug entity) {
		return new ResourceLocation(Updog.MOD_ID, "animations/pug.animation.json");
	}

	@Override
	public void setCustomAnimations(Pug entity, long instanceId, AnimationState<Pug> animationState) {
		CoreGeoBone head = getAnimationProcessor().getBone("head");

		if (head != null) {
			EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);

			head.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
			head.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
		}
	}


}
