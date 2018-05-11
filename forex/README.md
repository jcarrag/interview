### Description of changes
To achieve the 10,000 minimum request per day given that the 1forge api limit is 1,000 per day, caching of prices was introduced. Upon request the cache is first checked and if the requested currency is not found the server will call 1forge api to get __all__ supported currency then store the prices in cache. TTL of cache is configured via `akka.http.caching.lfu-cache.time-to-live`

To ensure that no other outgoing request to 1forge will happen while the server is waiting for a response, prior to calling 1forgeAPI the cache is pre-created with currency pair and *Promise[Price]*. So when a request comes the server will respond with the *Promise[Price]* and the *Promise[Price]* will all be completed when the 1forge have responded in a timely manner.

### SBT changes
Bumped akka dependencies to support `akka-http-caching`
```
"com.typesafe.akka"        %% "akka-actor"           % "2.4.20",
"com.typesafe.akka"        %% "akka-http"            % "10.0.11",
"com.typesafe.akka"        %% "akka-http-caching"    % "10.0.11",
"org.scalatest"            %% "scalatest"            % "3.0.4"        % Test,
"com.typesafe.akka"        %% "akka-http-testkit"    % "10.0.11"      % Test,
```

### Run
`sbt run`

This will bind to localhost:8888, can be changed in reference.conf

```
app.api.{
  interface = ""
  port = ""
}
```

### Test
`sbt test`
* there is a problem with the test running of out memory when tests are run in succession, and it only contains happy path test cases
