[![Generic badge](https://img.shields.io/badge/vert.x-4.0.3-purple.svg)](https://vertx.io)

About
======

This is a simple server application which implements a CR(UD) service with MongoDB as a backend and reactive API, built on the basis of Vert.x framework

Demonstrates the using of the following technologies:

* Reactive authorization and JWT

* Exposing public end points to access the data saved in the database


Public api contract:

| Method | Endpoint                   |Secure|      Description                                  |
|--------|----------------------------|------|---------------------------------------------------|
|POST    |  /register                 |  No  |    register new user                              |
|GET     |  /api/v1/token             |  Yes |    get authorization token                        |
|GET     |  /api/v1/:username         |  Yes |    get users profile                              |


Building
=========


To launch your tests:

```
mvn clean test
```

To package your application:

```
mvn clean package
```

To run your application:

```
java -cp ./target/vx-1.0.0-SNAPSHOT-fat.jar verticle.api.ProfileVerticle 
```

Run all containers, register a user, then get a token and perform GET request for users profile

```
POST /register HTTP/1.1
> Host: localhost:4000
> Content-Type: application/json
> Accept: */*

{
	"username": "Alisa",
	"password": "123",
	"city": "Amsterdam",
	"email": "alice@test.com",
	"deviceId": "2"
}

* upload completely sent off: 112 out of 112 bytes
* Mark bundle as not supporting multiuse

< HTTP/1.1 200 OK
< vary: origin
< content-length: 0

```

Getting token:

```
POST /api/v1/token HTTP/1.1
> Host: localhost:4000
> Content-Type: application/json
> Accept: */*

{
	"username": "Alisa",
	"password":"123"
}

```

Performing GET request:

```
 GET /api/v1/Alisa HTTP/1.1
> Host: localhost:4000
> Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSU.........ykA
> Accept: */*
```


Reference
==========


* [Vert.x Documentation](https://vertx.io/docs/)
* [Vert.x Stack Overflow](https://stackoverflow.com/questions/tagged/vert.x?sort=newest&pageSize=15)
* [Vert.x Initializr](http://start.vertx.io)


