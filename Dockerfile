FROM eclipse-temurin:21-jdk-jammy

ARG DATABASE_URL
ARG DATABASE_USER
ARG DATABASE_PASS


WORKDIR /app
COPY .mvn/ .mvn/
COPY --chmod=0755 mvnw mvnw
COPY ./src src/
COPY pom.xml pom.xml
ENV DB_URL="jdbc:postgresql://localhost:5432/menuonline"
ENV DB_USER="user"
ENV DB_PASS="pass"
RUN ./mvnw package -DskipTests && \
     mv target/$(./mvnw help:evaluate -Dexpression=project.artifactId -q -DforceStdout)-$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout).jar target/app.jar

ENTRYPOINT ["java", "-jar", "target/app.jar"]
