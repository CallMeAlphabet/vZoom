package com.vzoom.range;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class RangeSupport {
    private RangeSupport() {
    }

    public static String adapter(String version) {
        Properties properties = new Properties();
        try (InputStream input = RangeSupport.class.getClassLoader().getResourceAsStream("vzoom-range.properties")) {
            if (input == null) throw new IllegalStateException("Missing vZoom range metadata");
            properties.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read vZoom range metadata", e);
        }
        String adapter = properties.getProperty("adapter." + version);
        if (adapter == null) throw new IllegalStateException("Unsupported Minecraft version " + version);
        return adapter;
    }
}
