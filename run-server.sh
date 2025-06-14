#!/bin/bash

# Caminho do JAR
JAR_FILE="build/libs/dynaload-server-1.0-SNAPSHOT.jar"

# Verifica se o arquivo existe
if [ ! -f "$JAR_FILE" ]; then
  echo "[Error] JAR file not found: $JAR_FILE"
  exit 1
fi

# Executa o servidor
echo "[Starting Dynaload Server...]"
java -jar "$JAR_FILE"