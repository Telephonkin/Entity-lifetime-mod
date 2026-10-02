package net.telephonkin.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DefaultCommonConfig {
    private static final Path COMMON_CONFIG_PATH = FabricLoader.getInstance().getConfigDir();
    File COMMON_CONFIG = COMMON_CONFIG_PATH.resolve("entity_lifetime_mod_common_config.json5").toFile();

    public static DefaultCommonConfig config = new DefaultCommonConfig();

    public HashMap<String, Object> loadCommonConfig() throws IOException, URISyntaxException {
        if (COMMON_CONFIG.exists()) {
            Path CommonConfigFile = Paths.get(COMMON_CONFIG_PATH.toString() + "/entity_lifetime_mod_common_config.json5");
            Gson gson = new Gson();

            Map default_vanilla_common_config_map_as_map = gson.fromJson(Files.readString(CommonConfigFile), Map.class); // Use this Map as config for entities lifetime
            Map<String, Object> default_vanilla_common_config_map_unraw = (Map<String, Object>) default_vanilla_common_config_map_as_map;
            return new HashMap<String, Object>(default_vanilla_common_config_map_unraw);

        } else {
            // Take file DefaultCommonConfig.json5 from same directory and create it in config directory;
            // Use config from this DefaultCommonConfig.json5 file
            InputStream DefaultConfigFilePath = DefaultCommonConfig.class.getResourceAsStream("/DefaultEntityConfig.json5");
            InputStreamReader reader = new InputStreamReader(DefaultConfigFilePath, StandardCharsets.UTF_8);
            java.lang.reflect.Type mapType = new TypeToken<Map<String, Object>>() {}.getType();

            Gson gson = new Gson();

            // Casting config to proper HashMap type
            Map default_vanilla_common_config_map_as_map = gson.fromJson(reader, mapType); // Use this Map as config for common config
            Map<String, Object> default_vanilla_common_config_map_unraw = (Map<String, Object>) default_vanilla_common_config_map_as_map;
            HashMap<String, Object> default_vanilla_common_config_map = new HashMap<String, Object>(default_vanilla_common_config_map_unraw);

            // Copying default config to config folder
            String data = new String(Objects.requireNonNull(getClass().getResourceAsStream("/DefaultCommonConfig.json5")).readAllBytes());

            try (PrintWriter out = new PrintWriter(COMMON_CONFIG_PATH.toString() + "/entity_lifetime_mod_common_config.json5")) {
                out.println(data);
            }

            return default_vanilla_common_config_map;
        }
    }
}

