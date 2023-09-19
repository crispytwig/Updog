package com.crispytwig.updog.core.init;

import com.crispytwig.updog.Updog;
import com.crispytwig.updog.entity.Pug;
import com.crispytwig.updog.entity.Retriever;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntityInit {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Updog.MOD_ID);

    // Pug
    public static final RegistryObject<EntityType<Pug>> PUG =
            ENTITY_TYPES.register("pug",
                    () -> EntityType.Builder.of(Pug::new, MobCategory.CREATURE)
                            .sized(1.5f, 1.75f)
                            .build(new ResourceLocation(Updog.MOD_ID, "pug").toString()));

    // Retriever
    public static final RegistryObject<EntityType<Retriever>> RETRIEVER =
            ENTITY_TYPES.register("retriever",
                    () -> EntityType.Builder.of(Retriever::new, MobCategory.CREATURE)
                            .sized(1.5f, 1.75f)
                            .build(new ResourceLocation(Updog.MOD_ID, "retriever").toString()));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
