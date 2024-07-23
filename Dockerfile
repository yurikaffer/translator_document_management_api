# Usa a imagem base do JDK
FROM openjdk:17-jdk-slim

# Instala o Maven
RUN apt-get update && apt-get install -y maven

# Define o diretório de trabalho
WORKDIR /app

# Copia o arquivo pom.xml e baixa as dependências do Maven
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código-fonte do projeto
COPY src ./src

# Compila o projeto
RUN mvn clean package -DskipTests

# Expõe a porta do Spring Boot
EXPOSE 8080

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "target/translator_document_management-0.0.1-SNAPSHOT.jar"]
