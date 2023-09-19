package com.crispytwig.updog.client.renderer.layer;

import com.crispytwig.updog.Updog;
import com.crispytwig.updog.entity.Retriever;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import software.bernie.example.entity.CoolKidEntity;
import software.bernie.geckolib.GeckoLib;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class RetrieverCollarLayer extends GeoRenderLayer<Retriever> {
    public RetrieverCollarLayer(GeoRenderer<Retriever> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, Retriever animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {

        if (animatable.isTame()) {
            int color = animatable.getCollarColor().getId();
            ResourceLocation TEXTURE = new ResourceLocation(Updog.MOD_ID, "textures/entity/retriever/collar/" + DyeColor.byId(color).getName() + ".png");

            RenderType armorRenderType = RenderType.entityCutout(TEXTURE);

            getRenderer().reRender(getDefaultBakedModel(animatable), poseStack, bufferSource, animatable, armorRenderType,
                    bufferSource.getBuffer(armorRenderType), partialTick, packedLight, OverlayTexture.NO_OVERLAY,
                    1, 1, 1, 1);
        }

    }
}
