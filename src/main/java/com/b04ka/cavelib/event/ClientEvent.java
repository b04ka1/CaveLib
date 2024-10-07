package com.b04ka.cavelib.event;

import com.b04ka.cavelib.biome.BiomeSampler;
import com.b04ka.cavelib.biome.CaveBiomeVisulals;
import com.b04ka.cavelib.proxy.ClientProxy;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;


public class ClientEvent {
    private static float lastSampledFogNearness = 0.0F;
    private static float lastSampledWaterFogFarness = 0.0F;
    private static Vec3 lastSampledFogColor = Vec3.ZERO;
    private static Vec3 lastSampledWaterFogColor = Vec3.ZERO;

    public static float getLastSampledFogNearness() {
        return lastSampledFogNearness;
    }

    public static float getLastSampledWaterFogFarness() {
        return lastSampledWaterFogFarness;
    }

    public static Vec3 getLastSampledFogColor() {
        return lastSampledFogColor;
    }

    public static Vec3 getLastSampledWaterFogColor() {
        return lastSampledWaterFogColor;
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event){
            Entity cameraEntity = Minecraft.getInstance().cameraEntity;
                if (cameraEntity!=null){
                    ClientProxy.clSkyOverrideAmount = CaveBiomeVisulals.calculateBiomeSkyOverride(cameraEntity);
                    if (ClientProxy.clSkyOverrideAmount > 0) {
                        ClientProxy.clSkyOverrideColor = BiomeSampler.sampleBiomesVec3(Minecraft.getInstance().level, Minecraft.getInstance().cameraEntity.position(), biomeHolder -> Vec3.fromRGB24(biomeHolder.value().getSkyColor()));
                    }
                    ClientProxy.lastBiomeAmbientLightAmountPrev = ClientProxy.lastBiomeAmbientLightAmount;
                    ClientProxy.lastBiomeAmbientLightAmount = calculateBiomeAmbientLight(cameraEntity);
                    ClientProxy.lastBiomeLightColorPrev = ClientProxy.lastBiomeLightColor;
                    ClientProxy.lastBiomeLightColor = calculateBiomeLightColor(cameraEntity);
                    lastSampledFogNearness = calculateBiomeFogNearness(cameraEntity);
                    lastSampledWaterFogFarness = calculateBiomeWaterFogFarness(cameraEntity);
                    if (cameraEntity.level() instanceof ClientLevel) { //fixes crash with beholder
                        lastSampledFogColor = calculateBiomeFogColor(cameraEntity);
                        lastSampledWaterFogColor = calculateBiomeWaterFogColor(cameraEntity);
                    }
                }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void fogRender(ViewportEvent.RenderFog event) {
        if (event.isCanceled()) {
            //another mod has cancelled fog rendering.
            return;
        }
        //some mods incorrectly set the RenderSystem fog start and end directly, so this will have to do as a band-aid...
        float defaultFarPlaneDistance = RenderSystem.getShaderFogEnd();
        float defaultNearPlaneDistance = RenderSystem.getShaderFogStart();
        if (event.getCamera().getFluidInCamera() == FogType.WATER) {
            float farness = lastSampledWaterFogFarness;
            if (farness != 1.0F) {
                event.setCanceled(true);
                event.setFarPlaneDistance(defaultFarPlaneDistance * farness);
            }
        } else if (event.getMode() == FogRenderer.FogMode.FOG_TERRAIN) {
            float nearness = lastSampledFogNearness;
            boolean flag = Math.abs(nearness) - 1.0F < 0.01F;
            if (flag) {
                event.setCanceled(true);
                event.setNearPlaneDistance(defaultNearPlaneDistance * nearness);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void fogColor(ViewportEvent.ComputeFogColor event) {
        Entity player = Minecraft.getInstance().player;
        BlockState blockState = player.level().getBlockState(event.getCamera().getBlockPosition());
            if (event.getCamera().getFluidInCamera() == FogType.NONE) {
            float override = ClientProxy.clSkyOverrideAmount;
            float setR = event.getRed();
            float setG = event.getGreen();
            float setB = event.getBlue();

            boolean flag = false;
            if (override != 0.0F) {
                flag = true;
                Vec3 vec3 = lastSampledFogColor;
                setR = (float) (vec3.x - setR) * override + setR;
                setG = (float) (vec3.y - setG) * override + setG;
                setB = (float) (vec3.z - setB) * override + setB;
            }
            if (flag) {
                event.setRed(setR);
                event.setGreen(setG);
                event.setBlue(setB);
            }
        } else if (event.getCamera().getFluidInCamera() == FogType.WATER) {
            int i = Minecraft.getInstance().options.biomeBlendRadius().get();
            float override = ClientProxy.clSkyOverrideAmount;
            if (override != 0) {
                Vec3 vec3 = lastSampledWaterFogColor;
                event.setRed((float) (event.getRed() + (vec3.x - event.getRed()) * override));
                event.setGreen((float) (event.getGreen() + (vec3.y - event.getGreen()) * override));
                event.setBlue((float) (event.getBlue() + (vec3.z - event.getBlue()) * override));
            }
        }
    }

    private static float calculateBiomeAmbientLight(Entity player) {
        int i = Minecraft.getInstance().options.biomeBlendRadius().get();
        if (i == 0) {
            return CaveBiomeVisulals.getBiomeAmbientLight(player.level().getBiome(player.blockPosition()));
        } else {
            return BiomeSampler.sampleBiomesFloat(player.level(), player.position(), CaveBiomeVisulals::getBiomeAmbientLight);
        }
    }

    private static float calculateBiomeFogNearness(Entity player) {
        int i = Minecraft.getInstance().options.biomeBlendRadius().get();
        float nearness;
        if (i == 0) {
            nearness = CaveBiomeVisulals.getBiomeFogNearness(player.level().getBiome(player.blockPosition()));
        } else {
            nearness = BiomeSampler.sampleBiomesFloat(player.level(), player.position(), CaveBiomeVisulals::getBiomeFogNearness);
        }
        return nearness;
    }

    private static float calculateBiomeWaterFogFarness(Entity player) {
        int i = Minecraft.getInstance().options.biomeBlendRadius().get();
        float farness;
        if (i == 0) {
            farness = CaveBiomeVisulals.getBiomeWaterFogFarness(player.level().getBiome(player.blockPosition()));
        } else {
            farness = BiomeSampler.sampleBiomesFloat(player.level(), player.position(), CaveBiomeVisulals::getBiomeWaterFogFarness);
        }
        return farness;
    }

    private static Vec3 calculateBiomeLightColor(Entity player) {
        int i = Minecraft.getInstance().options.biomeBlendRadius().get();
        if (i == 0) {
            return CaveBiomeVisulals.getBiomeLightColorOverride(player.level().getBiome(player.blockPosition()));
        } else {
            return BiomeSampler.sampleBiomesVec3(player.level(), player.position(), CaveBiomeVisulals::getBiomeLightColorOverride);
        }
    }

    private static Vec3 calculateBiomeFogColor(Entity player) {
        int i = Minecraft.getInstance().options.biomeBlendRadius().get();
        Vec3 vec3;
        if (i == 0) {
            vec3 = ((ClientLevel) player.level()).effects().getBrightnessDependentFogColor(Vec3.fromRGB24(player.level().getBiomeManager().getNoiseBiomeAtPosition(player.blockPosition()).value().getFogColor()), 1.0F);
        } else {
            vec3 = ((ClientLevel) player.level()).effects().getBrightnessDependentFogColor(BiomeSampler.sampleBiomesVec3(player.level(), player.position(), biomeHolder -> Vec3.fromRGB24(biomeHolder.value().getFogColor())), 1.0F);
        }
        return vec3;
    }

    private Vec3 calculateBiomeWaterFogColor(Entity player) {
        int i = Minecraft.getInstance().options.biomeBlendRadius().get();
        Vec3 vec3;
        if (i == 0) {
            vec3 = ((ClientLevel) player.level()).effects().getBrightnessDependentFogColor(Vec3.fromRGB24(player.level().getBiomeManager().getNoiseBiomeAtPosition(player.blockPosition()).value().getWaterFogColor()), 1.0F);
        } else {
            vec3 = ((ClientLevel) player.level()).effects().getBrightnessDependentFogColor(BiomeSampler.sampleBiomesVec3(player.level(), player.position(), biomeHolder -> Vec3.fromRGB24(biomeHolder.value().getWaterFogColor())), 1.0F);
        }
        return vec3;
    }
}
