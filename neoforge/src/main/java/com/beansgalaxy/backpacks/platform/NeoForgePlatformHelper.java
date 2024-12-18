package com.beansgalaxy.backpacks.platform;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.NeoForgeClient;
import com.beansgalaxy.backpacks.network.Network2C;
import com.beansgalaxy.backpacks.network.Network2S;
import com.beansgalaxy.backpacks.network.clientbound.Packet2C;
import com.beansgalaxy.backpacks.network.serverbound.Packet2S;
import com.beansgalaxy.backpacks.platform.services.IPlatformHelper;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.battery.BatteryCodecs;
import com.beansgalaxy.backpacks.traits.battery.BatteryTraits;
import com.beansgalaxy.backpacks.traits.bucket.BucketCodecs;
import com.beansgalaxy.backpacks.traits.bucket.BucketTraits;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.atlas.sources.PalettedPermutations;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Main;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.moddiscovery.locators.NeoForgeDevProvider;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeConfig;
import net.neoforged.neoforge.common.NeoForgeEventHandler;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.nio.file.Path;
import java.util.function.Supplier;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public void send(Network2C network, Packet2C packet2C, ServerPlayer to) {
        PacketDistributor.sendToPlayer(to, packet2C);
    }

    @Override
    public void send(Network2C network, Packet2C packet2C, MinecraftServer server) {
        PacketDistributor.sendToAllPlayers(packet2C);
    }

    @Override
    public void send(Network2C network, Packet2C packet2C, MinecraftServer server, ServerPlayer player) {
        PacketDistributor.sendToPlayersTrackingEntity(player, packet2C);
    }

    @Override
    public void send(Network2S network, Packet2S packet2S) {
        PacketDistributor.sendToServer(packet2S);
    }

    public static final DeferredRegister.Items ITEMS_REGISTRY = DeferredRegister.createItems(Constants.MOD_ID);

    @Override
    public Supplier<Item> register(String name, Supplier<Item> item) {
        DeferredRegister.Items items = ITEMS_REGISTRY;
        return items.register(name, item);
    }

    public static final DeferredRegister.DataComponents COMPONENTS_REGISTRY =
                DeferredRegister.createDataComponents(Constants.MOD_ID);

    @Override
    public <T> DataComponentType<T> register(String name, DataComponentType<T> type) {
        DeferredRegister.DataComponents components = COMPONENTS_REGISTRY;
        components.register(name, () -> type);
        return type;
    }

    public static final DeferredRegister<EntityType<?>> ENTITY_REGISTRY =
                DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Constants.MOD_ID);

    public <T extends Entity> Supplier<EntityType<T>> register(String name, EntityType.Builder<T> type) {
        DeferredRegister<EntityType<?>> registry = ENTITY_REGISTRY;
        return registry.register(name, () -> type.build(name));
    }

    public static final DeferredRegister<SoundEvent> SOUND_REGISTRY =
                DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, Constants.MOD_ID);

    @Override
    public SoundEvent register(String name, SoundEvent event) {
        DeferredRegister<SoundEvent> registry = SOUND_REGISTRY;
        registry.register(name, () -> event);
        return event;
    }

    public static final DeferredRegister<Attribute> ATTRIBUTE_REGISTRY =
                DeferredRegister.create(BuiltInRegistries.ATTRIBUTE, Constants.MOD_ID);

    @Override
    public Holder<Attribute> register(String name, Attribute attribute) {
        return ATTRIBUTE_REGISTRY.register(name, () -> attribute);
    }

    @Override
    public ModelResourceLocation getModelVariant(ResourceLocation location) {
        return ModelResourceLocation.standalone(location);
    }

    @Override
    public Path getConfigPath() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public TraitComponentKind<BucketTraits> registerBucket() {
        return TraitComponentKind.register(BucketTraits.NAME, BucketCodecs.INSTANCE);
    }

    @Override
    public TraitComponentKind<BatteryTraits> registerBattery() {
        return TraitComponentKind.register(BatteryTraits.NAME, BatteryCodecs.INSTANCE);
    }
}