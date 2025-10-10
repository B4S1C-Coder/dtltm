FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests=true

FROM gcr.io/distroless/java21:latest AS runner

WORKDIR /app
COPY --from=builder ./app/target/dtltm-0.0.1-SNAPSHOT.jar ./app.jar
EXPOSE 20001
ENTRYPOINT ["java", "-jar", "app.jar"]