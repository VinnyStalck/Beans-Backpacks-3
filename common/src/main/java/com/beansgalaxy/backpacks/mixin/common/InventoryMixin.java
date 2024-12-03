package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.components.ender.EnderTraits;
import com.beansgalaxy.backpacks.shorthand.ShortContainer;
import com.beansgalaxy.backpacks.shorthand.Shorthand;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.ItemStorageTraits;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.function.Predicate;

@Mixin(Inventory.class)
public abstract class InventoryMixin {
      @Shadow @Final public Player player;
      @Shadow @Final public NonNullList<ItemStack> items;
      @Shadow public int selected;

      @Unique public final BackData backData = (BackData) this;

      @Inject(method = "tick", at = @At("TAIL"))
      public void tickCarriedBackpack(CallbackInfo ci)
      {
            ItemStack carried = player.containerMenu.getCarried();
            Level level = player.level();
            Traits.get(carried).ifPresent(traits ->
                        carried.inventoryTick(level, player, -1, false)
            );
            backData.getShorthand().tick(instance);

            for (Slot slot : player.containerMenu.slots) {
                  ItemStack stack = slot.getItem();
                  EnderTraits.get(stack).ifPresent(enderTraits -> {
                        if (!enderTraits.isLoaded())
                              enderTraits.reload(level);

                        if (player instanceof ServerPlayer serverPlayer) {
                              enderTraits.addListener(serverPlayer);
                        }
                  });
            }
      }

      @Unique @Final public Inventory instance = (Inventory) (Object) this;

      @Inject(method = "add(Lnet/minecraft/world/item/ItemStack;)Z", at = @At(value = "HEAD"), cancellable = true)
      public void addToBackpackBeforeInventory(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
            if (!stack.isEmpty()) {
                  if (ShortContainer.Weapon.putBackLastStack(player, stack)) {
                        cir.setReturnValue(true);
                        return;
                  }

                  ItemStorageTraits.runIfEquipped(player, (traits, equipmentSlot) -> {
                        ItemStack backpack = player.getItemBySlot(equipmentSlot);
                        return traits.pickupToBackpack(player, equipmentSlot, instance, backpack, stack, cir);
                  });
            }
      }

      @Inject(method = "add(ILnet/minecraft/world/item/ItemStack;)Z", at = @At("RETURN"), cancellable = true)
      public void addToBackpackAfterInventory(int $$0, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
            if (!cir.getReturnValue()) {
                  ItemStorageTraits.runIfEquipped(player, (traits, equipmentSlot) ->
                              traits.overflowFromInventory(equipmentSlot, player, stack, cir)
                  );
            }
      }

      @Inject(method = "getDestroySpeed", at = @At("HEAD"), cancellable = true)
      private void getShorthandDestroySpeed(BlockState blockState, CallbackInfoReturnable<Float> cir) {
            if (selected >= items.size()) {
                  Shorthand shorthand = backData.getShorthand();
                  int slot = shorthand.getSelected(instance);
                  if (slot == -1)
                        return;

                  float destroySpeed = shorthand.getItem(slot).getDestroySpeed(blockState);
                  cir.setReturnValue(destroySpeed);
            }
      }

      @Inject(method = "getSelected", at = @At("HEAD"), cancellable = true)
      private void getShorthandSelected(CallbackInfoReturnable<ItemStack> cir) {
            if (selected >= items.size()) {
                  Shorthand shorthand = backData.getShorthand();
                  int slot = shorthand.getSelected(instance);
                  if (slot == -1)
                        return;

                  ItemStack stack = shorthand.getItem(slot);
                  if (stack.isEmpty())
                        shorthand.resetSelected(instance);
                  else
                        cir.setReturnValue(stack);
            }
      }

      @Inject(method = "replaceWith", at = @At("TAIL"))
      private void backpackReplaceWith(Inventory that, CallbackInfo ci) {
            backData.getShorthand().replaceWith(Shorthand.get(that));
      }

      @Inject(method = "dropAll", at = @At("TAIL"))
      private void shorthandDropAll(CallbackInfo ci) {
            Iterator<ItemStack> iterator = backData.getShorthand().getContent().iterator();
            while (iterator.hasNext()) {
                  ItemStack itemstack = iterator.next();
                  if (!itemstack.isEmpty())
                        player.drop(itemstack, true, false);
                  iterator.remove();
            }
      }

      @Inject(method = "contains(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("TAIL"), cancellable = true)
      private void shorthandContains(ItemStack pStack, CallbackInfoReturnable<Boolean> cir) {
            Iterable<ItemStack> contents = backData.getShorthand().getContent();
            for (ItemStack itemstack : contents) {
                  if (!itemstack.isEmpty() && ItemStack.isSameItemSameComponents(itemstack, pStack)) {
                        cir.setReturnValue(true);
                        return;
                  }
            }
      }

      @Inject(method = "contains(Lnet/minecraft/tags/TagKey;)Z", at = @At("TAIL"), cancellable = true)
      private void shorthandContains(TagKey<Item> pTag, CallbackInfoReturnable<Boolean> cir) {
            Iterable<ItemStack> contents = backData.getShorthand().getContent();
            for (ItemStack itemstack : contents) {
                  if (!itemstack.isEmpty() && itemstack.is(pTag)) {
                        cir.setReturnValue(true);
                        return;
                  }
            }
      }

      @Inject(method = "contains(Ljava/util/function/Predicate;)Z", at = @At("TAIL"), cancellable = true)
      private void shorthandContains(Predicate<ItemStack> pPredicate, CallbackInfoReturnable<Boolean> cir) {
            Iterable<ItemStack> contents = backData.getShorthand().getContent();
            for (ItemStack itemstack : contents) {
                  if (pPredicate.test(itemstack)) {
                        cir.setReturnValue(true);
                        return;
                  }
            }
      }

      @Inject(method = "removeItem(Lnet/minecraft/world/item/ItemStack;)V", at = @At("TAIL"), cancellable = true)
      private void shorthandContains(ItemStack pStack, CallbackInfo ci) {
            Iterator<ItemStack> iterator = backData.getShorthand().getContent().iterator();
            while (iterator.hasNext()) {
                  if (iterator.next() == pStack) {
                        iterator.remove();
                        ci.cancel();
                        return;
                  }
            }
      }

      @Inject(method = "clearContent", at = @At("TAIL"))
      private void shorthandClearContent(CallbackInfo ci) {
            backData.getShorthand().clearContent();
      }
}
