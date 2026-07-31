# ---- Stage 1: Build ----
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
RUN apk add --no-cache maven
COPY . .
RUN mvn -pl cards-bff-dashboard-service -am clean package -DskipTests -B

# ---- Stage 2: Runtime ----
FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
WORKDIR /app
COPY --from=build /app/cards-bff-dashboard-service/target/cards-bff-dashboard-service-*.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "app.jar"]
