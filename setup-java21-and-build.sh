#!/bin/bash

echo "🔍 Verificando instalações Java disponíveis..."
/usr/libexec/java_home -V 2>/dev/null | grep "Java SE 21" || {
  echo "JDK 21 não encontrado no sistema."
  echo "Instale o JDK 21 (ex: Temurin ou Oracle) e tente novamente."
  exit 1
}

# Extrai o path do JDK 21
JAVA_21_PATH=$(/usr/libexec/java_home -v 21)

if [ -z "$JAVA_21_PATH" ]; then
  echo "Não foi possível detectar o JAVA_HOME do JDK 21."
  exit 1
fi

echo "JDK 21 encontrado em: $JAVA_21_PATH"
export JAVA_HOME="$JAVA_21_PATH"
echo "🌱 JAVA_HOME exportado."

echo "🛠️ Executando Gradle clean build com Java 21..."
./gradlew clean build --info

BUILD_STATUS=$?

if [ $BUILD_STATUS -eq 0 ]; then
  echo "Build finalizado com sucesso."
else
  echo "Build falhou. Veja os logs acima para mais detalhes."
fi