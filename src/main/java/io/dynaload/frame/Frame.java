package io.dynaload.frame;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class Frame {
    public final int requestId;
    public final byte opCode;
    public final byte[] payload;

    public Frame(int requestId, byte opCode, byte[] payload) {
        this.requestId = requestId;
        this.opCode = opCode;
        this.payload = payload;
    }

    public static Frame success(int requestId, byte opCode, byte[] payload) {
        return new Frame(requestId, opCode, payload);
    }

    public static Frame error(int requestId, String message) {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            try (ObjectOutputStream out = new ObjectOutputStream(bout)) {
                out.writeUTF("ERROR");
                out.writeObject(message);
            }
            return new Frame(requestId, (byte) 0x21, bout.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to encode error frame", e);
        }
    }
}
