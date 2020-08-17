#FROM fabric8/java-alpine-openjdk11-jre
FROM fabric8/java-centos-openjdk11-jre
ARG JAR_FILE=target/azkarra-getting-started-1.0-SNAPSHOT.jar
COPY ${JAR_FILE} deployments/app.jar
