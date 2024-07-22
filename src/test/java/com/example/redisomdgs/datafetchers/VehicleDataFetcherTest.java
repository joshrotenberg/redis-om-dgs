package com.example.redisomdgs.datafetchers;

import com.netflix.graphql.dgs.DgsQueryExecutor;
import com.netflix.graphql.dgs.client.GraphQLResponse;
import com.netflix.graphql.dgs.client.MonoGraphQLClient;
import com.redis.testcontainers.RedisStackContainer;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
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

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", container::getRedisHost);
        registry.add("spring.data.redis.port", container::getRedisPort);
    }

    VehicleDataFetcherTest(@LocalServerPort Integer port) {
        WebClient webClient = WebClient.create("http://localhost:" + port.toString() + "/graphql");
        this.monoGraphQLClient = MonoGraphQLClient.createWithWebClient(webClient);
    }

    @Test
    void vehicles() {
        @Language("GraphQL") String vehicleQuery = "{ vehicles { vin make model } }";

        List result = dgsQueryExecutor.executeAndExtractJsonPathAsObject(vehicleQuery, "data.vehicles[*]", List.class);
        assertThat(result.size()).isNotZero();
    }

    @Test
    void vehiclesWeb() {
        @Language("graphql") String vehicleQuery = "{ vehicles { vin make model } }";

        GraphQLResponse response =
                monoGraphQLClient.reactiveExecuteQuery(vehicleQuery).block();

        assert response != null;
        List result = response.extractValueAsObject("data.vehicles[*]", List.class);
        assertThat(result.size()).isNotZero();
    }
}
