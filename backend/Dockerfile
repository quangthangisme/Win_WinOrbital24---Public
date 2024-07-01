FROM eclipse-temurin:21-alpine AS backend-build
WORKDIR /app
COPY backend/.mvn .mvn
COPY backend/mvnw backend/pom.xml ./
RUN ./mvnw dependency:go-offline
COPY backend/src ./src
RUN ./mvnw clean install -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/target/*.jar /app/backend.jar
EXPOSE 8080

CMD ["java", "-jar", "/app/backend.jar"]
