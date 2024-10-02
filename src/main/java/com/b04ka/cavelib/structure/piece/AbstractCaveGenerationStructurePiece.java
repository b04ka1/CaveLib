package com.b04ka.cavelib.structure.piece;

import com.b04ka.cavelib.CaveLib;
import com.b04ka.cavelib.misc.ACMath;
import com.b04ka.cavelib.misc.CLUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

public abstract class AbstractCaveGenerationStructurePiece extends StructurePiece {
    protected final BlockPos chunkCorner;
    protected final BlockPos holeCenter;
    protected final int height;
    protected final int radius;
    private static boolean replaceBiomesError;
    public final Block surroundCornerOfLiquid;
    public final Block floor;
    public final Block belowFloor;
    public final ResourceLocation biomeResourceLocation;

    public AbstractCaveGenerationStructurePiece(StructurePieceType pieceType, BlockPos chunkCorner, BlockPos holeCenter, int height, int radius, ResourceKey<Biome> biomeResourceKey, Block surroundCornerOfLiquid, Block floor, Block belowFloor) {
        this(pieceType, chunkCorner, holeCenter, height, radius, chunkCorner.getY() - 2, chunkCorner.getY() + 16, biomeResourceKey!=null? biomeResourceKey.location():null, surroundCornerOfLiquid, floor, belowFloor);
    }

    public AbstractCaveGenerationStructurePiece(StructurePieceType pieceType, BlockPos chunkCorner, BlockPos holeCenter, int height, int radius, int minY, int maxY, ResourceLocation biomeResourceLocation, Block surroundCornerOfLiquid, Block floor, Block belowFloor) {
        super(pieceType, 0, createBoundingBox(chunkCorner, minY, maxY));
        this.chunkCorner = chunkCorner;
        this.holeCenter = holeCenter;
        this.height = height;
        this.radius = radius;
        this.surroundCornerOfLiquid = surroundCornerOfLiquid;
        this.floor = floor;
        this.belowFloor = belowFloor;
        this.biomeResourceLocation = biomeResourceLocation;
    }

    public AbstractCaveGenerationStructurePiece(StructurePieceType pieceType, CompoundTag tag) {
        super(pieceType, tag);
        this.chunkCorner = new BlockPos(tag.getInt("TPX"), tag.getInt("TPY"), tag.getInt("TPZ"));
        this.holeCenter = new BlockPos(tag.getInt("HCX"), tag.getInt("HCY"), tag.getInt("HCZ"));
        this.height = tag.getInt("Height");
        this.radius = tag.getInt("Radius");
        this.biomeResourceLocation = ResourceLocation.bySeparator(tag.getString("Biome"),':');
        this.surroundCornerOfLiquid = CLUtils.getBlockFromId(tag.getString("SurroundLiquid"));
        this.floor = !tag.getString("Floor").isEmpty() ? CLUtils.getBlockFromId(tag.getString("Floor")): null;
        this.belowFloor = !tag.getString("BelowFloor").isEmpty() && this.floor!=null ? CLUtils.getBlockFromId(tag.getString("BelowFloor")): null;
    }

    private static BoundingBox createBoundingBox(BlockPos origin, int minY, int maxY) {
        ChunkPos chunkPos = new ChunkPos(origin);
        return new BoundingBox(chunkPos.getMinBlockX(), minY, chunkPos.getMinBlockZ(), chunkPos.getMaxBlockX(), maxY, chunkPos.getMaxBlockZ());
    }

    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("TPX", this.chunkCorner.getX());
        tag.putInt("TPY", this.chunkCorner.getY());
        tag.putInt("TPZ", this.chunkCorner.getZ());
        tag.putInt("HCX", this.holeCenter.getX());
        tag.putInt("HCY", this.holeCenter.getY());
        tag.putInt("HCZ", this.holeCenter.getZ());
        tag.putInt("Height", this.height);
        tag.putInt("Radius", this.radius);
        if(this.biomeResourceLocation!=null){
            tag.putString("Biome", this.biomeResourceLocation.toString());
        }
        tag.putString("SurroundLiquid", BuiltInRegistries.BLOCK.getKey(this.surroundCornerOfLiquid).toString());
        if (this.floor != null) {
            tag.putString("Floor", BuiltInRegistries.BLOCK.getKey(this.floor).toString());
            if (this.belowFloor != null) {
                tag.putString("BelowFloor", BuiltInRegistries.BLOCK.getKey(this.belowFloor).toString());
            }
        }
    }

    public void replaceBiomes(WorldGenLevel level, int belowLevel) {
        if(replaceBiomesError){
          return;
        }
        try {
            if(this.biomeResourceLocation!=null) {
                Holder<Biome> biomeHolder = level.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(ResourceKey.create(Registries.BIOME, this.biomeResourceLocation));
                ChunkAccess chunkAccess = level.getChunk(this.chunkCorner);
                int stopY = level.getSeaLevel() - belowLevel;
                BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
                pos.set(this.chunkCorner.getX(), stopY, this.chunkCorner.getZ());
                if (chunkAccess != null && !biomeHolder.is(Biomes.PLAINS)) {
                    while (pos.getY() > level.getMinBuildHeight()) {
                        pos.move(0, -8, 0);
                        int sectionIndex = chunkAccess.getSectionIndex(pos.getY());
                        if (sectionIndex >= 0 && sectionIndex < chunkAccess.getSections().length) {
                            LevelChunkSection section = chunkAccess.getSection(sectionIndex);
                            PalettedContainer<Holder<Biome>> container = section.getBiomes().recreate();
                            if (container != null) {
                                for (int biomeX = 0; biomeX < 4; ++biomeX) {
                                    for (int biomeY = 0; biomeY < 4; ++biomeY) {
                                        for (int biomeZ = 0; biomeZ < 4; ++biomeZ) {
                                            container.getAndSetUnchecked(biomeX, biomeY, biomeZ, biomeHolder);
                                        }
                                    }
                                }
                                section.biomes = container;
                            }
                        }
                    }
                }
            }
        }catch (Exception e){
            replaceBiomesError = true;
            CaveLib.LOGGER.warn("Could not replace biomes for Alex's Caves. Error will show only once - likely a world-gen mod incompatibility. Or biome is null");
            e.printStackTrace();
        }
    }

    public void checkedSetBlock(WorldGenLevel level, BlockPos position, BlockState state) {
        if (this.getBoundingBox().isInside(position)) {
            level.setBlock(position, state, 128);
        }
    }

    public BlockState checkedGetBlock(WorldGenLevel level, BlockPos position) {
        if (this.getBoundingBox().isInside(position)) {
            return level.getBlockState(position);
        } else {
            return Blocks.VOID_AIR.defaultBlockState();
        }
    }

    public BlockState checkedGetBlockIgnoreY(WorldGenLevel level, BlockPos position) {
        if (this.getBoundingBox().isInside(position.getX(), this.getBoundingBox().minY(), position.getZ())) {
            return level.getBlockState(position);
        } else {
            return Blocks.VOID_AIR.defaultBlockState();
        }
    }

    public void addChildren(StructurePiece piece, StructurePieceAccessor accessor, RandomSource random) {

    }

    public void decorateFloor(WorldGenLevel level, RandomSource rand, BlockPos.MutableBlockPos carveBelow) {
        BlockPos carveBelowImmutable = carveBelow.immutable();
        BlockState floor = this.floor!=null ? this.floor.defaultBlockState() : null;
        BlockState belowFloor = this.belowFloor!=null ? this.belowFloor.defaultBlockState() : null;
        if (floor != null){
            if(belowFloor!= null){
            checkedSetBlock(level, carveBelow, floor);
                for (int i = 0; i < 1 + rand.nextInt(2); i++) {
                carveBelowImmutable = carveBelow.below();
                checkedSetBlock(level, carveBelowImmutable, belowFloor);
                }
            } else{
            float floorNoise = (ACMath.sampleNoise2D(carveBelow.getX(), carveBelow.getZ(), 50) + 1.0F) * 0.5F;
            checkedSetBlock(level, carveBelow, floor);
                for (int i = 0; i < Math.ceil(floorNoise * 3); i++) {
                carveBelow.move(0, 1, 0);
                checkedSetBlock(level, carveBelow, floor);
                }
            }
        }
    }
}
