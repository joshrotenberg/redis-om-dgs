# Redis OM on Netflix DGS

This is a small project to demonstrate how to use [Redis OM](https://github.com/redis/redis-om-spring) with the Netflix
[DGS Framework](https://netflix.github.io/dgs/) to stitch a [Redis JSON](https://github.com/RedisJSON/RedisJSON) backend
with a [GraphQL](https://graphql.org/) front end.

## Running the project

```shell
# Start redis 
docker-compose up 
# Run the project
./gradlew bootRun
```

Navigate to [http://localhost:8080/graphiql](http://localhost:8080/graphiql) to interact with the GraphQL API. See the
example queries below to get started. You can also interact with the Redis OM repository directly by using the Redis Ins

## Interesting parts

- [schema.graphqls](src/main/resources/schema.graphqls) - The GraphQL schema that defines the API. Using this schema,
  DGS generates the necessary classes to interact with the API. All of the annotations needed for Redis OM piggyback
  here
  so we don't need to write any additional code.

- [VehicleRepository](src/main/java/com/example/redisomdemo/VehicleRepository.java) - The repository that interacts with
  Redis OM. This only gets used to store the data from the parsed CSV file; we use Redis OM to interact with the data.

- [VehicleDataFetcher](src/main/java/com/example/redisomdemo/VehicleDataFetcher.java) - The data fetcher that retrieves
  data from Redis OM. This uses the `EntityStream` class to build the query and return the results.

## Example Queries

```shell
# see every vehicle model and model year
{
  vehicles {
    model
    modelYear
  }
}

# find all the chevy bolts
{
  vehicles(filter: {make: {startsWith: "chev"} model: {startsWith: "bo"}} ) {
    modelYear
    make
    model
    vin
  }
}

# use geo search to find the nearest nissan ariyas 
{
  vehicles(
    filter: {make: {startsWith: "nissan"}, model: {startsWith: "ar"}, near: {x: -122.3016563, y: 47.5858977, distance: 5}}
  ) {
    make
    model
    city
    vin
    vehicleLocation {
      x
      y
    }
  }
}
```