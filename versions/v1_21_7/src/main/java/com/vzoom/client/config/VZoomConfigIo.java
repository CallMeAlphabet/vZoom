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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vzoom.core.ZoomConfig;
import com.vzoom.core.ZoomState;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class VZoomConfigIo {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "vzoom.json";

    private VZoomConfigIo() {}

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }

    public static void load() {
        ZoomConfig cfg = ZoomState.INSTANCE.config;
        Path p = path();
        if (Files.exists(p)) {
            try (Reader reader = Files.newBufferedReader(p)) {
                ZoomConfig loaded = GSON.fromJson(reader, ZoomConfig.class);
                if (loaded != null) {
                    cfg.copyFrom(loaded);
                }
            } catch (IOException | RuntimeException e) {
                System.err.println("[vZoom] Failed to read config, using defaults: " + e.getMessage());
            }
        }
        cfg.applyDefaultsIfInvalid();
    }

    public static void save() {
        ZoomConfig cfg = ZoomState.INSTANCE.config;
        cfg.applyDefaultsIfInvalid();
        Path p = path();
        try {
            Files.createDirectories(p.getParent());
        } catch (IOException ignored) {
        }
        try (Writer writer = Files.newBufferedWriter(p)) {
            GSON.toJson(cfg, writer);
        } catch (IOException | RuntimeException e) {
            System.err.println("[vZoom] Failed to write config: " + e.getMessage());
        }
    }
}
