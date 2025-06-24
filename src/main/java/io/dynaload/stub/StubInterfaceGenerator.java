package io.dynaload.stub;

import io.dynaload.annotations.DynaloadCallable;
import io.dynaload.annotations.DynaloadService;
import io.dynaload.scan.export.ClassExportScanner;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StubInterfaceGenerator {

    public static void generateStubsForExportable(String basePackage) {
        try {
            var scan = new io.github.classgraph.ClassGraph()
                    .enableClassInfo()
                    .acceptPackages(basePackage)
                    .scan();

            for (var classInfo : scan.getAllClasses()) {
                var clazz = classInfo.loadClass();

                if (!clazz.isAnnotationPresent(DynaloadService.class)) continue;

                boolean hasCallable = false;
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(DynaloadCallable.class)) {
                        hasCallable = true;
                        break;
                    }
                }

                if (hasCallable) {
                    generateInterfaceStub(clazz);
                }
            }

        } catch (Exception e) {
            System.err.println("[Dynaload Stub] Failed to generate interface stubs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateInterfaceStub(Class<?> serviceClass) throws IOException {
        String stubPackage = "io.dynaload.remote.service";
        String stubSimpleName = serviceClass.getSimpleName();
        String stubFullName = stubPackage + "." + stubSimpleName;

        var builder = new ByteBuddy().makeInterface().name(stubFullName);

        for (Method method : serviceClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(DynaloadCallable.class)) {
                builder = builder
                        .defineMethod(method.getName(), method.getReturnType(), Modifier.PUBLIC | Modifier.ABSTRACT)
                        .withParameters(method.getParameterTypes())
                        .withoutCode();
            }
        }

        var dynamicType = builder.make();
        var bytecode = dynamicType.getBytes();

        // Caminho do arquivo .class exato no layout de pacote
        Path classFile = Paths.get("build/dynaload/io/dynaload/remote/service/" + stubSimpleName + ".class");
        Files.createDirectories(classFile.getParent());
        Files.write(classFile, bytecode);

        // Força o carregamento da classe e registra no export scanner
        Class<?> generatedClass = dynamicType
                .load(ClassLoader.getSystemClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                .getLoaded();

        String key = "remote/service/" + stubSimpleName;
        ClassExportScanner.register(key, generatedClass);

        System.out.println("[Dynaload] Export Service Interface : " + key + " class: " + stubFullName);
    }
}