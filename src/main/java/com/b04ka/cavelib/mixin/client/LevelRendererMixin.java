package com.b04ka.cavelib.mixin.client;

import com.b04ka.cavelib.CaveLib;
import com.b04ka.cavelib.proxy.ClientProxy;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(value = LevelRenderer.class, priority = 800)
public abstract class LevelRendererMixin {

    @Shadow
    private ClientLevel level;
    @Shadow
    private int ticks;

    @Shadow @Final private Minecraft minecraft;

    @Shadow protected abstract void renderEndSky(PoseStack p_109781_);

    @Shadow protected abstract boolean doesMobEffectBlockSky(Camera p_234311_);

    @Shadow @Nullable private VertexBuffer skyBuffer;

    @Shadow @Final private static ResourceLocation SUN_LOCATION;

    @Shadow @Final private static ResourceLocation MOON_LOCATION;

    @Shadow @Nullable private VertexBuffer starBuffer;

    @Shadow @Nullable private VertexBuffer darkBuffer;

    @Inject(method = "Lnet/minecraft/client/renderer/LevelRenderer;renderSky",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true)
    //have to completely override this method for compatibility reasons
    public void cl_renderSky(Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTick, Camera camera, boolean isFoggy, Runnable skyFogSetup, CallbackInfo ci) {
        float override = ClientProxy.clSkyOverrideAmount;
        if(override <= 0.0F){
            return;
        }
        ci.cancel();
        if (!this.level.effects().renderSky(this.level, this.ticks, partialTick, frustumMatrix, camera, projectionMatrix, isFoggy, skyFogSetup)) {
            skyFogSetup.run();
            if (!isFoggy) {
                FogType fogtype = camera.getFluidInCamera();
                if (fogtype != FogType.POWDER_SNOW && fogtype != FogType.LAVA && !this.doesMobEffectBlockSky(camera)) {
                    PoseStack posestack = new PoseStack();
                    posestack.mulPose(frustumMatrix);
                    if (this.minecraft.level.effects().skyType() == DimensionSpecialEffects.SkyType.END) {
                        this.renderEndSky(posestack);
                    } else if (this.minecraft.level.effects().skyType() == DimensionSpecialEffects.SkyType.NORMAL) {
                        Vec3 vec3 = this.level.getSkyColor(this.minecraft.gameRenderer.getMainCamera().getPosition(), partialTick);
                        float f = (float)vec3.x;
                        float f1 = (float)vec3.y;
                        float f2 = (float)vec3.z;
                        FogRenderer.levelFogColor();
                        Tesselator tesselator = Tesselator.getInstance();
                        RenderSystem.depthMask(false);
                        RenderSystem.setShaderColor(f, f1, f2, 1.0F);
                        ShaderInstance shaderinstance = RenderSystem.getShader();
                        this.skyBuffer.bind();
                        this.skyBuffer.drawWithShader(posestack.last().pose(), projectionMatrix, shaderinstance);
                        VertexBuffer.unbind();
                        RenderSystem.enableBlend();
                        float[] afloat = this.level.effects().getSunriseColor(this.level.getTimeOfDay(partialTick), partialTick);
                        float f11;
                        float f12;
                        float f7;
                        float f8;
                        float f9;

                        if (afloat != null && afloat.length >= 4) {
                            afloat[3] = afloat[3] * (1F - override);
                        }

                        if (afloat != null) {
                            RenderSystem.setShader(GameRenderer::getPositionColorShader);
                            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                            posestack.pushPose();
                            posestack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90.0F));
                            f11 = Mth.sin(this.level.getSunAngle(partialTick)) < 0.0F ? 180.0F : 0.0F;
                            posestack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(f11));
                            posestack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(90.0F));
                            float f4 = afloat[0];
                            f12 = afloat[1];
                            float f6 = afloat[2];
                            Matrix4f matrix4f = posestack.last().pose();
                            BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
                            bufferbuilder.addVertex(matrix4f, 0.0F, 100.0F, 0.0F).setColor(f4, f12, f6, afloat[3]);


                            for(int j = 0; j <= 16; ++j) {
                                f7 = (float)j * 6.2831855F / 16.0F;
                                f8 = Mth.sin(f7);
                                f9 = Mth.cos(f7);
                                bufferbuilder.addVertex(matrix4f, f8 * 120.0F, f9 * 120.0F, -f9 * 40.0F * afloat[3]).setColor(afloat[0], afloat[1], afloat[2], 0.0F);
                            }

                            BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
                            posestack.popPose();
                        }

                        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                        posestack.pushPose();
                        float rainLevel = this.level.getRainLevel(partialTick);
                        rainLevel = Math.max(override, rainLevel);
                        f11 = 1.0F - rainLevel;
                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, f11);
                        posestack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90.0F));
                        posestack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(this.level.getTimeOfDay(partialTick) * 360.0F));
                        Matrix4f matrix4f1 = posestack.last().pose();
                        f12 = 30.0F;
                        RenderSystem.setShader(GameRenderer::getPositionTexShader);
                        RenderSystem.setShaderTexture(0, SUN_LOCATION);
                        BufferBuilder bufferbuilder1 = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                        bufferbuilder1.addVertex(matrix4f1, -f12, 100.0F, -f12).setUv(0.0F, 0.0F);
                        bufferbuilder1.addVertex(matrix4f1, f12, 100.0F, -f12).setUv(1.0F, 0.0F);
                        bufferbuilder1.addVertex(matrix4f1, f12, 100.0F, f12).setUv(1.0F, 1.0F);
                        bufferbuilder1.addVertex(matrix4f1, -f12, 100.0F, f12).setUv(0.0F, 1.0F);
                        BufferUploader.drawWithShader(bufferbuilder1.buildOrThrow());
                        f12 = 20.0F;
                        RenderSystem.setShaderTexture(0, MOON_LOCATION);
                        int k = this.level.getMoonPhase();
                        int l = k % 4;
                        int i1 = k / 4 % 2;
                        float f13 = (float)(l + 0) / 4.0F;
                        f7 = (float)(i1 + 0) / 2.0F;
                        f8 = (float)(l + 1) / 4.0F;
                        f9 = (float)(i1 + 1) / 2.0F;
                        bufferbuilder1 = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                        bufferbuilder1.addVertex(matrix4f1, -f12, -100.0F, f12).setUv(f8, f9);
                        bufferbuilder1.addVertex(matrix4f1, f12, -100.0F, f12).setUv(f13, f9);
                        bufferbuilder1.addVertex(matrix4f1, f12, -100.0F, -f12).setUv(f13, f7);
                        bufferbuilder1.addVertex(matrix4f1, -f12, -100.0F, -f12).setUv(f8, f7);
                        BufferUploader.drawWithShader(bufferbuilder1.buildOrThrow());
                        float f10 = this.level.getStarBrightness(partialTick) * f11;
                        if (f10 > 0.0F) {
                            RenderSystem.setShaderColor(f10, f10, f10, f10);
                            FogRenderer.setupNoFog();
                            this.starBuffer.bind();
                            this.starBuffer.drawWithShader(posestack.last().pose(), projectionMatrix, GameRenderer.getPositionShader());
                            VertexBuffer.unbind();
                            skyFogSetup.run();
                        }

                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                        RenderSystem.disableBlend();
                        RenderSystem.defaultBlendFunc();
                        posestack.popPose();
                        RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
                        double d0 = this.minecraft.player.getEyePosition(partialTick).y - this.level.getLevelData().getHorizonHeight(this.level);
                        if (d0 < 0.0) {
                            posestack.pushPose();
                            posestack.translate(0.0F, 12.0F, 0.0F);
                            this.darkBuffer.bind();
                            this.darkBuffer.drawWithShader(posestack.last().pose(), projectionMatrix, shaderinstance);
                            VertexBuffer.unbind();
                            posestack.popPose();
                        }

                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                        RenderSystem.depthMask(true);
                    }
                }
            }

        }
    }
}
