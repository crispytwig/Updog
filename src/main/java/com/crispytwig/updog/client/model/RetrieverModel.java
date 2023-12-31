package com.crispytwig.updog.client.model;

import com.crispytwig.updog.Updog;
import com.crispytwig.updog.entity.Retriever;
import com.google.common.collect.Maps;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

import java.util.Locale;
import java.util.Map;

public class RetrieverModel extends GeoModel<Retriever> {

	@Override
	public ResourceLocation getModelResource(Retriever entity) {
		return new ResourceLocation(Updog.MOD_ID, "geo/retriever.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(Retriever entity) {
		if (entity.getVariant().getId() == 0) {
			return new ResourceLocation(Updog.MOD_ID, "textures/entity/retriever/golden.png");
		} else if (entity.getVariant().getId() == 1) {
			return new ResourceLocation(Updog.MOD_ID, "textures/entity/retriever/chocolate.png");
		} else if (entity.getVariant().getId() == 2) {
			return new ResourceLocation(Updog.MOD_ID, "textures/entity/retriever/black.png");
		}
		return new ResourceLocation(Updog.MOD_ID, "textures/entity/retriever/golden.png");
	}

	@Override
	public ResourceLocation getAnimationResource(Retriever entity) {
		return new ResourceLocation(Updog.MOD_ID, "animations/retriever.animation.json");
	}

	@Override
	public void setCustomAnimations(Retriever entity, long instanceId, AnimationState<Retriever> animationState) {
		CoreGeoBone head = getAnimationProcessor().getBone("head");

		if (head != null) {
			EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);

			head.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
			head.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
		}
	}


}
