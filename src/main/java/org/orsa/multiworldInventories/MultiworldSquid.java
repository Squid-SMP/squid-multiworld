package org.orsa.multiworldInventories;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiworldSquid implements ModInitializer {
   public static final String MOD_ID = "MultiworldSquid";
   public static final Logger LOGGER = LoggerFactory.getLogger("MultiworldSquid");

   public void onInitialize() {
      AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);

      ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register(MultiworldSquid::afterPlayerChangeWorld);
   }

   private static void afterPlayerChangeWorld(ServerPlayer player, ServerLevel origin, ServerLevel destination) {
      MultiworldInventoryComponent component = ModComponents.PER_WORLD_INV_KEY.get(player);

      var originDimension = getInventoryDim(origin.dimension());
      component.saveInventory(originDimension, player);

      var destinationDimension = getInventoryDim(destination.dimension());
      component.loadInventory(destinationDimension, player);

      enforceGamemode(player, destination);
   }

   private static void enforceGamemode(ServerPlayer player, ServerLevel destination) {
      ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
      if (config.dimensionGamemodes == null) {
         return;
      }

      ResourceKey<Level> dimensionKey = destination.dimension();
      String dimensionId = dimensionKey.identifier().toString();
      List<String> rawList = config.dimensionGamemodes.get(dimensionId);
      if (rawList == null || rawList.isEmpty()) {
         return;
      }

      if (hasGamemodeBypassPermission(player)) {
         return;
      }

      List<GameType> allowed = new ArrayList<>();
      for (String entry : rawList) {
         GameType gameType = GameType.byName(entry, null);
         if (gameType == null) {
            LOGGER.warn("[multiworld-squid] Unknown gamemode '{}' in dimensionGamemodes for '{}'", entry, dimensionId);
         }
         else {
            allowed.add(gameType);
         }
      }
      if (allowed.isEmpty()) {
         return;
      }

      GameType currentGameMode = player.gameMode.getGameModeForPlayer();
      if (!allowed.contains(currentGameMode)) {
         player.setGameMode(allowed.get(0));
      }
   }

   private static boolean hasGamemodeBypassPermission(ServerPlayer player) {
      FabricLoader fabricLoader = FabricLoader.getInstance();
      if (!fabricLoader.isModLoaded("luckperms")) {
         return false;
      }
      try {
         LuckPerms luckPerms = LuckPermsProvider.get();
         var userManager = luckPerms.getUserManager();
         User user = userManager.getUser(player.getUUID());
         if (user == null) {
            return false;
         }
         var cachedData = user.getCachedData();
         var permissionData = cachedData.getPermissionData();
         var permissionResult = permissionData.checkPermission("multiworld-squid.bypass-gamemode");
         return permissionResult.asBoolean();
      }
      catch (Exception e) {
         LOGGER.warn("[multiworld-squid] Failed to check LuckPerms bypass permission", e);
         return false;
      }
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
