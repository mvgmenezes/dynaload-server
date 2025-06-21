package io.dynaload.scan.export;


import io.dynaload.annotations.DynaloadExport;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClassExportScanner {
    private static final Map<String, Class<?>> registry = new HashMap<>();

    public static void scanAndRegisterAll(String basePackage) {
        try (ScanResult scanResult = new ClassGraph()
                .enableAllInfo()
                .acceptPackages(basePackage) // vazio = escaneia tudo
                .scan()) {

            Set<Class<?>> registered = new HashSet<>();

            for (ClassInfo classInfo : scanResult.getClassesWithAnnotation(DynaloadExport.class.getName())) {
                Class<?> clazz = classInfo.loadClass();
                DynaloadExport export = clazz.getAnnotation(DynaloadExport.class);

                if (export != null) {
                    // 1. Registra dependências primeiro
                    for (Class<?> dep : export.includeDependencies()) {
                        if (registered.add(dep)) {
                            System.out.println("[Dynaload] Export Dependency: " + dep.getName() + " - class: " + dep.getName());
                            register(dep.getName(), dep);
                        }
                    }

                    // 2. Agora registra a classe principal
                    if (registered.add(clazz)) {
                        String key = export.value();
                        if(export.value() == null || export.value().isBlank() || export.value().isEmpty()){
                            key = clazz.getName();
                        }
                        System.out.println("[Dynaload] Export Class : " + key + " - class: " + clazz.getName());
                        register(key, clazz);
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
