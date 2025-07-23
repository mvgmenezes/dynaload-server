# Dynaload

**Dynaload** is a lightweight, plugin-ready remote execution framework for the JVM. It enables dynamic **remote class loading**, **binary protocol communication**, and **method invocation** via TCP sockets — without any dependency on Spring or other frameworks.

---

## What is Dynaload?

Dynaload allows Java applications to **export models, services, and methods** that can be remotely discovered, downloaded (bytecode), and executed by external clients — all using a **custom binary protocol** and a **pluggable class loading system**.

---

## ⚙Use Cases

* Plugin-based systems with dynamic code loading
* Microservice-to-microservice RPC without HTTP
* Lightweight edge/IoT services with dynamic updates
* Internal frameworks that need class distribution
* Secure sandbox environments for isolated code execution

---

## How It Works

Dynaload scans your codebase for annotated components:

* `@DynaloadExport`: exports a class (and dependencies) for dynamic loading
* `@DynaloadService`: declares a service
* `@DynaloadCallable`: marks methods for remote invocation

It runs a binary TCP server that handles:

* Remote bytecode download (`GET_CLASS`)
* Remote method calls (`INVOKE`)
* Class discovery (`LIST_CLASSES`)
* Health checks (`PING`)
* Frame-based multiplexing per connection

---

## Features

* **Dynamic bytecode distribution**
* **Remote method invocation (Java to Java)**
* **Custom binary protocol (header + opcodes)**
* **Stub interface generation via ByteBuddy**
* **No framework required (Spring optional)**
* **Extensible and modular architecture**
* **Spring Boot integration via `dynaload-spring-starter`**

---

## Quick Start

### 1. Export the JAR

```bash
./gradlew build
```

### 2. Run the Server

```bash
java -jar dynaload-server-1.0-SNAPSHOT.jar
```

### 3. Initialize Manually

```java
public class Main {
    public static void main(String[] args) {
        Dynaload.start(9999, "com.example.package");
    }
}
```

### 4. Or Automatically with Annotation

```java
@DynaloadStart(port = 9999, basePackage = "com.example.package")
public class App {
    public static void main(String[] args) {
        DynaloadAutoBootstrap.init();
    }
}
```

---

## Sample Remote Method Invocation

```java
@DynaloadService
public class UserService {
    @DynaloadCallable
    public List<User> getAllUsers() {
        return repository.findAll();
    }
}
```

Remote clients can invoke: `userService::getAllUsers`.

---

## Frame Protocol

| Field        | Type    | Description           |
| ------------ | ------- | --------------------- |
| Header       | short   | Always `0xCAFE`       |
| Request ID   | int     | Unique ID per request |
| OpCode       | byte    | Operation type        |
| Payload Size | int     | Size of payload       |
| Payload      | byte\[] | Serialized data       |

### OpCodes

```java
0x01 = GET_CLASS
0x02 = INVOKE
0x03 = LIST_CLASSES
0x04 = PING
0x05 = CLOSE
0x7F = ERROR
```

---

## Architecture Internals

* **StubInterfaceGenerator** → Generates client stubs automatically
* **ClassExportScanner** → Scans for `@DynaloadExport` and registers keys
* **CallableScanner** → Registers remote-invocable methods
* **SocketServer** → Multiplexed binary TCP server

---

## Benchmarks (Preliminary)

| Scenario                   | Avg Latency | Payload Size | Notes                    |
| -------------------------- | ----------- | ------------ | ------------------------ |
| Remote method (no params)  | \~2ms       | \~100B       | Localhost                |
| Bytecode export of class   | \~4ms       | \~1.2 KB     | 1 class + 2 dependencies |
| Invocation with I/O & JSON | \~5–10ms    | Varies       | Dependent on logic       |

> Future benchmarks with network overhead and concurrency coming soon.

---

## Roadmap

* [ ] gRPC compatibility bridge
* [ ] Remote method timeout + retry policy
* [ ] API Key / Auth handshake
* [ ] Support for Kotlin coroutines
* [ ] Remote logging / metrics

---

## License

[MIT License](./LICENSE)

---

## Related Projects

* [Dynaload Client](../dynaload-client) – Java client that connects to a Dynaload server
* [Dynaload Spring Starter](../dynaload-spring-starter) – Spring Boot auto-configuration module
* [Dynaload Protocol Spec](../protocol.md) – Binary protocol spec

