# Redis OM on Netflix DGS

This is a small project to demonstrate how to use [Redis OM](https://github.com/redis/redis-om-spring) with the Netflix
[DGS Framework](https://netflix.github.io/dgs/) to stitch together
a [Redis JSON](https://github.com/RedisJSON/RedisJSON) backend
with a [GraphQL](https://graphql.org/) front end. It uses a data set of electric vehicle information from the Washington
State Department of Licensing (DOL):
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

## Index creation and Example Queries

Using RedisInsight's profiler (or `redis-cli monitor`), we can see the index that is created for the `Vehicle` type with
[FT.CREATE](https://redis.io/docs/latest/commands/ft.create/).

```redis
"FT.CREATE" "com.example.redisomdgs.codegen.types.VehicleIdx" "ON" "JSON" "PREFIX" "1" "com.example.redisomdgs.codegen.types.Vehicle:" "LANGUAGE" "english" "SCORE" "1.0" "SCHEMA" "$.vin" "AS" "vin" "TAG" "SEPARATOR" "|" "$.county" "AS" "county" "TAG" "SEPARATOR" "|" "$.city" "AS" "city" "TAG" "SEPARATOR" "|" "$.state" "AS" "state" "TAG" "SEPARATOR" "|" "$.postalCode" "AS" "postalCode" "TAG" "SEPARATOR" "|" "$.modelYear" "AS" "modelYear" "NUMERIC" "$.make" "AS" "make" "TAG" "SEPARATOR" "|" "$.model" "AS" "model" "TAG" "SEPARATOR" "|" "$.electricVehicleType" "AS" "electricVehicleType" "TAG" "SEPARATOR" "|" "$.cleanAlternativeFuelVehicleEligibility" "AS" "cleanAlternativeFuelVehicleEligibility" "TAG" "SEPARATOR" "|" "$.electricRange" "AS" "electricRange" "NUMERIC" "$.baseMsrp" "AS" "baseMsrp" "NUMERIC" "$.legislativeDistrict" "AS" "legislativeDistrict" "TAG" "SEPARATOR" "|" "$.dolVehicleId" "AS" "dolVehicleId" "NUMERIC" "$.vehicleLocation" "AS" "vehicleLocation" "GEO" "$.electricUtility" "AS" "electricUtility" "TAG" "SEPARATOR" "|" "$.censusTract" "AS" "censusTract" "TAG" "SEPARATOR" "|" "$.id" "AS" "id" "TAG" "SEPARATOR" "|"
```

We also get a very clear mapping between GraphQL and calls
to [FT.SEARCH](https://redis.io/docs/latest/commands/ft.search/).

### Get everything

With no filter, we can see all the vehicles in the database. The resulting query just asks for `"*"`.

```graphql
{
    vehicles {
        model
        modelYear
    }
}
```

This results in the following Redis query:

```redis
"FT.SEARCH" "com.example.redisomdgs.codegen.types.VehicleIdx" "*" "LIMIT" "0" "10000" "DIALECT" "1"
```

### Find all the Chevy Bolts

```graphql

{
    vehicles(filter: {make: {startsWith: "chev"} model: {startsWith: "bo"}} ) {
        modelYear
        make
        model
        vin
    }
}
```

And the resulting Redis query:

```redis
"FT.SEARCH" "com.example.redisomdgs.codegen.types.VehicleIdx" "(( @make:{chev*}) @model:{bo*})" "LIMIT" "0" "10000" "DIALECT" "1"
```

### Use geo search

```graphql

{
    vehicles(
        filter: {
            make: {startsWith: "nissan"},
            model: {startsWith: "ar"},
            near: {x: -122.3016563, y: 47.5858977, distance: 5}}
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

And, finally, the Redis query:

```redis
"FT.SEARCH" "com.example.redisomdgs.codegen.types.VehicleIdx" "((( @vehicleLocation:[-122.3016563 47.5858977 5.0 m]) @make:{nissan*}) @model:{ar*})" "LIMIT" "0" "10000" "DIALECT" "1"
```

## Implementation notes

### Redis OM

The demo uses `RedisDocumentRepository` to save data only, and utilizes `EntityStream` for the search capabilities.
A third option might be a Spring `@Service`. `EntityStream` is a good choice when you want to build a query based on
input, and adding features like sorting and paging is straightforward.

```java
// ...
SearchStream<Vehicle> stream = entityStream.of(Vehicle.class)
        .filter(filter)
        .sorted(Vehicle$.MODEL_YEAR, SortOrder.DESC)
        .collect(Collectors.toList());
// ... 
```

A good resource for `EntityStream` examples is
the [EntityStreamDocsTest.java](https://github.com/redis/redis-om-spring/blob/main/redis-om-spring/src/test/java/com/redis/om/spring/search/stream/EntityStreamDocsTest.java)
file.

### DGS

The DGS code generation is advantageous in simple situations because we keep our domain and response types in sync with
less work, but it also means it's tricky to add custom logic. For example, in `RedisOmDgsApplication`, we just resort to
static methods for the `fromCSV` call. Similarly, when we create `input` types for filters, implementing methods on them
is less ergonomic than if we had just implemented them manually. Fortunately you can mix and match the paradigms, or
have the code generation do the work first and copy the results to your project and disable the generation going
forward.

Performance may be another consideration. Redis is fast, but you may be able to take advantage of
in memory caching in your application as well with a combination
of [Data Loaders](https://netflix.github.io/dgs/data-loaders) to fetch and cache data in the application
and only use `EntityStream` for the search capabilities.

This demo only uses a small subset of DGS features. See the official [documentation](https://netflix.github.io/dgs/)
for more.
