# available-stock-processor project

This project uses Quarkus, the Supersonic Subatomic Java Framework, and KafkaStreams.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ . Or KafkaStreams: https://kafka.apache.org/documentation/streams/ .

## Application

This app runs a KafkaStreams topology which consumes orders and shipments (from topics of the same name) and produces a stream of stock reservations per product SKU.

### Logic
* The "warehouse", represented by the stock-levels topic, is the master of stock and periodically updates absolute stock levels per SKU.
* New customer orders and shipments create and release reservations on existing stock for each product SKU and quantity ordered.

### Assumptions
* Updates to stock-levels per SKU always supersede previous updates. (Lots of things can happen in the warehouse to change stock-levels.)
* reserved-stock per SKU, resulting from orders and shipments, will be used to modify in real time the latest stock-levels. 
 
(The reserved-stock-processor project: https://github.com/merlante/reserved-stock-processor does this last bit, producing a real time stream of stock reservations.)

## Quickstart

### Requirements

* A kafka cluster configured to use OAUTHBEARER authentication.
* A service account with OAUTHBEARER credentials and an oauth token endpoint.

The following topics are required in your kafka cluster for this app to run:
* stock-levels
* reserved-stock

### Run

To run the app, add the following vars to your environment:

```shell script
export BOOTSTRAP_SERVERS=<KAFKA_BOOTSTRAP_SERVERS>
export CLIENT_ID=<KAFKA_CLIENT_ID>
export CLIENT_SECRET=<KAFKA_CLIENT_SECRET>
export TOKEN_ENDPOINT_URI=<OAUTH_TOKEN_ENDPOINT_URI>
```

Then run one of the ./mvnw commands, below, e.g.
```shell script
./mvnw compile quarkus:dev
```

The app will connect to your kafka cluster and consume and produce records from topics, according to its KafkaStreams topology, until it is exited.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, and/or you want to build for a different native target (e.g. building for linux when running on a mac), you can run the native executable build in a container using: 
```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```
Use this command (or the next variation) if you want to build an executable suitable for running in a linux-based docker image (see below).

If you want to specify that docker is the container runtime, rather than podman, the default, do:
```shell script
./mvnw package -Pnative -Dnative-image.container-runtime=docker -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/available-stock-processor-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.html.

## Running in docker

### Build

Jvm image build (if you ran ./mvnw package [...] without -Pnative):
```shell script
docker build -f src/main/docker/Dockerfile.jvm -t [repo_name]available-stock-processor .
```

Native image build (if you ran ./mvnw package -Pnative [...]):
```shell script
docker build -f src/main/docker/Dockerfile.native -t [repo_name]available-stock-processor .
```

[repo_name] could be something like "quay.io/myaccount/", with quay.io being a pushable remote repository at quay.io, or it can be blank, with only a local name of, say, "available-stock-processor", needed.

To push to a remote:
```shell script
docker push [repo_name]available-stock-processor .
```

### Run

```shell script
docker run --rm [repo_name]available-stock-processor .
```

## Related guides

- Apache Kafka Streams ([guide](https://quarkus.io/guides/kafka-streams)): Implement stream processing applications based on Apache Kafka
