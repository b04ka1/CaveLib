package com.b04ka.cavelib.structure.piece;

import com.b04ka.cavelib.misc.ACMath;
import com.b04ka.cavelib.misc.VoronoiGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
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
import org.apache.commons.lang3.mutable.MutableBoolean;

public class BowlStructurePiece extends AbstractCaveGenerationStructurePiece {

    private VoronoiGenerator voronoiGenerator;

    public BowlStructurePiece(BlockPos chunkCorner, BlockPos holeCenter, int bowlHeight, int bowlRadius, ResourceKey<Biome> biomeResourceKey, Block surroundCornerOfLiquid) {
        super(CLStructurePieceRegistry.BOWL.get(), chunkCorner, holeCenter, bowlHeight, bowlRadius, biomeResourceKey, surroundCornerOfLiquid, null, null);
    }

    public BowlStructurePiece(BlockPos chunkCorner, BlockPos holeCenter, int bowlHeight, int bowlRadius, ResourceKey<Biome> biomeResourceKey, Block surroundCornerOfLiquid, Block floor) {
        super(CLStructurePieceRegistry.BOWL.get(), chunkCorner, holeCenter, bowlHeight, bowlRadius, biomeResourceKey, surroundCornerOfLiquid, floor, null);
    }

    public BowlStructurePiece(BlockPos chunkCorner, BlockPos holeCenter, int bowlHeight, int bowlRadius, ResourceKey<Biome> biomeResourceKey, Block surroundCornerOfLiquid, Block floor, Block belowFloor) {
        super(CLStructurePieceRegistry.BOWL.get(), chunkCorner, holeCenter, bowlHeight, bowlRadius, biomeResourceKey, surroundCornerOfLiquid, floor, belowFloor);
    }

    public BowlStructurePiece(CompoundTag tag) {
        super(CLStructurePieceRegistry.BOWL.get(), tag);
    }

    public BowlStructurePiece(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag tag) {
        this(tag);
    }

    public void postProcess(WorldGenLevel level, StructureManager featureManager, ChunkGenerator chunkGen, RandomSource random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
        if (voronoiGenerator == null) {
            voronoiGenerator = new VoronoiGenerator(level.getSeed());
            voronoiGenerator.setOffsetAmount(0.6F);
        }
        int cornerX = this.chunkCorner.getX();
        int cornerY = this.chunkCorner.getY();
        int cornerZ = this.chunkCorner.getZ();
        boolean flag = false;
        BlockPos.MutableBlockPos carve = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos carveAbove = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos carveBelow = new BlockPos.MutableBlockPos();
        carve.set(cornerX, cornerY, cornerZ);
        carveAbove.set(cornerX, cornerY, cornerZ);
        carveBelow.set(cornerX, cornerY, cornerZ);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                MutableBoolean doFloor = new MutableBoolean(false);
                for (int y = 15; y >= 0; y--) {
                    carve.set(cornerX + x, Mth.clamp(cornerY + y, level.getMinBuildHeight(), level.getMaxBuildHeight()), cornerZ + z);
                    if (inCircle(carve) && !checkedGetBlock(level, carve).is(Blocks.BEDROCK)) {
                        flag = true;
                        checkedSetBlock(level, carve, Blocks.CAVE_AIR.defaultBlockState());
                        surroundCornerOfLiquid(level, carve);
                        carveBelow.set(carve.getX(), carve.getY() - 1, carve.getZ());
                        doFloor.setTrue();
                    }
                }
                if (doFloor.isTrue() && !checkedGetBlock(level, carveBelow).isAir()) {
                    decorateFloor(level, random, carveBelow);
                    doFloor.setFalse();
                }
            }
        }
        if (flag) {
            replaceBiomes(level, 32);
        }
    }

    private void surroundCornerOfLiquid(WorldGenLevel level, Vec3i center) {
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
        float wallNoise = (ACMath.sampleNoise3D(carve.getX(), (int) (carve.getY() * 0.1F), carve.getZ(), 40) + 1.0F) * 0.5F;
        double yDist = ACMath.smin(1F - Math.abs(this.holeCenter.getY() - carve.getY()) / (float) (height * 0.5F), 1.0F, 0.3F);
        double distToCenter = carve.distToLowCornerSqr(this.holeCenter.getX(), carve.getY(), this.holeCenter.getZ());
        double targetRadius = yDist * (radius * wallNoise) * radius;
        return distToCenter < targetRadius;
    }


}