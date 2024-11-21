package com.beansgalaxy.backpacks.shorthand.storage;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.OptionalInt;

public abstract class ShortContainer implements Container {
      protected final Int2ObjectArrayMap<ItemStack> stacks;
      protected final String name;

      public String getName() {
            return name;
      }

      public ShortContainer(String name) {
            this.name = name;
            Int2ObjectArrayMap<ItemStack> map = new Int2ObjectArrayMap<>();
            map.defaultReturnValue(ItemStack.EMPTY);
            this.stacks = map;
      }

      public abstract int size();

      public int getMaxSlot() {
            OptionalInt max = stacks.int2ObjectEntrySet().stream().mapToInt(entry ->
                        entry.getValue().isEmpty()
                                    ? 0 : entry.getIntKey()
            ).max();
            return max.orElse(0);
      }

      @Override
      public int getContainerSize() {
            int maxSlot = getMaxSlot();
            return Math.max(size(), maxSlot + 1);
      }

      @Override
      public boolean isEmpty() {
            return stacks.int2ObjectEntrySet().stream().allMatch(stack -> stack.getValue().isEmpty());
      }

      @Override
      public ItemStack getItem(int slot) {
            return stacks.get(slot);
      }

      @Override
      public ItemStack removeItem(int slot, int amount) {
            ItemStack stack = getItem(slot);
            ItemStack split = stack.getCount() > amount
                        ? stack.split(amount)
                        : removeItemNoUpdate(slot);

            return split;
      }

      @Override
      public ItemStack removeItemNoUpdate(int slot) {
            return stacks.remove(slot);
      }

      @Override
      public void setItem(int slot, ItemStack stack) {
            if (stack.isEmpty())
                  stacks.remove(slot);

            stacks.put(slot, stack);
      }

      @Override
      public void setChanged() {

      }

      @Override
      public boolean stillValid(Player player) {
            return !player.isRemoved();
      }

      @Override
      public void clearContent() {
            stacks.clear();
      }

      public void save(CompoundTag tag, RegistryAccess access) {
            CompoundTag container = new CompoundTag();
            stacks.forEach((slot, tool) -> {
                  if (tool.isEmpty())
                        return;

                  RegistryOps<Tag> serializationContext = access.createSerializationContext(NbtOps.INSTANCE);
                  container.put(String.valueOf(slot), ItemStack.CODEC.encodeStart(serializationContext, tool).getOrThrow());
            });

            tag.put(name, container);
      }

      public void load(CompoundTag tag, RegistryAccess access) {
            CompoundTag shorthand = tag.getCompound(name);
            for (String allKey : shorthand.getAllKeys()) {
                  CompoundTag slot = shorthand.getCompound(allKey);
                  int index = Integer.parseInt(allKey);
                  RegistryOps<Tag> serializationContext = access.createSerializationContext(NbtOps.INSTANCE);
                  ItemStack stack = ItemStack.OPTIONAL_CODEC.parse(serializationContext, slot).getOrThrow();
                  setItem(index, stack);
            }
      }

}