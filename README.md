# Design doc

## Guiding principles

This is expected to be a short exercise:

 - KISS
 - If requirements are unclear do something simple & sensible
 - Leverage the Spring stack as much as possible, especially starters, to save both time and LOCs
 - Ignore most real life requirements
 
 ## Persistence layer
 
 Persistence layer uses JPA. 
 
 A `Book` entity is defined which has a db generated identifier and two non null fields: `author` and `title`. That's it. 
 
 `BookRepository`, a `CrudRepository`, allows to perform CRUD operations on books. 
 
 Persistence layer is tested using `DataJpaTest` as it makes no sense to mock the database. 
 
 ## CRUD layer
 
 As the goal is to track DB operations, we must give a way to perform such operations. Spring Data Rest allows to automatically expose a `CrudRepository` as a REST API. It has many shortcomings for real APIs general but is really helpful here.
 
 ```
# Using https://httpie.org/
 
# List all books
http GET localhost:8080/books
# Create a new book
http POST localhost:8080/books author="Daniel Kahneman" title="Thinking fast and slow"
# Update a book
http PATCH localhost:8080/books/1 title="Thinking, fast and slow"
# Get a book
http GET localhost:8080/books/1
```

## CRUD / DB Auditing

Statement of the problem is: _app should notify [...] when an operation in the database is performed (CRUD)_

This statement can be interpreted a number of different ways:

 1. Auditing is performed at service level 
 1. Auditing is performed at repository level
 1. Auditing is performed at ORM level
 1. Auditing is performed at database level.

Our app is pure CRUD and don't have any service, this rule out the first option.

Second option is viable and easy. A clean way to do that is to use AOP to automatically audit `save` and `delete` method. Auditing could be enabled by adding an annotation to the repository. Because I have already done something like that past, GDPR compliance sometimes requires to keep an audit log, I decided try something else. It was the opportunity to learn something new.

Spring JPA uses Hibernate. Hibernates allows to add listeners and interceptors so it should be possible to audit changes at this level. That's what I tried to do. My implementation relies on `PostActionEventListener`. This is the first time I use it and I am not confident it is reliable (interactions with TX etc.). Straightforward tests passes but writing a real test suite would be too costly for this exercise. Let's say it's good enough.

Finally, most real databases maintain a (WAL|OpLog). This log could be used for auditing purposes. Using https://debezium.io/ could be fun. Luckily there is no connector for our H2 embedded database, no rabbit hole to follow.
 
 This layer is tested through an integration test which start the whole app. It would be better to only initialize the DB layer, but did not had time to figure out how to do that.
 
## Front end 

Clients open a WebSocket using SockJS and then use STOMP. As soon as a client is connected, it subscribes to `/topic/dbevents`. Each time a message is received it is displayed at the end of an `ul`. 

On the server side `WebSocketAuditor` publishes all `AuditedOp` to `/topoic/dbevents`.

This is tested by a basic end-to-end test. Testing only the websocket layer would be better, but I'm running out of time.

Note: This is the first time I use WebSockets.
 
## /healthz endpoint

Implemented as a check alive endpoint, always returning 200.
 
# Usage

## Build

```
./mvmw package
```

## Local run

Starts the server on `localhost:8080` using an embedded in-memory H2 and an in-memory broker. Everything is transient an vanish when the server stop.

```
./mvm spring-boot:run
```

## Docker run

I did not have time to build a docker image, then use docker compose to start a server instance and an ActiveMQ instance. 

# Notes

Open questions:
- Does it work with a real broker?
- Does it work behind a load balancer? My understanding is that clients directly subscribe to the broker so it should work
- Was using Hibernate listener a good choice ? Probably not :)

Flaws & limitations:
- Too many coarse grained integration tests. Good design and thus require time.
