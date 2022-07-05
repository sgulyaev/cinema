# Cinema

---
## Inspired by kotlin-jooby-svelte-template from codeborne

## Start 
    git clone https://github.com/sgulyaev/cinema.git --config core.autocrlf=input
 

## Running in Docker
`docker-compose up --build`

This will bind
- DB on 127.0.0.1:65432
- App on 127.0.0.1:8888

or to just start the DB:
`docker-compose up -d db`

## Development

To run tests:

* `docker-compose up -d db` - if need to run db tests
* `./gradlew test` 

All tests can run from IDE

Can start Application locally
* `./gradlew jar`
* `docker-compose up -d db` 
* from ./build/libs/ `java -jar app.jar` 

To test api use runnable http samples located in `rest-api.http` file