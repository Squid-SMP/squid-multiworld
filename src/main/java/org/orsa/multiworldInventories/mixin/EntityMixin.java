package org.orsa.multiworldInventories.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.ValueInput;
import org.orsa.multiworldInventories.ModComponents;
import org.orsa.multiworldInventories.MultiworldInventoryComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Entity.class, priority = 2000)
public class EntityMixin {

    @Inject(method = "load", at = @At("RETURN"))
    private void migrateComponentKey(ValueInput input, CallbackInfo ci) {
        if (!((Object) this instanceof ServerPlayer player)) {
            return;
        }

        MultiworldInventoryComponent component = ModComponents.PER_WORLD_INV_KEY.get(player);
        if (!component.isEmpty()) {
            return;
        }

        input.child("cardinal_components")
             .flatMap(cc -> cc.child("multiworld-inventories:per_world_inventory"))
             .ifPresent(component::readData);
    }
}
