# Build container.
FROM gradle:jdk11 as build

# Copy contents of repository to container and build JAR.
WORKDIR /app
COPY . .

RUN gradle clean shadowJar

# Primary container.
FROM openjdk:13-jdk-alpine

# Add healthcheck dependency.
RUN apk add curl

# Copy JAR from build container.
WORKDIR /app
COPY --from=build /app/build/libs/voyager-1.0-all.jar voyager.jar

# Expose volume with web pages.
VOLUME /data

ENTRYPOINT [ "java", "-jar", "voyager.jar" ]

EXPOSE 5000

# Supply any arguments here. These are the defaults.
CMD [ "/data", "--port=5000" ]