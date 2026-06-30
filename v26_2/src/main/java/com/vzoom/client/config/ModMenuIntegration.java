package com.vzoom.client.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.vzoom.client.gui.VZoomConfigScreen;
import net.minecraft.client.gui.screens.Screen;

/**
 * Bridges vZoom's settings screen into Mod Menu. ModMenu is an optional
 * dependency: this entrypoint is only ever invoked when ModMenu is present.
 */
public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<Screen> getModConfigScreenFactory() {
        return VZoomConfigScreen::new;
    }
}
