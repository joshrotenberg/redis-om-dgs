# Redis OM on Netflix DGS

This is a small project to demonstrate how to use [Redis OM](https://github.com/redis/redis-om-spring) with the Netflix
[DGS Framework](https://netflix.github.io/dgs/) to stitch a [Redis JSON](https://github.com/RedisJSON/RedisJSON) backend
with a [GraphQL](https://graphql.org/) front end. It uses a data set of electric vehicles from
<https://catalog.data.gov/dataset/electric-vehicle-population-data>.

## Running the project

```shell
# Start redis 
docker-compose up -d

# Run the application
./gradlew bootRun
```

Navigate to <http://localhost:8001/> to see the RedisInsight dashboard, and <http://localhost:8080/graphiql> to interact
with the GraphQL API. See the example queries below to get started.

## Interesting parts

- [schema.graphqls](src/main/resources/schema/schema.graphqls) - The GraphQL schema that defines the API. Using this
  schema, DGS generates the necessary classes to interact with the API. All of the necessary annotations for RedisOM
  can be added in the GraphQL schema which lets the models be entirely generated.

- [VehicleRepository](src/main/java/com/example/redisomdgs/repositories/VehicleRepository.java) - The repository that
  interacts with Redis OM. This only gets used to store the data from the parsed CSV file; we use Redis OM to interact
  with the data.

- [VehicleDataFetcher](src/main/java/com/example/redisomdgs/datafetchers/VehicleDataFetcher.java) - The data fetcher
  that retrieves data from Redis OM. This uses the `EntityStream` class to build the query and return the results.

- [VehicleFilter](src/main/java/com/example/redisomdgs/filters/VehicleFilter.java) - The filter class that is used to
  filter vehicles based on the query. This is used in the `VehicleDataFetcher` to filter the results.

## Index

```shell
"FT.CREATE" "com.example.redisomdgs.codegen.types.VehicleIdx" "ON" "JSON" "PREFIX" "1" "com.example.redisomdgs.codegen.types.Vehicle:" "LANGUAGE" "english" "SCORE" "1.0" "SCHEMA" "$.vin" "AS" "vin" "TAG" "SEPARATOR" "|" "$.county" "AS" "county" "TAG" "SEPARATOR" "|" "$.city" "AS" "city" "TAG" "SEPARATOR" "|" "$.state" "AS" "state" "TAG" "SEPARATOR" "|" "$.postalCode" "AS" "postalCode" "TAG" "SEPARATOR" "|" "$.modelYear" "AS" "modelYear" "NUMERIC" "$.make" "AS" "make" "TAG" "SEPARATOR" "|" "$.model" "AS" "model" "TAG" "SEPARATOR" "|" "$.electricVehicleType" "AS" "electricVehicleType" "TAG" "SEPARATOR" "|" "$.cleanAlternativeFuelVehicleEligibility" "AS" "cleanAlternativeFuelVehicleEligibility" "TAG" "SEPARATOR" "|" "$.electricRange" "AS" "electricRange" "NUMERIC" "$.baseMsrp" "AS" "baseMsrp" "NUMERIC" "$.legislativeDistrict" "AS" "legislativeDistrict" "TAG" "SEPARATOR" "|" "$.dolVehicleId" "AS" "dolVehicleId" "NUMERIC" "$.vehicleLocation" "AS" "vehicleLocation" "GEO" "$.electricUtility" "AS" "electricUtility" "TAG" "SEPARATOR" "|" "$.censusTract" "AS" "censusTract" "TAG" "SEPARATOR" "|" "$.id" "AS" "id" "TAG" "SEPARATOR" "|"
```

## Example Queries

```shell
# see every vehicle model and model year
# "FT.SEARCH" "com.example.redisomdgs.codegen.types.VehicleIdx" "*" "LIMIT" "0" "10000" "DIALECT" "1"
{
  vehicles {
    model
    modelYear
  }
}

# find all the chevy bolts
# "FT.SEARCH" "com.example.redisomdgs.codegen.types.VehicleIdx" "(( @make:{chev*}) @model:{bo*})" "LIMIT" "0" "10000" "DIALECT" "1"
{
  vehicles(filter: {make: {startsWith: "chev"} model: {startsWith: "bo"}} ) {
    modelYear
    make
    model
    vin
  }
}

# use geo search to find the nearest nissan ariyas 
# "FT.SEARCH" "com.example.redisomdgs.codegen.types.VehicleIdx" "((( @vehicleLocation:[-122.3016563 47.5858977 5.0 m]) @make:{nissan*}) @model:{ar*})" "LIMIT" "0" "10000" "DIALECT" "1"
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