package org.orsa.multiworldInventories;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.ladysnake.cca.api.v3.component.ComponentV3;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public class MultiworldInventoryComponent implements ComponentV3, AutoSyncedComponent {
   private final Map<Identifier, PlayerInventorySnapshot> inventories = new HashMap<>();
   private static final Codec<ItemStack> STACK_CODEC = ItemStack.CODEC
      .optionalFieldOf("item")
      .xmap(opt -> opt.orElse(ItemStack.EMPTY), stack -> stack.isEmpty() ? Optional.empty() : Optional.of(stack))
      .codec();
   private static final Codec<List<ItemStack>> STACK_LIST_CODEC = STACK_CODEC.listOf();

   public void saveInventory(ResourceKey<Level> levelKey, ServerPlayer player) {
      var inventory = player.getInventory();
      inventories.put(levelKey.identifier(), new PlayerInventorySnapshot(
         inventory.items.stream().map(ItemStack::copy).toList(),
         List.of(
            player.getItemBySlot(EquipmentSlot.HEAD).copy(),
            player.getItemBySlot(EquipmentSlot.CHEST).copy(),
            player.getItemBySlot(EquipmentSlot.LEGS).copy(),
            player.getItemBySlot(EquipmentSlot.FEET).copy()
         ),
         List.of(player.getItemBySlot(EquipmentSlot.OFFHAND).copy())
      ));
   }

   public void loadInventory(ResourceKey<Level> levelKey, ServerPlayer player) {
      PlayerInventorySnapshot snapshot = inventories.get(levelKey.identifier());
      var inventory = player.getInventory();

      if (snapshot != null) {
         for (int i = 0; i < snapshot.items().size() && i < inventory.items.size(); i++){
            inventory.items.set(i, snapshot.items().get(i).copy());
         }

         if (snapshot.armor().size() >= 4) {
            player.setItemSlot(EquipmentSlot.HEAD,  snapshot.armor().get(0).copy());
            player.setItemSlot(EquipmentSlot.CHEST, snapshot.armor().get(1).copy());
            player.setItemSlot(EquipmentSlot.LEGS,  snapshot.armor().get(2).copy());
            player.setItemSlot(EquipmentSlot.FEET,  snapshot.armor().get(3).copy());
         }

         if (!snapshot.offhand().isEmpty()) {
            player.setItemSlot(EquipmentSlot.OFFHAND, snapshot.offhand().getFirst().copy());
         }
      }
      else {
         inventory.clearContent();
         var empty = ItemStack.EMPTY;

         player.setItemSlot(EquipmentSlot.HEAD,    empty);
         player.setItemSlot(EquipmentSlot.CHEST,   empty);
         player.setItemSlot(EquipmentSlot.LEGS,    empty);
         player.setItemSlot(EquipmentSlot.FEET,    empty);
         player.setItemSlot(EquipmentSlot.OFFHAND, empty);
      }
   }

   public boolean isEmpty() {
      return inventories.isEmpty();
   }

   public void writeData(ValueOutput out) {
      Map<String, PlayerInventorySnapshot> rawHashMap = new HashMap<>();
      inventories.forEach((id, snapshot) -> rawHashMap.put(id.toString(), snapshot));
      out.store("inventories", PlayerInventorySnapshot.INVENTORY_MAP_CODEC, rawHashMap);
   }

   public void readData(ValueInput in) {
      inventories.clear();
      Map<String, PlayerInventorySnapshot> raw = in.read("inventories", PlayerInventorySnapshot.INVENTORY_MAP_CODEC)
         .orElseGet(Map::of);
      raw.forEach((key, snapshot) -> inventories.put(Identifier.parse(key), snapshot));
   }

   public boolean isRequiredOnClient() {
      return false;
   }

   public record PlayerInventorySnapshot(List<ItemStack> items, List<ItemStack> armor, List<ItemStack> offhand) {
      public static final Codec<PlayerInventorySnapshot> SNAPSHOT_CODEC = RecordCodecBuilder.create(
         instance -> instance.group(
                  MultiworldInventoryComponent.STACK_LIST_CODEC.fieldOf("items").forGetter(PlayerInventorySnapshot::items),
                  MultiworldInventoryComponent.STACK_LIST_CODEC.fieldOf("armor").forGetter(PlayerInventorySnapshot::armor),
                  MultiworldInventoryComponent.STACK_LIST_CODEC.fieldOf("offhand").forGetter(PlayerInventorySnapshot::offhand)
               )
               .apply(instance, PlayerInventorySnapshot::new)
      );

      static final Codec<Map<String, PlayerInventorySnapshot>> INVENTORY_MAP_CODEC = Codec.unboundedMap(
         Codec.STRING, SNAPSHOT_CODEC
      );
   }
}
