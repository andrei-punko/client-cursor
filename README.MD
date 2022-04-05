
# Example of Client Cursor API

![Java CI with Maven](https://github.com/andrei-punko/client-cursor/workflows/Java%20CI%20with%20Maven/badge.svg)
[![Coverage](.github/badges/jacoco.svg)](https://github.com/andrei-punko/client-cursor/actions/workflows/maven.yml)
[![Branches](.github/badges/branches.svg)](https://github.com/andrei-punko/client-cursor/actions/workflows/maven.yml)

Based on [Template for Spring Boot application](https://github.com/andrei-punko/spring-boot-template)

Includes web-server on port 9080 with `/articles` endpoint exposed.  
Supports CRUD set of operations and R with pagination

## Prerequisites:
- JDK 8
- Maven 3

## How to build:
    mvn clean install

## How to build Docker image with application inside:
    docker build ./ -t client-cursor-app

## How to start:
- Use [run.bat](./run.bat) script or

- Use next command:  
java -jar target/client-cursor-0.0.1-SNAPSHOT.jar \
 --spring.datasource.url=jdbc:h2:mem:testdb \
 --spring.datasource.username=sa \
 --spring.datasource.password=password \
 --spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect

- Use Docker compose:  
 `docker-compose up`

## Link for quick check:  
http://localhost:9080/articles

## Swagger documentation:  
http://localhost:9080/swagger-ui.html

## Useful CURL commands:
- New article addition:  
`curl -i -H "Accept: application/json" -H "Content-Type: application/json" -d '{ "title": "Some tittle", "text": "Some text", "author": { "id": 1 } }' -X POST http://localhost:9080/articles`

- Get existing article:  
`curl -i http://localhost:9080/articles/1`

- Update existing article:  
`curl -i -H "Accept: application/json" -H "Content-Type: application/json" -d '{ "title": "Another tittle" }' -X PATCH http://localhost:9080/articles/2`

- Get list of articles using cursor:  
`curl -i 'http://localhost:9080/articles?size=2'`  
Next and prev cursors placed in response: `{"data":[...], "prev":..., "next":...}`  
Use cursor as a parameter in next requests:  
`curl -i 'http://localhost:9080/articles?size=2&cursor=PUT_NEXT_OR_PREV_CURSOR_HERE'`  
If needed - add `sort` parameter to query, for example `sort=title`.  
You need to pass it only in initial query, after that all info will be encoded into next/prev cursor and you need to pass cursor only (and `size` if needed).  
Order direction via `order` parameter is supported (`ASC`/`DESC`)

- Deletion of article:  
`curl -i -X DELETE http://localhost:9080/articles/1`
