FROM eclipse-temurin:21-jdk-jammy

ARG DB_URL
ARG DB_USER
ARG DB_PASS
ARG BUCKET_ENDPOINT
ARG REGION
ARG BUCKET_NAME
ARG BUCKET_ACCESS_KEY_ID
ARG BUCKET_SECRET_ACCESS_KEY
ARG STRIPE_SECRET_KEY

WORKDIR /app

COPY .mvn/ .mvn/
COPY --chmod=0755 mvnw mvnw
COPY ./src src/
COPY pom.xml pom.xml

ENV DB_URL=$DB_URL
ENV DB_USER=$DB_USER
ENV DB_PASS=$DB_PASS
ENV BUCKET_ENDPOINT=$BUCKET_ENDPOINT
ENV REGION=$REGION
ENV BUCKET_NAME=$BUCKET_NAME
ENV BUCKET_ACCESS_KEY_ID=$BUCKET_ACCESS_KEY_ID
ENV BUCKET_SECRET_ACCESS_KEY=$BUCKET_SECRET_ACCESS_KEY
ENV STRIPE_SECRET_KEY=$STRIPE_SECRET_KEY

RUN ./mvnw package -DskipTests && \
     mv target/$(./mvnw help:evaluate -Dexpression=project.artifactId -q -DforceStdout)-$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout).jar target/app.jar

ENTRYPOINT ["java", "-jar", "target/app.jar"]
