# My-Blog-Back-App

Spring MVC blog web application backend (Java 21, WAR packaged) serving a REST API consumed by an Nginx-hosted frontend. Uses H2 in-memory database, plain Spring JDBC, deployed on Tomcat 10.1.x.

## Technology Stack

| Component         | Technology              | Version   |
|-------------------|-------------------------|-----------|
| Language          | Java                    | 21        |
| Framework         | Spring Web MVC          | 6.2.16    |
| Database          | H2 (in-memory)          | 2.2.224   |
| Data access       | Spring JDBC             | 3.5.9     |
| JSON              | Jackson                 | 2.19.4    |
| Servlet API       | Jakarta Servlet         | 6.0.0     |
| Servlet container | Apache Tomcat           | 10.1.x    |
| Build tool        | Apache Maven            | 3.8.7     |
| Testing           | JUnit 5, Mockito, Hamcrest | —      |

## Build & Test

```bash
./mvnw clean package               # compile + test + produce WAR
./mvnw test                        # run tests only
./mvnw clean package -DskipTests   # skip tests
```

Output: `target/my-blog-back-app.war`

## Deploy to Tomcat 10.1.x

The Nginx frontend is configured to send API requests to `http://localhost:8080/`, so the app must be deployed on Tomcat at the root context path (`/`). To achieve this, deploy the WAR as `ROOT.war`:

```bash
# Stop Tomcat if running
$CATALINA_HOME/bin/shutdown.sh

# Remove existing ROOT app (if any)
rm -r $CATALINA_HOME/webapps/ROOT
rm $CATALINA_HOME/webapps/ROOT.war

# Copy the built WAR
cp target/my-blog-back-app.war $CATALINA_HOME/webapps/ROOT.war
```

## Start, Stop & Restart

```bash
# Start
$CATALINA_HOME/bin/startup.sh
# App is available at http://localhost:8080/

# Stop
$CATALINA_HOME/bin/shutdown.sh

# Restart (stop, then start)
$CATALINA_HOME/bin/shutdown.sh && $CATALINA_HOME/bin/startup.sh
```

> **Note:** The H2 database is in-memory — all data is lost when Tomcat stops.

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
java -cp ~/.m2/repository/com/h2database/h2/2.2.224/h2-2.2.224.jar org.h2.tools.Shell \
  -url "jdbc:h2:tcp://localhost:9092/mem:blogdb" \
  -user sa \
  -password ""
```