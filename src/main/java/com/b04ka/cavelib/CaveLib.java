package com.b04ka.cavelib;

import com.b04ka.cavelib.event.ClientEvent;
import com.b04ka.cavelib.event.CommonEvent;
import com.b04ka.cavelib.proxy.ClientProxy;
import com.b04ka.cavelib.proxy.CommonProxy;
import com.b04ka.cavelib.structure.piece.CLStructurePieceRegistry;
import com.b04ka.cavelib.sufrace.CaveSufraceRules;
import com.b04ka.cavelib.sufrace.SurfaceRuleConditionRegistry;
import com.b04ka.cavelib.utils.TestBiome;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforgespi.Environment;
import org.slf4j.Logger;

import java.util.function.Supplier;

@Mod(CaveLib.MODID)
public class CaveLib {

    public static <T> T unsafeRunForDist(Supplier<Supplier<T>> clientTarget, Supplier<Supplier<T>> serverTarget) {
        switch (Environment.get().getDist()) {
            case CLIENT -> {
                return clientTarget.get().get();
            }
            case DEDICATED_SERVER -> {
                return serverTarget.get().get();
            }
            default -> throw new IllegalArgumentException("UNSIDED?");
        }
    }

    public static CommonProxy PROXY = unsafeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
    public static final String MODID = "cavelib";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CaveLib(IEventBus modEventBus, ModContainer container) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        NeoForge.EVENT_BUS.register(new CommonEvent());
        SurfaceRuleConditionRegistry.DEF_REG.register(modEventBus);
        CLStructurePieceRegistry.STRUCTURE_PIECE.register(modEventBus);

        TestBiome.DEFERRED_REGISTER.register(modEventBus);
        TestBiome.init();
    }
    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(()->{
            CaveSufraceRules.setup();
        });
    }
    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> PROXY.clientInit());
    }
}
