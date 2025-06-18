package io.dynaload;

import io.dynaload.scan.callable.CallableScanner;
import io.dynaload.scan.export.ClassExportScanner;
import io.dynaload.socket.SocketServer;


public class Dynaload {

    public static void main(String[] args) {
        start(9999, "io.dynaload.model.test");
    }

    public static void start(int port, String basePackage) {


        if(basePackage == null || basePackage.isBlank()){
            System.out.println("[Dynaload] Base package not defined, scan all packages :( , Please include it on @DynaloadStart");
            // Escaneia todas as classes disponíveis no classpath e registra as anotadas com @DynaloadExport
            ClassExportScanner.scanAndRegisterAll("");
            // Escaneia métodos anotados com @DynaloadCallable (invocação remota)
            CallableScanner.scanAndRegister("");

        }else{
            System.out.println("[Dynaload] Scanning dynaload configurations on " + basePackage + " base package.");
            ClassExportScanner.scanAndRegisterAll(basePackage);
            CallableScanner.scanAndRegister(basePackage);
        }



        new SocketServer().start(port);
    }
}