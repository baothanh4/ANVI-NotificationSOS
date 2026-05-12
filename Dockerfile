FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# EXPOSE is ignored by Render but useful for documentation
EXPOSE 8081

CMD ["java", "-jar", "-Dserver.port=${PORT:8081}", "target/ANVI-SOS-0.0.1-SNAPSHOT.jar"]