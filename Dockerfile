# This image builds
FROM ubuntu:bionic as server-build

WORKDIR /app

RUN apt-get update && apt-get install -y openjdk-11-jre-headless && apt-get clean

COPY gradlew ./
COPY gradle gradle/
RUN ./gradlew --version

COPY build.gradle* ./
RUN ./gradlew deps

COPY . ./
RUN ./gradlew jar


# The final image - Bellsoft alpine OpenJDK images are the smallest
FROM bellsoft/liberica-openjdk-alpine:11 as final
RUN adduser -S user

WORKDIR /app
COPY --from=server-build /app/build/libs ./

# Run under non-privileged user with minimal write permissions
USER user

ENV JAVA_OPTS="-Xmx330m -Xss512k"
CMD java $JAVA_OPTS -jar app.jar