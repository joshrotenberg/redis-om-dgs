package com.example.redisomdgs.repositories;

import com.example.redisomdgs.codegen.types.Vehicle;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface VehicleRepository extends RedisDocumentRepository<Vehicle, String> {
}
