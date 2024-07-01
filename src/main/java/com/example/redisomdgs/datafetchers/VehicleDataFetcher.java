package com.example.redisomdgs.datafetchers;

import com.example.redisomdgs.codegen.types.Filter;
import com.example.redisomdgs.codegen.types.Vehicle;
import com.example.redisomdgs.filters.VehicleFilter;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import com.redis.om.spring.search.stream.EntityStream;
import com.redis.om.spring.search.stream.SearchStream;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@DgsComponent
public class VehicleDataFetcher {

    @Autowired
    EntityStream entityStream;

    @DgsQuery
    public List<Vehicle> vehicles(@InputArgument Filter filter) {
        SearchStream<Vehicle> stream = entityStream.of(Vehicle.class);

        VehicleFilter.applyFilters(stream, filter);

        return stream.collect(Collectors.toList());
    }
}
