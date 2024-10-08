package com.b04ka.cavelib.structure.piece;

import com.b04ka.cavelib.misc.ACMath;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public class CavernStructurePiece extends AbstractCaveGenerationStructurePiece {

    public CavernStructurePiece(BlockPos chunkCorner, BlockPos holeCenter, int bowlHeight, int bowlRadius, Block surroundCornerOfLiquid) {
        super(CLStructurePieceRegistry.CAVERN.get(), chunkCorner, holeCenter, bowlHeight, bowlRadius, null, surroundCornerOfLiquid, null, null);
    }

    public CavernStructurePiece(CompoundTag tag) {
        super(CLStructurePieceRegistry.CAVERN.get(), tag);
    }

    public CavernStructurePiece(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag tag) {
        this(tag);
    }

    public void postProcess(WorldGenLevel level, StructureManager featureManager, ChunkGenerator chunkGen, RandomSource random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
        int cornerX = this.chunkCorner.getX();
        int cornerY = this.chunkCorner.getY();
        int cornerZ = this.chunkCorner.getZ();
        BlockPos.MutableBlockPos carve = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos carveAbove = new BlockPos.MutableBlockPos();
        carve.set(cornerX, cornerY, cornerZ);
        carveAbove.set(cornerX, cornerY, cornerZ);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 15; y >= 0; y--) {
                    carve.set(cornerX + x, Mth.clamp(cornerY + y, level.getMinBuildHeight(), level.getMaxBuildHeight()), cornerZ + z);
                    carveAbove.set(carve.getX(), carve.getY() + 1, carve.getZ());
                    if (inCircle(carve) && !checkedGetBlock(level, carve).is(Blocks.BEDROCK)) {
                        checkedSetBlock(level, carve, Blocks.CAVE_AIR.defaultBlockState());
                        surroundCornerOfLiquid(level, carve);
                    }
                }
            }
        }
    }

    private void surroundCornerOfLiquid(WorldGenLevel level, BlockPos.MutableBlockPos center) {
        BlockPos.MutableBlockPos offset = new BlockPos.MutableBlockPos();
        for (Direction dir : Direction.values()) {
            offset.set(center);
            offset.move(dir);
            BlockState state = checkedGetBlock(level, offset);
            if (!state.getFluidState().isEmpty()) {
                checkedSetBlock(level, offset, this.surroundCornerOfLiquid.defaultBlockState());
            }
        }
    }

    private boolean inCircle(BlockPos carve) {
        float df1 = (ACMath.sampleNoise3D(carve.getX(), carve.getY(), carve.getZ(), 30) + 1.0F) * 0.5F;
        float df2 = ACMath.sampleNoise3D(carve.getX() - 1200, carve.getY() + 100, carve.getZ() + 120, 10) * 0.15F;
        float innerCircleOrb = ACMath.sampleNoise3D(carve.getX() + 400, carve.getY() + 40, carve.getZ() - 600, 20);
        double df1Smooth = ACMath.smin(df1 + df2, 1.0F, 0.1F);
        double yDist = ACMath.smin(1F - Math.abs(this.holeCenter.getY() - carve.getY()) / (float) (height * 0.5F), 1.0F, 0.3F);
        double distToCenter = carve.distToLowCornerSqr(this.holeCenter.getX(), carve.getY(), this.holeCenter.getZ());
        double targetRadius = yDist * (radius * df1Smooth) * radius;
        return distToCenter < targetRadius && (innerCircleOrb > -0.5F || innerCircleOrb < -0.75F);
    }
}