/*
 * Copyright 2026 CallMeAlphabet (ItzAlphabet)
 * Copyright 2026 Vextoly
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
