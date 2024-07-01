package com.example.redisomdgs.repositories;

import com.redis.om.spring.repository.RedisDocumentRepository;
import com.example.redisomdgs.codegen.types.Vehicle;

public interface VehicleRepository extends RedisDocumentRepository<Vehicle, String> {
}
