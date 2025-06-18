package io.dynaload.frame;

import io.dynaload.scan.callable.CallableRegistry;
import io.dynaload.scan.export.ClassExportScanner;

import java.io.*;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FrameDispatcher {

    public Frame dispatch(Frame frame) {
        try {
            int requestId = frame.requestId;
            byte opCode = frame.opCode;
            byte[] payload = frame.payload;

            return switch (opCode) {
                case 0x01 -> handleGetClass(payload, requestId);
                case 0x02 -> handleInvoke(payload, requestId);
                case 0x03 -> handleListClasses(requestId);
                default -> Frame.error(requestId, "Unknown opCode: " + opCode);
            };
        } catch (Exception e) {
            e.printStackTrace();
            return Frame.error(frame.requestId, e.getMessage());
        }
    }

    private Frame handleGetClass(byte[] payload, int requestId) {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(payload))) {
            String path = in.readUTF();
            Class<?> clazz = ClassExportScanner.get(path);
            if (clazz == null)
                return Frame.error(requestId, "Class not found: " + path);

            String classResource = clazz.getName().replace('.', '/') + ".class";
            byte[] bytecode = clazz.getClassLoader().getResourceAsStream(classResource).readAllBytes();

            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            try (DataOutputStream out = new DataOutputStream(bout)) {
                out.writeUTF(clazz.getName());
                out.writeInt(bytecode.length);
                out.write(bytecode);
            }

            return Frame.success(requestId, (byte) 0x11, bout.toByteArray());

        } catch (Exception e) {
            return Frame.error(requestId, e.getMessage());
        }
    }

    private Frame handleInvoke(byte[] payload, int requestId) {
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(payload))) {
            String methodId = in.readUTF();
            int argCount = in.readInt();
            Object[] args = new Object[argCount];
            for (int i = 0; i < argCount; i++) {
                args[i] = in.readObject();
            }

            var method = CallableRegistry.getMethod(methodId);
            var instance = CallableRegistry.getInstance(methodId);
            Object result = method.invoke(instance, args);

            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            try (ObjectOutputStream out = new ObjectOutputStream(bout)) {
                out.writeUTF("SUCCESS");
                out.writeObject(result);
            }

            return Frame.success(requestId, (byte) 0x20, bout.toByteArray());

        } catch (Exception e) {
            return Frame.error(requestId, e.getMessage());
        }
    }

    private Frame handleListClasses(int requestId) {
        try {
            var classes = ClassExportScanner.getRegisteredKeys();

            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            try (DataOutputStream out = new DataOutputStream(bout)) {
                out.writeInt(classes.size());
                for (String path : classes) {
                    out.writeUTF(path);
                }
            }

            return Frame.success(requestId, (byte) 0x30, bout.toByteArray());
        } catch (Exception e) {
            return Frame.error(requestId, e.getMessage());
        }
    }
}
