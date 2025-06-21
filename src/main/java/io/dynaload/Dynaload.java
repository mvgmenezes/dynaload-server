package io.dynaload;

import io.dynaload.scan.callable.CallableScanner;
import io.dynaload.scan.export.ClassExportScanner;
import io.dynaload.socket.SocketServer;
import io.dynaload.stub.StubInterfaceGenerator;


public class Dynaload {

    public static void main(String[] args) {
        start(9999, "io.dynaload.model.test");
    }

    public static void start(int port, String basePackage) {


        if(basePackage == null || basePackage.isBlank()){
            System.out.println("[Dynaload] Base package not defined, scan all packages :( , Please include it on @DynaloadStart");

            // 1. Escaneia e gera interfaces de stubs (Services e Methods remotes)
            StubInterfaceGenerator.generateStubsForExportable("");
            // Escaneia todas as classes disponíveis no classpath e registra as anotadas com @DynaloadExport
            ClassExportScanner.scanAndRegisterAll("");
            // Escaneia métodos anotados com @DynaloadCallable (invocação remota)
            CallableScanner.scanAndRegister("");

        }else{
            System.out.println("[Dynaload] Scanning dynaload configurations on " + basePackage + " base package.");
            // 1. Escaneia e gera interfaces de stubs (Services e Methods remotes)
            StubInterfaceGenerator.generateStubsForExportable(basePackage);
            ClassExportScanner.scanAndRegisterAll(basePackage);
            CallableScanner.scanAndRegister(basePackage);
        }

        new SocketServer().start(port);
    }
}