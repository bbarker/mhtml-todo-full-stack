# full-stack-scala

TodoMVC with Akka-http, Scala.js, Autowire and [Monadic-HTML](https://github.com/OlivierBlanvillain/monadic-html).

This project is a fork of [full-stack-scala](https://github.com/OlivierBlanvillain/full-stack-scala), which in turn is essentially a fork of [Scalajs-React TodoMVC Example](https://github.com/tastejs/todomvc/tree/gh-pages/examples/scalajs-react) enhanced with a back-end and the proper sbt configuration for development/deployment.

- `model`: Shared Todo model
- `web-client`: Scala.js/mhtml client
- `web-server`: Akka-http server
- `web-static`: Static files

#### Development:
    
```
sbt ~web-server/re-start
```

#### Deployment:

```
sbt web-server/assembly
java -jar target/scala-2.11/web-server-assembly-1.0-SNAPSHOT.jar
```
