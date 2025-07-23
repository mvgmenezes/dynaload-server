
# Dynaload Server

**Dynaload Server** is a lightweight, framework-independent runtime designed to dynamically expose Java classes, interfaces, and methods over TCP sockets. It supports bytecode export, remote method invocation, and service discovery using a custom binary protocol.

---

## Build & Run

### Export the JAR

```bash
./gradlew build
```

### Run the Server

```bash
java -jar dynaload-server-1.0-SNAPSHOT.jar
```

### Publish to Maven Local

```bash
./gradlew build
./gradlew publishToMavenLocal
```

### Start via Shell Script

Open a new terminal:

```bash
./run-server.sh
```

---

## How It Works

Dynaload Server scans the classpath for:

* `@DynaloadExport`: Marks a class for remote bytecode export
* `@DynaloadService` + `@DynaloadCallable`: Marks methods for remote invocation

It opens a custom TCP socket and uses a binary protocol based on structured frames. Each command (e.g., `GET_CLASS`, `INVOKE`, `LIST_CLASSES`) is identified by a specific OpCode.

---

## Getting Started

### Manual Initialization

```java
public class Main {
    public static void main(String[] args) {
        Dynaload.start(9999, "com.example.package");
    }
}
```

### Automatic Initialization Using Annotation

```java
@DynaloadStart(basePackage = "com.example.package", port = 9999)
public class Application {
    public static void main(String[] args) {
        DynaloadAutoBootstrap.init();
    }
}
```

> The server runs in a separate thread named `Dynaload-Server-Thread`.

---

## Supported Annotations

### `@DynaloadExport`

```java
@DynaloadExport(value = "v1/account", includeDependencies = { Address.class })
public class Account {
  ...
}
```

Exports the class for remote loading via `GET_CLASS`. Also allows declaring dependent classes to be exported together.

### `@DynaloadService` + `@DynaloadCallable`

```java
@DynaloadService
public class UserService {

    @DynaloadCallable
    public List<User> getAllUsers() {
        return repository.findAll();
    }
}
```

Marks the method for remote execution via `INVOKE`.

### `@DynaloadStart`

```java
@DynaloadStart(port = 9999, basePackage = "com.myapp")
```

Optional. Enables auto-starting the server using `DynaloadAutoBootstrap.init()`.

---

## Frame Structure

| Field        | Type     | Description                        |
| ------------ | -------- | ---------------------------------- |
| Header       | `short`  | Always `0xCAFE`                    |
| Request ID   | `int`    | Identifier for the client request  |
| OpCode       | `byte`   | Operation type (e.g., `GET_CLASS`) |
| Payload Size | `int`    | Size of the binary payload         |
| Payload      | `byte[]` | Serialized data                    |

### OpCodes

```java
DynaloadOpCodes.GET_CLASS    = 0x01
DynaloadOpCodes.INVOKE       = 0x02
DynaloadOpCodes.LIST_CLASSES = 0x03
DynaloadOpCodes.PING         = 0x04
DynaloadOpCodes.CLOSE        = 0x05
DynaloadOpCodes.ERROR        = 0x7F
```

---

## Internal Components

### `StubInterfaceGenerator`

Generates `@RemoteService` interfaces automatically for any class annotated with `@DynaloadService` and `@DynaloadCallable`, using ByteBuddy. Interfaces are exported under the `io.dynaload.remote.service` package.

### `ClassExportScanner`

Scans and registers classes annotated with `@DynaloadExport`. Each class can be retrieved using its assigned export key (e.g., `v1/account`).

### `CallableScanner`

Scans and registers callable methods annotated with `@DynaloadCallable` inside services annotated with `@DynaloadService`.

### `SocketServer`

Implements a multiplexed TCP socket server. Each client can issue multiple requests over a single connection. Commands such as `INVOKE` support persistent connections, while others like `GET_CLASS` close after the response.

---

## Example: Exporting a Class with Dependencies

```java
@DynaloadExport(value = "v1/user", includeDependencies = { Address.class, Role.class })
public class User implements Serializable {
  ...
}
```

---

## Current Limitations

* No authentication/authorization (planned: API Key support)
* Only supports native Java serialization (ObjectInputStream)
* Only `public` and serializable methods are supported
* `INVOKE` and `GET_CLASS` require all dependencies to be registered or already visible

---

## Debug Tip

Use the `LIST_CLASSES` command to verify which classes have been successfully registered and exported by the server.

---

## Caution

Avoid running `Dynaload.start(...)` on the main thread of a Spring Boot application. Use a new thread (`new Thread(...).start()`) or `@PostConstruct` with `@Async` for embedded setups.

---

## Example Project Structure

```
project-root/
├── src/
│   └── main/java/
│       ├── com/example/model/User.java
│       ├── com/example/service/UserService.java
│       └── com/example/Main.java
├── build/dynaload/
│   └── io/dynaload/remote/service/UserService.class
```

---

## See Also

* [Dynaload Client](../dynaload-client)
* [Dynaload Spring Starter](../dynaload-spring-starter) (optional)
* [Dynaload Protocol Spec](../protocol.md)

