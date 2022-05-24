#TIS-USERMANAGEMENT

A webapp for managing users of TIS Admin products.

## Tech

It is a classic WebMVC app, using Hibernate Object Mapping, Controller and server [decorated][1] UI.
This has meant it doesn't expose a REST API like most other services.

It is built using:

- Java (8)
- Spring Boot 
- Maven
- Junit (4)
- Thymeleaf

# Running locally

1. Use Spring Boot Plugin for Maven:
    ```shell
    mvn spring-boot:run
    ```
1.  Use the docker container from the latest build on Prod
    ```shell
    docker pull ${DOCKER_REPO}:latest
    docker run 
    ```
    
[1]: https://en.wikipedia.org/wiki/Decorator_pattern