package io.dynaload.frame;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class FrameReader {
    private static final short MAGIC_HEADER = (short) 0xCAFE;

    public static Frame readFrame(InputStream in) throws IOException {
        DataInputStream dataIn = new DataInputStream(in);
        try {
            short header = dataIn.readShort();
            if (header != MAGIC_HEADER) throw new IOException("Invalid header");

            int requestId = dataIn.readInt();
            byte opCode = dataIn.readByte();
            int length = dataIn.readInt();
            byte[] payload = new byte[length];
            dataIn.readFully(payload);

            return new Frame(requestId, opCode, payload);
        } catch (EOFException eof) {
            return null;
        }
    }
}