# My-Blog-Back-App

Spring Boot blog web application backend (Java 21, embedded Tomcat) serving a REST API consumed by an Nginx-hosted frontend. Uses H2 in-memory database and plain Spring JDBC.

## Technology Stack

| Component         | Technology              | Version                  |
|-------------------|-------------------------|--------------------------|
| Language          | Java                    | 21                       |
| Framework         | Spring Boot             | 3.5.13                   |
| Database          | H2 (in-memory)          | managed by Spring Boot   |
| Data access       | Spring JDBC             | managed by Spring Boot   |
| JSON              | Jackson                 | managed by Spring Boot   |
| Servlet container | Embedded Tomcat         | managed by Spring Boot   |
| Build tool        | Apache Maven            | 3.8.7                    |
| Testing           | JUnit 5, Mockito, Hamcrest | managed by Spring Boot |

## Build & Test

```bash
./mvnw clean package               # compile + test + produce JAR
./mvnw test                        # run tests only
./mvnw clean package -DskipTests   # skip tests
```

Output: `target/my-blog-back-app.jar`

## Run

```bash
# Run with Maven (development)
./mvnw spring-boot:run

# Or run the JAR directly
java -jar target/my-blog-back-app.jar
```

The app starts on `http://localhost:8080/`.

> **Note:** The H2 database is in-memory — all data is lost when the application stops.

REST verification examples:

```bash
# Create a post
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{"title":"Hello World","text":"My first post.","tags":["intro"]}'

# List posts (page 1, 5 per page)
curl "http://localhost:8080/api/posts?search=&pageNumber=1&pageSize=5"

# Get a single post (replace 1 with the returned id)
curl http://localhost:8080/api/posts/1

# Add a comment
curl -X POST http://localhost:8080/api/posts/1/comments \
  -H "Content-Type: application/json" \
  -d '{"text":"Great post!","postId":1}'

# Like a post
curl -X POST http://localhost:8080/api/posts/1/likes

# Delete the post
curl -X DELETE http://localhost:8080/api/posts/1
```

## H2 DB Remote Access

The H2 TCP server starts on port 9092 when the app runs outside the `test` profile. Connect with any JDBC client:

- **URL:** `jdbc:h2:tcp://localhost:9092/mem:blogdb`
- **Username:** `sa`
- **Password:** *(empty)*

Example connection using a built-in H2 shell tool:

```bash
java -cp ~/.m2/repository/com/h2database/h2/2.3.232/h2-2.3.232.jar org.h2.tools.Shell \
  -url "jdbc:h2:tcp://localhost:9092/mem:blogdb" \
  -user sa \
  -password ""
```
