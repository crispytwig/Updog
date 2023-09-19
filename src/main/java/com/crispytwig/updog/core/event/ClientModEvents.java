package com.crispytwig.updog.core.event;

import com.crispytwig.updog.Updog;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Updog.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // PUG
        // event.registerEntityRenderer(EntityInit.PUG.get(), PugRenderer::new);
        // event.registerEntityRenderer(EntityInit.SHEEPDOG.get(), SheepdogRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // event.registerLayerDefinition(BeetleModel.LAYER_LOCATION, BeetleModel::createBodyLayer);
    }
}
