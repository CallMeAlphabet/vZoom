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

/**
 * Loads/saves the shared {@link ZoomConfig} to {@code config/vzoom.json} using
 * Gson (bundled with Minecraft).
 */
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
