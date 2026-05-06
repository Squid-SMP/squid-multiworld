package org.orsa.multiworldInventories;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiworldInventories implements ModInitializer {
   public static final String MOD_ID = "MultiworldInventories";
   public static final Logger LOGGER = LoggerFactory.getLogger("MultiworldInventories");

   public void onInitialize() {
      AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);

      ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(MultiworldInventories::afterPlayerChangeWorld);
   }

   private static void afterPlayerChangeWorld(ServerPlayer player, ServerLevel origin, ServerLevel destination) {
      MultiworldInventoryComponent component = ModComponents.PER_WORLD_INV_KEY.get(player);

      var originDimension = getInventoryDim(origin.dimension());
      component.saveInventory(originDimension, player);

      var destinationDimension = getInventoryDim(destination.dimension());
      component.loadInventory(destinationDimension, player);
   }

   public static ResourceKey<Level> getInventoryDim(ResourceKey<Level> level) {
      ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

      if (config.netherSharesInventory && level == Level.NETHER) {
         return Level.OVERWORLD;
      }
      else if (config.theendSharesInventory && level == Level.END) {
         return Level.OVERWORLD;
      }

      return level;
   }
}
