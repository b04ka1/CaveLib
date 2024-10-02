package com.b04ka.cavelib.structure.piece;

import com.b04ka.cavelib.CaveLib;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;


public class CLStructurePieceRegistry {
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECE = DeferredRegister.create(Registries.STRUCTURE_PIECE, CaveLib.MODID);

    public static final Supplier<StructurePieceType> BOWL = STRUCTURE_PIECE.register("bowl", ()-> BowlStructurePiece::new);

    public static final Supplier<StructurePieceType> CAVERN = STRUCTURE_PIECE.register("cavern", ()-> CavernStructurePiece::new);

    public static final Supplier<StructurePieceType> CANYON = STRUCTURE_PIECE.register("canyon", ()-> CanyonStructurePiece::new);
}
