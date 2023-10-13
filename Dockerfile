FROM alpine:latest AS build

RUN apk update
RUN apk add openjdk-17-jdk -y

COPY . .

RUN apk add maven -y
RUN mvn clean install

EXPOSE 8080

COPY --from=build /target/todolist-0.0.1.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]