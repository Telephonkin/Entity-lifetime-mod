package net.telephonkin.data;

import net.telephonkin.data.CastLinkedTreeMapToHashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.lang.reflect.Type;
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

    public HashMap<String, HashMap<String,Object>> loadEntityConfig() throws IOException, URISyntaxException {
        if (ENTITY_CONFIG.exists()) {
            Path EntityConfigFile = Paths.get(ENTITY_CONFIG_PATH.toString() + "/entity_lifetime_mod_entity_config.json5");
            Gson gson = new Gson();

            Map defaultVanillaEntityConfigMapAsMap = gson.fromJson(Files.readString(EntityConfigFile), Map.class); // Use this Map as config for entities lifetime
            Map<String, HashMap<String,Object>> defaultVanillaEntityConfigMapUnraw = (Map<String, HashMap<String,Object>>) defaultVanillaEntityConfigMapAsMap;

            HashMap<String, HashMap<String, Object>> existedConfig = new HashMap<>();

            //Cast every nested LinkedTreeMap into a real HashMap
            if (defaultVanillaEntityConfigMapUnraw != null) {
                for (Map.Entry<?, ?> entry : defaultVanillaEntityConfigMapUnraw.entrySet()) {
                    String entityKey = String.valueOf(entry.getKey());

                    // Ensure the inner value is treated as a Map, then clone it into a real HashMap
                    if (entry.getValue() instanceof Map<?, ?> innerRawMap) {
                        HashMap<String, Object> innerHashMap = new HashMap<>();
                        for (Map.Entry<?, ?> innerEntry : innerRawMap.entrySet()) {
                            innerHashMap.put(String.valueOf(innerEntry.getKey()), innerEntry.getValue());
                        }
                        // Put the completely safe inner HashMap into our top-level HashMap
                        existedConfig.put(entityKey, innerHashMap);
                    }
                }
            }

            return existedConfig;

        } else {
            // Take file DefaultEntityConfig.json5 from same directory and create it in config directory;
            // Use config from this DefaultEntityConfig.json5 file
            InputStream DefaultConfigFilePath = DefaultEntityConfig.class.getResourceAsStream("/DefaultEntityConfig.json5");
            InputStreamReader reader = new InputStreamReader(DefaultConfigFilePath, StandardCharsets.UTF_8);
            Type mapType = new TypeToken<Map<String, Map<String,Object>>>() {}.getType();

            Gson gson = new Gson();

            // Casting config to proper HashMap type

            Map default_vanilla_entity_config_map_as_map = gson.fromJson(reader, mapType); // Use this Map as config for entities lifetime
            Map<String, HashMap<String,Object>> default_vanilla_entity_config_map_unraw = (Map<String, HashMap<String,Object>>) default_vanilla_entity_config_map_as_map;
            HashMap<String, HashMap<String,Object>> default_vanilla_entity_config_map = new HashMap<String, HashMap<String,Object>>(default_vanilla_entity_config_map_unraw);

            // Copying default config to config folder
            String data = new String(Objects.requireNonNull(getClass().getResourceAsStream("/DefaultEntityConfig.json5")).readAllBytes());

            try (PrintWriter out = new PrintWriter(ENTITY_CONFIG_PATH.toString() + "/entity_lifetime_mod_entity_config.json5")) {
                out.println(data);
            }

            return default_vanilla_entity_config_map;
        }
    }
}

