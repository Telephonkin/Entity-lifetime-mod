package net.telephonkin.data;

import com.mojang.authlib.minecraft.client.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import com.google.gson.Gson;
import com.google.common.reflect.TypeToken;
//import java.lang.reclect.Type;
import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import blue.endless.jankson.Comment;

public class DefaultConfig {
    private static final Jankson JANKSON = Jankson.builder().build();

    private static final File default_vanilla_config_file = FabricLoader.getInstance().getConfigDir().resolve("DefaultConfig.json5").toFile();

    public static DefaultConfig config = new DefaultConfig();
    @Comment("Enable or disable the custom mechanic")
    public boolean enableFeature = true;

    @Comment("The higher the value, the deadlier the explosion")
    public int explosionPower = 4;
    public void loadMyConfig() {
        // Check that default config file exist
        if (default_vanilla_config_file.exists()) {
            // Read and parse the existing configuration file
            // e.g., using Gson, Jackson, or a custom parser
        } else {

            // Create a default configuration file because it's missing
            // e.g., Files.createFile(configPath);
        }
    }
}

