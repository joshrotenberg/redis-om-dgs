package com.example.redisomdgs.datafetchers;

import com.example.redisomdgs.codegen.types.Vehicle;
import com.netflix.graphql.dgs.DgsQueryExecutor;
import com.netflix.graphql.dgs.client.GraphQLResponse;
import com.netflix.graphql.dgs.client.MonoGraphQLClient;
import com.redis.testcontainers.RedisStackContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class VehicleDataFetcherTest {

    @Container
    private static RedisStackContainer container = new RedisStackContainer(
            RedisStackContainer.DEFAULT_IMAGE_NAME.withTag(RedisStackContainer.DEFAULT_TAG));

    @Autowired
    DgsQueryExecutor dgsQueryExecutor;

    final MonoGraphQLClient monoGraphQLClient;

    VehicleDataFetcherTest(@LocalServerPort Integer port) {
        WebClient webClient = WebClient.create("http://localhost:" + port.toString() + "/graphql");
        this.monoGraphQLClient = MonoGraphQLClient.createWithWebClient(webClient);
    }

    @Test
    void vehicles() {
        String vehicleQuery = "{ vehicles { vin make model } }";

        List<Vehicle> result = dgsQueryExecutor.executeAndExtractJsonPathAsObject(vehicleQuery, "data.vehicles[*]", List.class);
        assertThat(result.size()).isNotZero();
    }

    @Test
    void vehiclesWeb() {
        String vehicleQuery = "{ vehicles { vin make model } }";

        GraphQLResponse response =
                monoGraphQLClient.reactiveExecuteQuery(vehicleQuery).block();

        List<Vehicle> result = response.extractValueAsObject("data.vehicles[*]", List.class);
        assertThat(result.size()).isNotZero();
    }
}
