package io.dynaload.scan.callable.springframework;

import io.dynaload.scan.callable.CallableRegistry;

import java.lang.reflect.Method;

public class CallableExportScanner {

    public static void register(Object instance, Method method) {
        String methodId = generateMethodId(instance.getClass(), method);
        CallableRegistry.register(methodId, method, instance);
        System.out.println("[Dynaload] Export Method (Spring): " + methodId + " from service: " + instance.getClass().getName());
    }

    private static String generateMethodId(Class<?> clazz, Method method) {
        String className = clazz.getSimpleName();
        if (className.contains("$$")) {
            className = className.substring(0, className.indexOf("$$")); // remove proxy suffix
        }
        return Character.toLowerCase(className.charAt(0)) + className.substring(1) + "::" + method.getName();
    }
}
