package io.dynaload.socket;


import io.dynaload.scan.ClassScanner;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer {

    public void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[Dynaload] Server started on port " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("[Dynaload] Receiving request from: " + socket.getInetAddress());
                new Thread(() -> handleClient(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket socket) {
        //DataInputStream in – usado para ler dados que o cliente envia
        //DataOutputStream out – usado para enviar dados de volta ao cliente
        try (DataInputStream receivedClient = new DataInputStream(socket.getInputStream());
             DataOutputStream sendClient = new DataOutputStream(socket.getOutputStream())) {

            while(true) {
                String command;
                try {
                    command = receivedClient.readUTF();
                } catch (EOFException eof) {
                    System.out.println("[Dynaload] Client closed connection.");
                    break;
                }

                switch (command) {
                    case "GET_CLASS" -> handleGetClass(receivedClient, sendClient);
                    case "PING" -> sendClient.writeUTF("PONG");
                    case "LIST_CLASSES" -> handleListClasses(sendClient);
                    case "CLOSE" -> {
                        sendClient.writeUTF("CLOSED");
                        System.out.println("[Dynaload] Client requested close.");
                        return;
                    }
                    default -> sendClient.writeUTF("UNKNOWN_COMMAND");
                }
            }

        } catch (Exception e) {
            System.err.println("[Dynaload] Error: " + e.getMessage());
        }
    }

    private void handleListClasses(DataOutputStream out) throws IOException {
        var classes = ClassScanner.getRegisteredKeys();
        out.writeInt(classes.size());
        for (String path : classes) {
            out.writeUTF(path);
        }
    }

    private void handleGetClass(DataInputStream in, DataOutputStream out) throws IOException {
        //O servidor lê uma string enviada pelo cliente, no formato UTF-8. "v1/account"
        String path = in.readUTF();
        //O servidor procura se tem uma classe registrada para esse path.
        Class<?> clazz = ClassScanner.get(path);
        if (clazz == null) {
            out.writeInt(-1); // not found
            return;
        }

        //Converte o nome da classe em um caminho de arquivo .class.
        String resourcePath = clazz.getName().replace('.', '/') + ".class";

        byte[] bytecode;
        // Tenta abrir o arquivo .class da classe como um recurso.
        try (InputStream stream = clazz.getClassLoader().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                out.writeInt(-2); // can't load bytecode
                return;
            }
            //le o conteudo do .class para um array de bytes
            bytecode = stream.readAllBytes();
        }
        //Informa o nome do className
        out.writeUTF(clazz.getName());
        //Informa ao cliente quantos bytes ele vai receber.
        out.writeInt(bytecode.length);
        //Envia os bytes do .class compilado para o cliente.
        out.write(bytecode);
    }
}