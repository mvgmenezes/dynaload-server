package io.dynaload.scan.callable;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CallableRegistry {
    private static final Map<String, Method> methodMap = new HashMap<>();
    private static final Map<String, Object> instanceMap = new HashMap<>();

    public static void register(String methodId, Method method, Object targetInstance) {
        methodMap.put(methodId, method);
        instanceMap.put(methodId, targetInstance);
    }

    public static Method getMethod(String methodId) {
        return methodMap.get(methodId);
    }

    public static Object getInstance(String methodId) {
        return instanceMap.get(methodId);
    }

    public static Set<String> getAllMethodIds() {
        return methodMap.keySet();
    }
}
