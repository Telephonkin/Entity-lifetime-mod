package net.telephonkin.data;

import com.google.gson.Gson;
import net.fabricmc.loader.api.FabricLoader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;


public class DefaultConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir();
    File CONFIG = CONFIG_PATH.resolve("entity_lifetime_config.json5").toFile();

    public static DefaultConfig config = new DefaultConfig();

    //static String default_vanilla_mod_config = "net/telephonkin/data/DefaultConfig.json5"; // Placed in resources folder

    public HashMap<String, Integer> loadConfig() throws IOException {

        if (CONFIG.exists()) {
            // Read the existing entity_lifetime_config.json5 configuration file
            String map = CONFIG.toString();
            return new Gson().fromJson(map, HashMap.class);
        } else {
            // Take file DefaultConfig.json5 from same directory and create it in config directory;
            // Use config from this DefaultConfig.json5 file
            File DefaultConfigFile = new File("DefaultConfig.json5");
            String map = DefaultConfigFile.toString();
            HashMap<String,Integer> default_vanilla_config_map = new Gson().fromJson(map, HashMap.class);
            Files.writeString(CONFIG_PATH, (CharSequence) default_vanilla_config_map);
            return default_vanilla_config_map;
        }
        return null;
    }
}

