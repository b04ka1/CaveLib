package com.b04ka.cavelib;

import com.b04ka.cavelib.event.ClientEvent;
import com.b04ka.cavelib.event.CommonEvent;
import com.b04ka.cavelib.structure.piece.CLStructurePieceRegistry;
import com.b04ka.cavelib.sufrace.CaveSurfaceRules;
import com.b04ka.cavelib.sufrace.SurfaceRuleConditionRegistry;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(CaveLib.MODID)
public class CaveLib {
    public static final String MODID = "cavelib";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CaveLib(IEventBus modEventBus, ModContainer container) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        NeoForge.EVENT_BUS.register(new CommonEvent());
        SurfaceRuleConditionRegistry.DEF_REG.register(modEventBus);
        CLStructurePieceRegistry.STRUCTURE_PIECE.register(modEventBus);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CaveSurfaceRules.setup();
        });
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() ->
                NeoForge.EVENT_BUS.register(new ClientEvent()));
    }
}
