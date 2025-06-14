#!/bin/bash

set -e

echo "🔍 Verificando se o Homebrew está instalado..."
if ! command -v brew &> /dev/null; then
  echo "Homebrew não encontrado. Instale com: /bin/bash -c \"\$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)\""
  exit 1
fi

echo "Homebrew está instalado."

echo "Atualizando fórmulas do Homebrew..."
brew update

echo "Instalando OpenJDK 21 (Temurin)..."
brew install temurin@21

echo "Localizando caminho do Java 21..."
JAVA_PATH=$(/usr/libexec/java_home -v 21)

if [ -z "$JAVA_PATH" ]; then
  echo "Java 21 não encontrado após instalação."
  exit 1
fi

echo "Java 21 instalado em: $JAVA_PATH"

echo "💾 Atualizando ~/.zshrc (ou ~/.bashrc) com JAVA_HOME..."
SHELL_RC="$HOME/.zshrc"
if [ "$SHELL" == "/bin/bash" ]; then
  SHELL_RC="$HOME/.bashrc"
fi

{
  echo ""
  echo "# >>> OpenJDK 21"
  echo "export JAVA_HOME=$JAVA_PATH"
  echo "export PATH=\$JAVA_HOME/bin:\$PATH"
  echo "# <<< OpenJDK 21"
} >> "$SHELL_RC"

echo "JAVA_HOME e PATH configurados em $SHELL_RC"

echo "♻Para aplicar agora, execute:"
echo "source $SHELL_RC"

echo "Validando versão do Java:"
JAVA_HOME=$JAVA_PATH $JAVA_PATH/bin/java -version