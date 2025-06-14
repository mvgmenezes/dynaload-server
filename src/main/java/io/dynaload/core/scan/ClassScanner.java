package io.dynaload.core.scan;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ClassScanner {
    private static final Map<String, Class<?>> registry = new HashMap<>();

    public static void register(String key, Class<?> clazz) {
        registry.put(key, clazz);
    }

    public static Class<?> get(String path) {
        return registry.get(path);
    }

    public static Set<String> getRegisteredPaths() {
        return registry.keySet();
    }
}
