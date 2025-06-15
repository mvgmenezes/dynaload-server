plugins {
    id("java")
    id("maven-publish") // <-- Public on Maven repo
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        //vendor.set(JvmVendorSpec.ADOPTOPENJDK) // ou tem outras opções: GRAAL_VM, ORACLE, etc.
    }
}

group = "io.dynaload"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()  // Puxa do ~/.m2
    mavenCentral()
}

dependencies {
    dependencies {
        implementation("io.github.classgraph:classgraph:4.8.165")
        implementation("io.dynaload:dynaload-server:1.0-SNAPSHOT")
    }
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "io.dynaload.Dynaload"
    }

    from(sourceSets.main.get().output)

    //(fat jar):
//    dependsOn(configurations.runtimeClasspath)
//    from({
//        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
//    })
    // Fat JAR: inclui todas as dependências exceto ele mesmo
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith("jar") && !it.name.startsWith("dynaload-server") }
            .map { zipTree(it) }
    })
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            groupId = "io.dynaload"
            artifactId = "dynaload-server"
            version = "1.0-SNAPSHOT"
        }
    }
    repositories {
        mavenLocal()
    }
}