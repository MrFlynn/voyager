FROM openjdk:13-jdk-alpine

ADD . ./
EXPOSE 8080

ENTRYPOINT ["java","-jar","build/libs/voyager-1.0-all.jar","babyscrape","-p=8080"]