package com.crispytwig.updog.client.renderer;

import com.crispytwig.updog.Updog;
import com.crispytwig.updog.client.model.PugModel;
import com.crispytwig.updog.client.renderer.layer.PugCollarLayer;
import com.crispytwig.updog.client.renderer.layer.RetrieverCollarLayer;
import com.crispytwig.updog.entity.Pug;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PugRenderer extends GeoEntityRenderer<Pug> {
        public PugRenderer(EntityRendererProvider.Context renderManager) {
            super(renderManager, new PugModel());

            addRenderLayer(new PugCollarLayer(this));
        }

        @Override
        public void render(Pug entity, float entityYaw, float partialTick, PoseStack poseStack,
                           MultiBufferSource bufferSource, int packedLight) {
            if(entity.isBaby()) {
                poseStack.scale(0.4f, 0.4f, 0.4f);
            }

            super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        }
    }
