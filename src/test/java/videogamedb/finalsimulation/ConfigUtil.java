package videogamedb.finalsimulation;

import java.util.HashMap;
import java.util.Map;

public class ConfigUtil {

    private static final Map<String, Map<String, String>> CONFIG_MAP = new HashMap<>();

    static {
        CONFIG_MAP.put("local", Map.of("users", "10", "ramp.time", "20", "test.duration", "60"));
        CONFIG_MAP.put("default", Map.of("users", "3", "ramp.time", "60", "test.duration", "60"));
    }

    public static String getConfig(String env, String configName) {
        var envMap = CONFIG_MAP.get(env);

        if (envMap != null && envMap.containsKey(configName)) {
            return envMap.get(configName);
        }

        return CONFIG_MAP.get("default").get(configName);
    }

}
