FROM maven:3.9.2-eclipse-temurin-20 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:20-jdk-alpine
WORKDIR /app

COPY --from=build /app/target/securenotes-1.0.0.jar ./securenotes.jar

EXPOSE 8080
CMD ["java", "-jar", "securenotes.jar"]