package net.telephonkin.data;

import java.util.HashMap;
import java.util.Map;

public class ConfigConverter {
    public static ConfigConverter cast = new ConfigConverter();

    private HashMap<String, HashMap<String, Object>> output;

    public HashMap<String, HashMap<String,Object>> process(Map<String, HashMap<String,Object>> inputLinkedTreeMap) {
        for (Map.Entry<String, HashMap<String, Object>> entry : inputLinkedTreeMap.entrySet()) {
            output.put(entry.getKey(),entry.getValue());
        }
        return output;
    }
}
