package org.orsa.multiworldInventories;

import net.minecraft.resources.Identifier;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;

public class ModComponents implements EntityComponentInitializer {

   public static final Identifier PER_WORLD_INV_ID = Identifier.fromNamespaceAndPath("multiworld-squid", "per_world_inventory");
   public static final ComponentKey<MultiworldInventoryComponent> PER_WORLD_INV_KEY = ComponentRegistryV3.INSTANCE.getOrCreate(PER_WORLD_INV_ID, MultiworldInventoryComponent.class);

   public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
      registry.registerForPlayers(
              PER_WORLD_INV_KEY,
              player -> new MultiworldInventoryComponent(),
              RespawnCopyStrategy.ALWAYS_COPY
      );
   }
}
