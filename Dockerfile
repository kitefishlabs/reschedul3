FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/reschedul2.jar /reschedul2/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/reschedul2/app.jar"]
