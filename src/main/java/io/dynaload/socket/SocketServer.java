package io.dynaload.socket;


import io.dynaload.frame.Frame;
import io.dynaload.frame.FrameDispatcher;
import io.dynaload.frame.FrameReader;
import io.dynaload.frame.FrameWriter;
import io.dynaload.scan.callable.CallableRegistry;
import io.dynaload.scan.export.ClassExportScanner;

import java.io.*;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServer {

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final FrameDispatcher dispatcher = new FrameDispatcher(); // instância criada aqui

    public void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[Dynaload] Server started on port " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("[Dynaload] Connection from: " + socket.getInetAddress());
                new Thread(() -> handleClient(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket socket) {
        try {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            while (true) {
                Frame frame;
                try {
                    frame = FrameReader.readFrame(in);
                    if (frame == null) break;
                } catch (Exception e) {
                    System.err.println("[Dynaload] Invalid frame or header: " + e.getMessage());
                    sendFramingError(out, e);
                    break; // Sai do loop, vai para o finally
                }

                if (frame.opCode == 0x02) { // INVOKE mantém conexão
                    executor.submit(() -> {
                        Frame response = dispatcher.dispatch(frame);
                        try {
                            FrameWriter.writeFrame(out, response);
                        } catch (IOException e) {
                            System.err.println("[Dynaload] Failed to write response: " + e.getMessage());
                        }
                    });
                } else {
                    // Tratamento para GET_CLASS, LIST_CLASSES, PING - fecha após resposta
                    Frame response = dispatcher.dispatch(frame);
                    FrameWriter.writeFrame(out, response);
                    break;
                }
            }

            socket.close();

        } catch (IOException e) {
            System.err.println("[Dynaload] Error on client connection: " + e.getMessage());
        }
    }

    private void sendFramingError(OutputStream out, Exception e) {
        try {
            DataOutputStream dout = new DataOutputStream(out);
            ByteArrayOutputStream payloadBuffer = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(payloadBuffer);
            oos.writeUTF("ERROR");
            oos.writeObject("Invalid header: " + e.getMessage());
            byte[] payload = payloadBuffer.toByteArray();

            dout.writeShort(0xCAFE);        // magic
            dout.writeInt(-1);              // requestId inválido
            dout.writeByte(0x7F);           // opCode reservado para erro de framing
            dout.writeInt(payload.length);  // tamanho do payload
            dout.write(payload);
            dout.flush();
        } catch (IOException ioException) {
            System.err.println("[Dynaload] Failed to send error to client: " + ioException.getMessage());
        }
    }
//    public void start(int port) {
//        try (ServerSocket serverSocket = new ServerSocket(port)) {
//            System.out.println("[Dynaload] Server started on port " + port);
//            while (true) {
//                Socket socket = serverSocket.accept();
//                System.out.println("[Dynaload] Receiving request from: " + socket.getInetAddress());
//                new Thread(() -> handleClient(socket)).start();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void handleClient(Socket socket) {
//        //DataInputStream in – usado para ler dados que o cliente envia
//        //DataOutputStream out – usado para enviar dados de volta ao cliente
//        try (DataInputStream receivedClient = new DataInputStream(socket.getInputStream());
//             DataOutputStream sendClient = new DataOutputStream(socket.getOutputStream())) {
//
//            while(true) {
//                String command;
//                try {
//                    command = receivedClient.readUTF();
//                } catch (EOFException eof) {
//                    System.out.println("[Dynaload] Client closed connection.");
//                    break;
//                }
//
//                switch (command) {
//                    case "GET_CLASS" -> handleGetClass(receivedClient, sendClient);
//                    case "PING" -> sendClient.writeUTF("PONG");
//                    case "LIST_CLASSES" -> handleListClasses(sendClient);
//                    case "INVOKE" -> handleInvoke(receivedClient, sendClient);
//                    case "CLOSE" -> {
//                        sendClient.writeUTF("CLOSED");
//                        System.out.println("[Dynaload] Client requested close.");
//                        return;
//                    }
//                    default -> sendClient.writeUTF("UNKNOWN_COMMAND");
//                }
//            }
//
//        } catch (Exception e) {
//            System.err.println("[Dynaload] Error: " + e.getMessage());
//        }
//    }

//    private void handleListClasses(DataOutputStream out) throws IOException {
//        var classes = ClassExportScanner.getRegisteredKeys();
//        out.writeInt(classes.size());
//        for (String path : classes) {
//            out.writeUTF(path);
//        }
//    }
//
//    private void handleGetClass(DataInputStream in, DataOutputStream out) throws IOException {
//        //O servidor lê uma string enviada pelo cliente, no formato UTF-8. "v1/account"
//        String path = in.readUTF();
//        //O servidor procura se tem uma classe registrada para esse path.
//        Class<?> clazz = ClassExportScanner.get(path);
//        if (clazz == null) {
//            out.writeInt(-1); // not found
//            return;
//        }
//
//        //Converte o nome da classe em um caminho de arquivo .class.
//        String resourcePath = clazz.getName().replace('.', '/') + ".class";
//
//        byte[] bytecode;
//        // Tenta abrir o arquivo .class da classe como um recurso.
//        try (InputStream stream = clazz.getClassLoader().getResourceAsStream(resourcePath)) {
//            if (stream == null) {
//                out.writeInt(-2); // can't load bytecode
//                return;
//            }
//            //le o conteudo do .class para um array de bytes
//            bytecode = stream.readAllBytes();
//        }
//        //Informa o nome do className
//        out.writeUTF(clazz.getName());
//        //Informa ao cliente quantos bytes ele vai receber.
//        out.writeInt(bytecode.length);
//        //Envia os bytes do .class compilado para o cliente.
//        out.write(bytecode);
//    }
//
//    private void handleInvoke(DataInputStream in, DataOutputStream out) throws IOException, ClassNotFoundException {
//        String methodId = in.readUTF(); // Ex: "accountService.getAll"
//        int argCount = in.readInt();
//        Object[] args = new Object[argCount];
//
//        // Use um único ObjectInputStream para desserializar todos os argumentos
//        ObjectInputStream objectIn = new ObjectInputStream(in);
//        for (int i = 0; i < argCount; i++) {
//            args[i] = objectIn.readObject();
//        }
//
//        try {
//            // Recupera e executa o método
//            Method method = CallableRegistry.getMethod(methodId);
//            Object target = CallableRegistry.getInstance(methodId);
//
//            if (method == null || target == null) {
//                throw new IllegalStateException("Method or target not found for ID: " + methodId);
//            }
//
//            Object result = method.invoke(target, args);
//
//            out.writeUTF("SUCCESS");
//
//            ObjectOutputStream objectOut = new ObjectOutputStream(out);
//            objectOut.writeObject(result);
//            objectOut.flush(); // garante que os bytes saiam imediatamente
//        } catch (Exception e) {
//            out.writeUTF("ERROR");
//
//            ObjectOutputStream objectOut = new ObjectOutputStream(out);
//            objectOut.writeObject(e.getMessage());
//            objectOut.flush();
//        }
//    }
}