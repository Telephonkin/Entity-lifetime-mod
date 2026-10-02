package net.telephonkin.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DefaultEntityConfig {
    private static final Path ENTITY_CONFIG_PATH = FabricLoader.getInstance().getConfigDir();
    File ENTITY_CONFIG = ENTITY_CONFIG_PATH.resolve("entity_lifetime_mod_entity_config.json5").toFile();

    public static DefaultEntityConfig config = new DefaultEntityConfig();

    public HashMap<String, Integer> loadEntityConfig() throws IOException, URISyntaxException {
        if (ENTITY_CONFIG.exists()) {
            Path EntityConfigFile = Paths.get(ENTITY_CONFIG_PATH.toString() + "/entity_lifetime_mod_entity_config.json5");
            Gson gson = new Gson();

            Map default_vanilla_entity_config_map_as_map = gson.fromJson(Files.readString(EntityConfigFile), Map.class); // Use this Map as config for entities lifetime
            Map<String, Integer> default_vanilla_entity_config_map_unraw = (Map<String, Integer>) default_vanilla_entity_config_map_as_map;
            return new HashMap<String, Integer>(default_vanilla_entity_config_map_unraw);

        } else {
            // Take file DefaultEntityConfig.json5 from same directory and create it in config directory;
            // Use config from this DefaultEntityConfig.json5 file
            InputStream DefaultConfigFilePath = DefaultEntityConfig.class.getResourceAsStream("/DefaultEntityConfig.json5");
            InputStreamReader reader = new InputStreamReader(DefaultConfigFilePath, StandardCharsets.UTF_8);
            java.lang.reflect.Type mapType = new TypeToken<Map<String, Object>>() {}.getType();

            Gson gson = new Gson();

            // Casting config to proper HashMap type

            Map default_vanilla_entity_config_map_as_map = gson.fromJson(reader, mapType); // Use this Map as config for entities lifetime
            Map<String, Integer> default_vanilla_entity_config_map_unraw = (Map<String, Integer>) default_vanilla_entity_config_map_as_map;
            HashMap<String, Integer> default_vanilla_entity_config_map = new HashMap<String, Integer>(default_vanilla_entity_config_map_unraw);

            // Copying default config to config folder
            String data = new String(Objects.requireNonNull(getClass().getResourceAsStream("/DefaultEntityConfig.json5")).readAllBytes());

            try (PrintWriter out = new PrintWriter(ENTITY_CONFIG_PATH.toString() + "/entity_lifetime_mod_entity_config.json5")) {
                out.println(data);
            }

            return default_vanilla_entity_config_map;
        }
    }
}

