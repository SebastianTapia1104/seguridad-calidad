# Usa una imagen base de Java (por ejemplo, OpenJDK)
FROM eclipse-temurin:17-jdk-focal
# Definimos el directorio de trabajo
WORKDIR /app
# Copia tu archivo JAR (por ejemplo, miapp.jar) al contenedor
COPY target/backend-0.0.1-SNAPSHOT.jar backend.jar
# Expone el puerto en el que se ejecutará tu aplicación
EXPOSE 8080
# Comando para ejecutar tu aplicación (ajusta según tu caso)
CMD ["java", "-jar", "backend.jar"]