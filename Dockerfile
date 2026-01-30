FROM eclipse-temurin:21-jdk-jammy

ARG DB_URL
ARG DB_USER
ARG DB_PASS


WORKDIR /app
COPY .mvn/ .mvn/
COPY --chmod=0755 mvnw mvnw
COPY ./src src/
COPY pom.xml pom.xml
ENV DB_URL=$DB_URL
ENV DB_USER=$DB_USER
ENV DB_PASS=$DB_PASS
RUN ./mvnw package -DskipTests && \
     mv target/$(./mvnw help:evaluate -Dexpression=project.artifactId -q -DforceStdout)-$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout).jar target/app.jar

ENTRYPOINT ["java", "-jar", "target/app.jar"]
