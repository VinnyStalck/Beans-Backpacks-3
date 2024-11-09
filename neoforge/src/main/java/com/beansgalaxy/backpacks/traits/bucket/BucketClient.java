package com.beansgalaxy.backpacks.traits.bucket;

import com.beansgalaxy.backpacks.NeoForgeMain;
import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.beansgalaxy.backpacks.util.Tint;
import com.beansgalaxy.backpacks.util.TraitTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class BucketClient implements IClientTraits {
      static final BucketClient INSTANCE = new BucketClient();

      @Override
      public void renderTooltip(GenericTraits trait, ItemStack itemStack, PatchedComponentHolder holder, GuiGraphics gui, int mouseX, int mouseY, CallbackInfo ci) {
            if (!trait.isEmpty(holder)) {
                  FluidStack fluidStack = holder.getOrDefault(NeoForgeMain.DATA_FLUID, FluidStack.EMPTY);
                  Component name = fluidStack.getHoverName();
                  TraitTooltip<?> tooltip = new TraitTooltip<>(trait, itemStack, holder, name);
                  gui.renderTooltip(Minecraft.getInstance().font, List.of(name), Optional.of(tooltip), mouseX, mouseY);
                  ci.cancel();
            }
      }

      @Override
      public void getBarWidth(GenericTraits trait, PatchedComponentHolder holder, CallbackInfoReturnable<Integer> cir) {
            Fraction fullness = trait.fullness(holder);
            if (fullness.equals(Fraction.ONE))
                  cir.setReturnValue(14);
            else {
                  float value = fullness.multiplyBy(Fraction.getFraction(13, 1)).floatValue();
                  cir.setReturnValue(Mth.floor(value) + 1);
            }
      }

      @Override
      public void getBarColor(GenericTraits trait, PatchedComponentHolder holder, CallbackInfoReturnable<Integer> cir) {
            if (trait.fullness(holder).equals(Fraction.ONE))
                  cir.setReturnValue(Mth.color(0.9F, 0.2F, 0.3F));
            else
                  cir.setReturnValue(Mth.color(0.4F, 0.4F, 1.0F));
      }

      @Override
      public <T extends GenericTraits> ClientTooltipComponent getTooltipComponent(TraitTooltip<T> tooltip) {
            T traits = tooltip.traits();
            if (traits instanceof BucketTraits trait) {
                  FluidStack stack = tooltip.getOrDefault(NeoForgeMain.DATA_FLUID, FluidStack.EMPTY);
                  IClientFluidTypeExtensions type = IClientFluidTypeExtensions.of(stack.getFluidType());
                  int amount = stack.getAmount();

                  ResourceLocation texture = type.getStillTexture(stack);
                  Tint tint = new Tint(type.getTintColor(stack));
                  int buckets = amount / FluidType.BUCKET_VOLUME;
                  int bottles = amount % FluidType.BUCKET_VOLUME / 250;
                  int droplets = amount % FluidType.BUCKET_VOLUME % 250;
                  return new BucketTooltip(tooltip.itemStack(), tooltip.title(), texture, tint, buckets, bottles, droplets);
            }
            return null;
      }

      @Override
      public void appendEquipmentLines(GenericTraits traits, Consumer<Component> pTooltipAdder) {
            BucketTraits bucketTraits = (BucketTraits) traits;
            long size = bucketTraits.size();
            pTooltipAdder.accept(Component.translatable("traits.beansbackpacks.equipment." + traits.name() + (size == 1 ? ".solo" : ".size"), size).withStyle(ChatFormatting.GOLD));
      }

      @Override
      public void appendTooltipLines(GenericTraits traits, List<Component> lines) {
            BucketTraits bucketTraits = (BucketTraits) traits;
            long size = bucketTraits.size();
            lines.add(Component.translatable("traits.beansbackpacks.inventory." + traits.name() + (size == 1 ? ".solo" : ".size"), size).withStyle(ChatFormatting.GOLD));
      }
}
