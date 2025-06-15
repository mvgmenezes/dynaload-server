package io.dynaload.core.scan;


import io.dynaload.annotations.DynaloadExport;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClassScanner {
    private static final Map<String, Class<?>> registry = new HashMap<>();

    public static void scanAndRegisterAll() {
        try (ScanResult scanResult = new ClassGraph()
                .enableAllInfo()
                .acceptPackages("") // vazio = escaneia tudo
                .scan()) {


            Set<Class<?>> registeredManually = new HashSet<>();
            Set<Class<?>> alreadyProcessed = new HashSet<>();

            for (ClassInfo classInfo : scanResult.getClassesWithAnnotation(DynaloadExport.class.getName())) {
                Class<?> clazz = classInfo.loadClass();
                DynaloadExport dynaloadExport = clazz.getAnnotation(DynaloadExport.class);
                if (dynaloadExport != null) {

                    System.out.println("[Dynaload] Loading " + dynaloadExport.value() + " - class " + clazz.getName() );
                    register(dynaloadExport.value(), clazz);
                    registeredManually.add(clazz);

                    // 2. Processa includeDependencies
                    for (Class<?> dep : dynaloadExport.includeDependencies()) {
                        if (!alreadyProcessed.contains(dep) && !registeredManually.contains(dep)) {
                            System.out.println("[Dynaload] Loading Dependency " + dep.getName());
                            register(dep.getName(), dep); // ou alguma key mais amigável
                            alreadyProcessed.add(dep);
                        }
                    }
                }
            }
        }
    }

    public static void register(String key, Class<?> clazz) {
        registry.put(key, clazz);
    }

    public static Class<?> get(String path) {
        return registry.get(path);
    }

    public static Set<String> getRegisteredKeys() {
        return registry.keySet();
    }
}
