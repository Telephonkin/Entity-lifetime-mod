package net.telephonkin.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class DefaultConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir();
    File CONFIG = CONFIG_PATH.resolve("entity_lifetime_config.json5").toFile();

    public static DefaultConfig config = new DefaultConfig();

    public Map loadConfig() throws IOException, URISyntaxException {
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
            //File DefaultConfigFile = new File("DefaultConfig.json5");
            URL DefaultConfigFile = DefaultConfig.class.getResource("/DefaultConfig.json5");
            assert DefaultConfigFile != null;
            Path DefaultConfigFilePath = Paths.get(DefaultConfigFile.toURI());
            Gson gson = new Gson();

            TypeToken<Map<String, Integer>> mapType = new TypeToken<Map<String, Integer>>(){};
            //Map<String, Integer> default_vanilla_config_map = gson.fromJson(map, mapType);
            Map default_vanilla_config_map = gson.fromJson(Files.readString(DefaultConfigFilePath), Map.class);
                    //Type type = new TypeToken<HashMap<String, String>>(){}.getType();
            //HashMap<String, Integer> myMap = new Gson().fromJson(map, map.getClass());
            //HashMap<String,Integer> default_vanilla_config_map = new Gson().fromJson(
            //        map,
            //        new TypeToken<HashMap<String, Integer>>()  {}.getType()
            //);
            //Files.writeString(CONFIG_PATH, (CharSequence) default_vanilla_config_map);
            return default_vanilla_config_map;
        }
    }
}

