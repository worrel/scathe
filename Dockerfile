FROM hseeberger/scala-sbt:8u151-2.12.4-1.1.0 as build

WORKDIR /
COPY . /code
WORKDIR /code
RUN sbt assembly



FROM openjdk:8

RUN mkdir /app
COPY --from=build /code/target/scala-2.12/scathe-all.jar /app/scathe-all.jar
WORKDIR /app