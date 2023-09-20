package com.crispytwig.updog.client.renderer;

import com.crispytwig.updog.client.model.RetrieverModel;
import com.crispytwig.updog.client.renderer.layer.RetrieverCollarLayer;
import com.crispytwig.updog.entity.Retriever;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class RetrieverRenderer extends GeoEntityRenderer<Retriever> {
        public RetrieverRenderer(EntityRendererProvider.Context renderManager) {
            super(renderManager, new RetrieverModel());
            addRenderLayer(new RetrieverCollarLayer(this));
        }

        @Override
        public void render(Retriever entity, float entityYaw, float partialTick, PoseStack poseStack,
                           MultiBufferSource bufferSource, int packedLight) {
            if(entity.isBaby()) {
                poseStack.scale(0.4f, 0.4f, 0.4f);
            }

            super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        }
    }
