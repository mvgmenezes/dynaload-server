package io.dynaload.init;

import io.dynaload.Dynaload;
import io.dynaload.annotations.DynaloadStart;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

public class DynaloadAutoBootstrap {

    public static void init() {
        try (ScanResult scanResult = new ClassGraph()
                .enableAllInfo()
                .acceptPackages("io.dynaload")
                .scan()) {

            Class<?> startClass = scanResult.getClassesWithAnnotation("io.dynaload.annotations.DynaloadStart")
                    .loadClasses()
                    .stream()
                    .findFirst()
                    .orElse(null);

            if (startClass != null) {
                DynaloadStart annotation = startClass.getAnnotation(DynaloadStart.class);
                int port = annotation.port();
                String basePackages = annotation.basePackage();
                System.out.println("[Dynaload] Starting Dynaload Server...");
                Dynaload.start(port, basePackages);
            }else{
                System.err.println("[Dynaload] Warning: No class annotated with @DynaloadStart was found. Dynaload server will not start.");
            }

        } catch (Exception e) {
            System.err.println("[Dynaload] Failed to initialize Dynaload automatically:");
            e.printStackTrace();
        }
    }
}
