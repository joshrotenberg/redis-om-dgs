package com.example.redisomdgs.mutations;

import com.example.redisomdgs.RedisOmDgsApplication;
import com.example.redisomdgs.codegen.types.Vehicle;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import graphql.schema.DataFetchingEnvironment;

import java.util.logging.Logger;

@DgsComponent
public class VehicleMutation {
    private static final Logger log = Logger.getLogger(RedisOmDgsApplication.class.getName());

    @DgsData(parentType = "Mutation", field = "updateVehicleLocation")
    public Vehicle updateVehicleLocation(DataFetchingEnvironment dfe) {
        Vehicle vehicle = dfe.getSource();
        log.info(dfe.toString());
//        assert vehicle != null;
//        vehicle.setCity((String) dfe.getArgument("city"));
        return vehicle;
    }

}
