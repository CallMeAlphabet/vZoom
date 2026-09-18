package com.vzoom.range;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;

@Mod("vzoom")
public final class NeoForgeRangeBootstrap {
    public NeoForgeRangeBootstrap(IEventBus modBus) {
        String version = FMLLoader.versionInfo().mcVersion();
        String adapter = RangeSupport.adapter(version);
        try {
            modBus.register(Class.forName(adapter + ".VZoomClient$ModBus"));
            NeoForge.EVENT_BUS.register(Class.forName(adapter + ".VZoomClient$GameBus"));
            Class.forName(adapter + ".config.VZoomConfigIo").getMethod("load").invoke(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to initialize vZoom for Minecraft " + version, e);
        }
    }
}
