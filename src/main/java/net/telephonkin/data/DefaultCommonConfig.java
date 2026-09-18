package net.telephonkin.data;

import com.google.gson.Gson;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
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
            URL DefaultConfigFile = DefaultCommonConfig.class.getResource("/DefaultCommonConfig.json5");

            assert DefaultConfigFile != null; // Ensure that there is DefaultCommonConfig.json5

            Path DefaultConfigFilePath = Paths.get(DefaultConfigFile.toURI());
            Gson gson = new Gson();

            // Casting config to proper HashMap type
            Map default_vanilla_common_config_map_as_map = gson.fromJson(Files.readString(DefaultConfigFilePath), Map.class); // Use this Map as config for entities lifetime
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

