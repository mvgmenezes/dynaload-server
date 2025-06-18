package io.dynaload.scan.callable;

import io.dynaload.annotations.DynaloadCallable;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import io.dynaload.annotations.DynaloadService;

public class CallableScanner {
    public static void scanAndRegister(String basePackage) {
        try (ScanResult scanResult = new ClassGraph().enableAllInfo().acceptPackages(basePackage).scan()) {
            for (ClassInfo classInfo : scanResult.getAllClasses()) {
                Class<?> clazz = classInfo.loadClass();

                if (!clazz.isAnnotationPresent(DynaloadService.class)) {
                    continue; // apenas classes com @DynaloadService
                }

                if (clazz.isInterface() || clazz.isAnnotation() || Modifier.isAbstract(clazz.getModifiers()) || clazz.isEnum()) {
                    continue;
                }

                Object instance = clazz.getDeclaredConstructor().newInstance();

                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(DynaloadCallable.class)) {
                        String methodId = generateMethodId(clazz, method);
                        CallableRegistry.register(methodId, method, instance);
                        System.out.println("[Dynaload] Callable registered: " + methodId + " - service: " + clazz.getName());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[Dynaload] Error during scanning: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String generateMethodId(Class<?> clazz, Method method) {
        String className = clazz.getSimpleName();
        return Character.toLowerCase(className.charAt(0)) + className.substring(1) + "." + method.getName();
    }
}
