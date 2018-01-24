This was fun! I learnt some new concepts (like defining an Algebra), and challenged some of my preconceptions (Akka HTTP).

### Assumptions
- As the existing model lacked session management and an `User.isAdmin` bit, I believed a stub basic HTTP authentication implementation would be enough to demonstrate including authentication.
- In terms of features I chose to include, I prioritised:
   1. HTTP API.
   2. Client/Domain separation. I have found Domain Driven Design to be useful previously, and I believe it's still worth it, even for a project as small as this.
   3. Tests.
 
 ### Lessons
 - [Akka HTTP](https://doc.akka.io/docs/akka-http/current/) is great for when you're going full Akka-Streams/Actors etc. (due to its seamless integrations), but as a standalone HTTP library I was unimpressed. It felt like a lot of boilerplate to override decisions Akka HTTP had made itself, for example: custom (de-)serialisation, and authenticationd. I chose Akka HTTP because it's a library I had used before, but now I'm unlikely to use it outside of the Akka ecosystem. Instead I'd like to try [http4s](https://github.com/http4s/http4s), which seems to use cats from its foundation and has functional concepts like `IO` already considered.
 - [Circe](https://github.com/circe/circe) is more flexible than I had realised. It has built in support for [object enumerations and value classes](https://github.com/circe/circe/blob/master/modules/generic-extras/src/main/scala/io/circe/generic/extras/semiauto.scala#L43-L61).
 
 ### Continuing
 In terms of what I'd add next:
 1. A logger.
 2. Domain-level migrations. Something similar to [Stamina](https://github.com/scalapenos/stamina) which I found effective previously in an Akka app, which could be wired into the route level versioning.
 3. Authentication, with session management, hashing & salting, and a way to determine and create admins. Also, type-level privilege restrictions.
 4. Deployment strategy.

## Instructions
From inside `interview/users/` run:

### Run
```
sbt run
```

### Test
```
sbt test
```
