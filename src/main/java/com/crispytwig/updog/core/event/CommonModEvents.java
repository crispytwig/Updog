package com.crispytwig.updog.core.event;

import com.crispytwig.updog.Updog;
import com.crispytwig.updog.core.init.EntityInit;
import com.crispytwig.updog.entity.Pug;
import com.crispytwig.updog.entity.Retriever;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Updog.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonModEvents {
    @SubscribeEvent
    public static void entityAttributeEvent(EntityAttributeCreationEvent event) {
        event.put(EntityInit.PUG.get(), Pug.setAttributes());
        event.put(EntityInit.RETRIEVER.get(), Retriever.setAttributes());
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        // event.register(EntityInit.DWELLER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.WORLD_SURFACE, Dweller::canSpawn, SpawnPlacementRegisterEvent.Operation.OR);
        // OR specifies "that placement OR this placement" if a placement already exists.
    }
}
