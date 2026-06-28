package net.telephonkin.data;

import com.google.gson.Gson;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DefaultConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir();
    File CONFIG = CONFIG_PATH.resolve("entity_lifetime_config.json5").toFile();

    public static DefaultConfig config = new DefaultConfig();

    public HashMap<String, Integer> loadConfig() throws IOException, URISyntaxException {
        if (CONFIG.exists()) {
            Path ConfigFile = Paths.get(CONFIG_PATH.toString() + "/entity_lifetime_config.json5");
            Gson gson = new Gson();

            Map default_vanilla_config_map_as_map = gson.fromJson(Files.readString(ConfigFile), Map.class); // Use this Map as config for entities lifetime
            Map<String, Integer> default_vanilla_config_map_unraw = (Map<String, Integer>) default_vanilla_config_map_as_map;
            return new HashMap<String, Integer>(default_vanilla_config_map_unraw);

        } else {
            // Take file DefaultConfig.json5 from same directory and create it in config directory;
            // Use config from this DefaultConfig.json5 file
            URL DefaultConfigFile = DefaultConfig.class.getResource("/DefaultConfig.json5");

            assert DefaultConfigFile != null; // Ensure that there is DefaultConfig.json5

            Path DefaultConfigFilePath = Paths.get(DefaultConfigFile.toURI());
            Gson gson = new Gson();

            // Casting config to proper HashMap type
            Map default_vanilla_config_map_as_map = gson.fromJson(Files.readString(DefaultConfigFilePath), Map.class); // Use this Map as config for entities lifetime
            Map<String, Integer> default_vanilla_config_map_unraw = (Map<String, Integer>) default_vanilla_config_map_as_map;
            HashMap<String, Integer> default_vanilla_config_map = new HashMap<String, Integer>(default_vanilla_config_map_unraw);

            // Copying default config to config folder
            String data = new String(Objects.requireNonNull(getClass().getResourceAsStream("/DefaultConfig.json5")).readAllBytes());

            try (PrintWriter out = new PrintWriter(CONFIG_PATH.toString() + "/entity_lifetime_config.json5")) {
                out.println(data);
            }

            return default_vanilla_config_map;
        }
    }
}

