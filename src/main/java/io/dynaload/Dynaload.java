package io.dynaload;

import io.dynaload.core.scan.ClassScanner;
import io.dynaload.core.socket.SocketServer;
import io.dynaload.model.test.Account;

public class Dynaload {
    public static void main(String[] args) {
        // Registro manual inicial (futuramente via scanner/reflection)
        ClassScanner.register("v1/account", Account.class);

        // Inicia o socket
        new SocketServer().start(9999);
    }
}