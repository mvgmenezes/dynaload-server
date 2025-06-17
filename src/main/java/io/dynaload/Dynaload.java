package io.dynaload;

import io.dynaload.scan.ClassScanner;
import io.dynaload.socket.SocketServer;


public class Dynaload {

    public static void main(String[] args) {
        start(9999);
    }

    public static void start(int port) {
        // Escaneia todas as classes disponíveis no classpath e registra as anotadas com @DynaloadExport
        ClassScanner.scanAndRegisterAll();
        new SocketServer().start(port);
    }
}