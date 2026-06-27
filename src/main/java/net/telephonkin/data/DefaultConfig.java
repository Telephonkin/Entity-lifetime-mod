package net.telephonkin.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DefaultConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir();
    File CONFIG = CONFIG_PATH.resolve("entity_lifetime_config.json5").toFile();

    public static DefaultConfig config = new DefaultConfig();

    public Map<String, Integer> loadConfig() throws IOException, URISyntaxException {
        if (CONFIG.exists()) {
            // Read the existing entity_lifetime_config.json5 configuration file
            String map = CONFIG.toString();
            return new Gson().fromJson(
                    map,
                    new TypeToken<HashMap<String, Integer>>()  {}.getType()
            );
        } else {
            // Take file DefaultConfig.json5 from same directory and create it in config directory;
            // Use config from this DefaultConfig.json5 file
            URL DefaultConfigFile = DefaultConfig.class.getResource("/DefaultConfig.json5");

            assert DefaultConfigFile != null; // Ensure that there is DefaultConfig.json5

            Path DefaultConfigFilePath = Paths.get(DefaultConfigFile.toURI());
            Gson gson = new Gson();

            Map default_vanilla_config_map_as_map = gson.fromJson(Files.readString(DefaultConfigFilePath), Map.class); // Use this Map as config for entities lifetime

            Map<String, Integer> default_vanilla_config_map_unraw = (Map<String, Integer>) default_vanilla_config_map_as_map;


            HashMap<String, Integer> default_vanilla_config_map = new HashMap<String, Integer>(default_vanilla_config_map_unraw);
            //HashMap<String, Integer> hashMap =
            //        (default_vanilla_config_map_unraw instanceof HashMap)
            //                ? (HashMap) default_vanilla_config_map_unraw
            //                : new HashMap<String, Integer>(default_vanilla_config_map_unraw);

            //HashMap<String, Integer> default_vanilla_config_map = (HashMap<String, Integer>) default_vanilla_config_map_unraw;

            //System.out.println(CONFIG_PATH.toString() + "/entity_lifetime_config.json5");

            try (PrintWriter out = new PrintWriter(CONFIG_PATH.toString() + "/entity_lifetime_config.json5")) {
                out.println(default_vanilla_config_map.toString());
            }

            return default_vanilla_config_map;
        }
    }
}

