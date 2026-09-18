package com.vzoom.range;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;

public final class FabricRangeModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<Screen> getModConfigScreenFactory() {
        return this::create;
    }

    private Screen create(Screen parent) {
        String version = FabricLoader.getInstance().getModContainer("minecraft").get().getMetadata().getVersion().getFriendlyString();
        try {
            String adapter = RangeSupport.adapter(version);
            return (Screen) Class.forName(adapter + ".gui.VZoomConfigScreen").getConstructor(Screen.class).newInstance(parent);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to open the vZoom configuration", e);
        }
    }
}
