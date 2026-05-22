# syntax=docker/dockerfile:1

FROM gradle:8.14.3-jdk17 AS build
WORKDIR /app

COPY build.gradle settings.gradle ./
COPY src src

RUN gradle bootJar --no-daemon

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN groupadd --system --gid 10001 app \
	&& useradd --system --uid 10001 --gid app --no-create-home --shell /usr/sbin/nologin app

COPY --from=build /app/build/libs/admin-*-SNAPSHOT.jar /app/app.jar

USER 10001

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
