FROM docker.io/azul/zulu-openjdk-alpine:21
WORKDIR /app

# Cache dependencies separately so they don't re-download on every code change
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN ./mvnw dependency:go-offline -B

COPY src ./src

EXPOSE 8080 35729
ENTRYPOINT ["./mvnw", "spring-boot:run"]