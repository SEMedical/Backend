FROM openjdk:18-jdk-alpine
COPY ./target/app.jar /tmp/app.jar
ENTRYPOINT java -jar /tmp/app.jar -Xmx9093m -Xms9083m -Xmn4096m -XX:+UseG1GC  -XX:MetaspaceSize=96m