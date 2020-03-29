FROM openjdk:8-alpine

COPY target/uberjar/git-stats.jar /git-stats/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/git-stats/app.jar"]
