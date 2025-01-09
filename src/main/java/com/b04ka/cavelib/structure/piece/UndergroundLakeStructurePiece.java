package com.b04ka.cavelib.structure.piece;

import com.b04ka.cavelib.misc.ACMath;
import com.b04ka.cavelib.misc.CLUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public class UndergroundLakeStructurePiece extends AbstractCaveGenerationStructurePiece {
    private final Fluid fluid;

    public UndergroundLakeStructurePiece(BlockPos chunkCorner, BlockPos holeCenter, int bowlHeight, int bowlRadius, ResourceKey<Biome> biomeResourceKey, Block surroundCornerOfLiquid, Block surroundCornerOfOtherLiquids, Block fluidBlock, Fluid fluid) {
        super(CLStructurePieceRegistry.UNDERGROUND_LAKE.get(), chunkCorner, holeCenter, bowlHeight, bowlRadius, biomeResourceKey, surroundCornerOfLiquid, fluidBlock, surroundCornerOfOtherLiquids);
        this.fluid = fluid;
    }

    public UndergroundLakeStructurePiece(CompoundTag tag) {
        super(CLStructurePieceRegistry.UNDERGROUND_LAKE.get(), tag);
        this.fluid = CLUtils.getFluidFromId(tag.getString("Fluid"));
    }

    public UndergroundLakeStructurePiece(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag tag) {
        this(tag);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        super.addAdditionalSaveData(context, tag);
        tag.putString("Fluid", BuiltInRegistries.FLUID.getKey(this.fluid).toString());
    }

    public void postProcess(WorldGenLevel level, StructureManager featureManager, ChunkGenerator chunkGen, RandomSource random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
        int cornerX = this.chunkCorner.getX();
        int cornerY = this.chunkCorner.getY();
        int cornerZ = this.chunkCorner.getZ();
        boolean flag = false;
        BlockPos.MutableBlockPos carve = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos carveAbove = new BlockPos.MutableBlockPos();
        carve.set(cornerX, cornerY, cornerZ);
        carveAbove.set(cornerX, cornerY, cornerZ);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 15; y >= 0; y--) {
                    carve.set(cornerX + x, Mth.clamp(cornerY + y, level.getMinBuildHeight(), level.getMaxBuildHeight()), cornerZ + z);
                    carveAbove.set(carve.getX(), carve.getY() + 1, carve.getZ());
                    float widthSimplexNoise1 = Math.min(ACMath.sampleNoise3D(carve.getX(), carve.getY(), carve.getZ(), radius) - 0.5F, 1.0F) * 0.94F;
                    float heightSimplexNoise1 = ACMath.sampleNoise3D(carve.getX() + 440, 0, carve.getZ() - 440, 20) * 0.5F + 0.5F;
                    double yDist = ACMath.smin(1F - Math.abs(this.holeCenter.getY() - carve.getY()) / (float) (height * heightSimplexNoise1), 0.7F, 0.3F);
                    double distToCenter = carve.distToLowCornerSqr(this.holeCenter.getX(), carve.getY(), this.holeCenter.getZ());
                    double targetRadius = yDist * (radius + widthSimplexNoise1 * radius) * radius;
                    double acidRadius = targetRadius - (targetRadius * 0.25F);
                    if (distToCenter <= targetRadius) {
                        FluidState fluidState = checkedGetBlock(level, carve).getFluidState();
                        flag = true;
                        if (isPillarBlocking(carve, yDist)) {
                            if (!fluidState.isEmpty()) {
                                checkedSetBlock(level, carve, this.surroundCornerOfLiquid.defaultBlockState());
                            }
                        } else {
                            if (carve.getY() < -10) {
                                checkedSetBlock(level, carve, this.floor.defaultBlockState()); //fluid
                                surroundCornerLiquid(level, carve);
                            } else {
                                if (isTouchingNonAcidLiquid(level, carve)) {
                                    surroundCornerOtherLiquid(level, carve);
                                    checkedSetBlock(level, carve, this.belowFloor.defaultBlockState());
                                }
                                checkedSetBlock(level, carve, Blocks.CAVE_AIR.defaultBlockState());
                            }

                        }

                    }
                }
            }
        }
        if (flag) {
            replaceBiomes(level, 20);
        }
    }

    private boolean isPillarBlocking(BlockPos.MutableBlockPos carve, double yDist) {
        float sample = ACMath.sampleNoise3D(carve.getX(), 0, carve.getZ(), 40) + ACMath.sampleNoise3D(carve.getX() - 440, 0, carve.getZ() + 412, 15) * 0.2F + ACMath.sampleNoise3D(carve.getX() - 100, carve.getY(), carve.getZ() - 400, 100) * 0.9F + 0.6F;
        float f = (float) (ACMath.smin((float) yDist / 0.67F, 1, 0.2F) + 1F);
        return sample >= 0.35F * f && sample <= ACMath.smin(1, (float) yDist / 0.67F + 0.35F, 0.2F) * f;
    }

    private void surroundCornerLiquid(WorldGenLevel level, BlockPos.MutableBlockPos center) {
        BlockPos.MutableBlockPos offset = new BlockPos.MutableBlockPos();
        for (Direction dir : ACMath.NOT_UP_DIRECTIONS) {
            offset.set(center);
            offset.move(dir);
            BlockState state = checkedGetBlockIgnoreY(level, offset);
            if (!state.getFluidState().is(this.fluid)) {
                checkedSetBlock(level, offset, this.surroundCornerOfLiquid.defaultBlockState());
            }
        }
    }

    private void surroundCornerOtherLiquid(WorldGenLevel level, BlockPos.MutableBlockPos center) {
        BlockPos.MutableBlockPos offset = new BlockPos.MutableBlockPos();
        for (Direction dir : Direction.values()) {
            offset.set(center);
            offset.move(dir);
            BlockState state = checkedGetBlock(level, offset);
            if (!state.getFluidState().isEmpty() && !state.getFluidState().is(this.fluid)) {
                checkedSetBlock(level, offset, this.floor.defaultBlockState());
            }
        }
    }

    private boolean isTouchingNonAcidLiquid(WorldGenLevel level, BlockPos.MutableBlockPos center) {
        BlockPos.MutableBlockPos offset = new BlockPos.MutableBlockPos();
        for (Direction dir : Direction.values()) {
            offset.set(center);
            offset.move(dir);
            FluidState state = checkedGetBlock(level, offset).getFluidState();
            if (!state.isEmpty() && !state.is(this.fluid)) {
                return true;
            }
        }
        FluidState state = checkedGetBlock(level, center).getFluidState();
        return !state.isEmpty() && !state.is(this.fluid);
    }
}