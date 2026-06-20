package net.telephonkin.data;

import com.google.gson.Gson;
import net.fabricmc.loader.api.FabricLoader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class DefaultConfig {
    private static final File default_vanilla_config_file = FabricLoader.getInstance().getConfigDir().resolve("net/telephonkin/data/DefaultConfig.json5").toFile();

    public static DefaultConfig config = new DefaultConfig();

    static String default_vanilla_mod_config = "net/telephonkin/data/DefaultConfig.json5"; // Placed in resources folder

    private InputStream getFileFromResourceAsStream(String fileName) {

        // The class loader that loaded the class
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);

        // the stream holding the file content
        if (inputStream == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return inputStream;
        }

    }

    public void loadMyConfig() throws IOException {

        //InputStream default_vanilla_config_as_stream = DefaultConfig.config.getFileFromResourceAsStream(default_vanilla_mod_config);
        File DefaultConfigFile = new File("DefaultConfig.json5");
        String map = DefaultConfigFile.toString();
        HashMap<String,Integer> default_vanilla_config_map = new Gson().fromJson(map, HashMap.class);


        if (default_vanilla_config_file.exists()) {
            // Read and parse the existing configuration file
            // e.g., using Gson, Jackson, or a custom parser
            default_vanilla_config_file.createNewFile();
        } else {

            // Create a default configuration file because it's missing
            // e.g., Files.createFile(configPath);
        }
    }
}

