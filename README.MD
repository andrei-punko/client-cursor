# Client Cursor API via REST example

![Java CI with Maven](https://github.com/andrei-punko/client-cursor/workflows/Java%20CI%20with%20Maven/badge.svg)
[![Coverage](.github/badges/jacoco.svg)](https://github.com/andrei-punko/client-cursor/actions/workflows/maven.yml)

Based on [Template for Spring Boot application](https://github.com/andrei-punko/spring-boot-3-template)

Includes a web server on port `9080` with `/articles` endpoint.  
Supports CRUD and cursor-based list pagination (`prev` / `next`).

## Prerequisites

- Maven 3
- JDK 21

## How to build

```
mvn clean install
```

## How to build Docker image

```
docker build ./ -t client-cursor-app
```

## How to start

Use [run.bat](./run.bat) or:

```
java -jar target/client-cursor-0.0.1-SNAPSHOT.jar \
 --spring.datasource.url=jdbc:h2:mem:testdb \
 --spring.datasource.username=sa \
 --spring.datasource.password=password \
 --spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect
```

Or with Docker Compose:

```
docker-compose up
```

## Quick links

- API: http://localhost:9080/articles
- Swagger: http://localhost:9080/swagger-ui/index.html
- Presentation (Marp slides): [docs/README.md](docs/README.md)

## Useful CURL commands

### Create article

```
curl -i -H "Accept: application/json" -H "Content-Type: application/json" \
  -d '{ "title": "Some title", "text": "Some text", "author": "Jane Doe" }' \
  -X POST http://localhost:9080/articles
```

### Get article by id

```
curl -i http://localhost:9080/articles/1
```

### Update article

```
curl -i -H "Accept: application/json" -H "Content-Type: application/json" \
  -d '{ "title": "Another title" }' \
  -X PATCH http://localhost:9080/articles/2
```

### List articles by cursor

```
curl -i 'http://localhost:9080/articles?size=2'
```

Response shape: `{"data":[...], "prev":..., "next":...}`

Pass `next` / `prev` as `cursor` in the next request:

```
curl -i 'http://localhost:9080/articles?size=2&cursor=PUT_NEXT_OR_PREV_CURSOR_HERE'
```

Optional sorting on the **first** request only:

```
curl -i 'http://localhost:9080/articles?size=2&sort_by=title&sort_order=ASC'
```

- `sort_by`: `title` | `author` | `summary`
- `sort_order`: `ASC` | `DESC` (default `ASC`)
- After the first page, sort is taken from the cursor — do not pass `sort_by` together with `cursor`
- `prev` is `null` on the first page; `next` is `null` on the last page

### Delete article

```
curl -i -X DELETE http://localhost:9080/articles/1
```

## Functional tests

App must be running on port `9080`, then:

```
run-func-tests.bat
```
