package com.vzoom.range;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class FabricRangeBootstrap implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        String version = FabricLoader.getInstance().getModContainer("minecraft").get().getMetadata().getVersion().getFriendlyString();
        String adapter = RangeSupport.adapter(version);
        try {
            Object instance = Class.forName(adapter + ".VZoomClient").getDeclaredConstructor().newInstance();
            instance.getClass().getMethod("onInitializeClient").invoke(instance);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to initialize vZoom for Minecraft " + version, e);
        }
    }
}
