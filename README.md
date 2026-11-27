#TIS-USERMANAGEMENT

A service for managing users of TIS Admin products.

## Tech

It is a Spring Boot API service exposing REST endpoints for user management operations.

It is built using:

- Java (11)
- Spring Boot 
- Maven
- Junit
- Spring Security

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
