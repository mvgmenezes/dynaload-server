package io.dynaload.frame;

import io.dynaload.util.DynaloadOpCodes;
import io.dynaload.scan.callable.CallableRegistry;
import io.dynaload.scan.export.ClassExportScanner;

import java.io.*;
import java.lang.reflect.Modifier;


public class FrameDispatcher {

    public Frame dispatch(Frame frame) {
        try {
            int requestId = frame.requestId;
            byte opCode = frame.opCode;
            byte[] payload = frame.payload;

            return switch (opCode) {
                case DynaloadOpCodes.GET_CLASS -> handleGetClass(payload, requestId);
                case DynaloadOpCodes.INVOKE -> handleInvoke(payload, requestId);
                case DynaloadOpCodes.LIST_CLASSES -> handleListClasses(requestId);
                case DynaloadOpCodes.PING -> handlePing(requestId);        // ping
                case DynaloadOpCodes.CLOSE -> handleClose(requestId);
                default -> Frame.error(requestId, "Unknown opCode: " + opCode);
            };
        } catch (Exception e) {
            e.printStackTrace();
            return Frame.error(frame.requestId, e.getMessage());
        }
    }

    private Frame handlePing(int requestId) {
        return Frame.success(requestId, DynaloadOpCodes.PONG, new byte[0]); // resposta PING
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

            return Frame.success(requestId, DynaloadOpCodes.GET_CLASS_RESPONSE, bout.toByteArray());

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
            if (method == null) {
                return Frame.error(requestId, "Method not found: " + methodId);
            }

            var instance = CallableRegistry.getInstance(methodId);
            if (instance == null && !Modifier.isStatic(method.getModifiers())) {
                return Frame.error(requestId, "Instance not registered: " + methodId);
            }
            Object result = method.invoke(instance, args);

            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            try (ObjectOutputStream out = new ObjectOutputStream(bout)) {
                out.writeUTF("SUCCESS");
                out.writeObject(result);
            }

            return Frame.success(requestId, DynaloadOpCodes.INVOKE_RESPONSE, bout.toByteArray());

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

            return Frame.success(requestId, DynaloadOpCodes.LIST_CLASSES_RESPONSE, bout.toByteArray());
        } catch (Exception e) {
            return Frame.error(requestId, e.getMessage());
        }
    }

    private Frame handleClose(int requestId) {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            try (DataOutputStream out = new DataOutputStream(bout)) {
                out.writeUTF("CLOSED");
            }
            return Frame.success(requestId, DynaloadOpCodes.CLOSED_RESPONSE, bout.toByteArray());
        } catch (Exception e) {
            return Frame.error(requestId, e.getMessage());
        }
    }
}
