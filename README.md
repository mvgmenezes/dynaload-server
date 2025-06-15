# export jar
./gradlew build
# start jar
java -jar dynaload-server-1.0-SNAPSHOT.jar

# publish jar to maven local 
./gradlew build
./gradlew publishToMavenLocal

# run server sh 
open a new terminal
./run-server.sh

@RegistryExportable
@RegistryInstance
@RegistryCallable

