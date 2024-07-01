package com.example.redisomdgs;

import com.example.redisomdgs.codegen.types.Vehicle;
import com.example.redisomdgs.repositories.VehicleRepository;
import com.google.common.collect.Streams;
import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.geo.Point;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.List;
import java.util.logging.Logger;

@SpringBootApplication
@EnableRedisDocumentRepositories(basePackages = "com.example.redisomdgs.*")
public class RedisOmDgsApplication {
    private static final Logger log = Logger.getLogger(RedisOmDgsApplication.class.getName());

    private final VehicleRepository vehicleRepository;

    public RedisOmDgsApplication(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(RedisOmDgsApplication.class, args);
    }

    private static Point fromCsvPoint(String csvPoint) {
        String point = csvPoint.replace("POINT (", "").replace(")", "").trim();
        String[] coordinates = point.split(" ");

        if (coordinates[0].isBlank() || coordinates[1].isBlank()) {
            log.warning("Invalid point for row");
            return new Point(0.0, 0.0);
        }
        return new Point(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));
    }

    private static Vehicle fromCsv(CSVRecord record) {
        Vehicle vehicle = new Vehicle();

        vehicle.setVin(record.get(0));
        vehicle.setCounty(record.get(1));
        vehicle.setCity(record.get(2));
        vehicle.setState(record.get(3));
        vehicle.setPostalCode(record.get(4));
        vehicle.setModelYear(Integer.parseInt(record.get(5)));
        vehicle.setMake(record.get(6));
        vehicle.setModel(record.get(7));
        vehicle.setElectricVehicleType(record.get(8));
        vehicle.setCleanAlternativeFuelVehicleEligibility(record.get(9));
        vehicle.setElectricRange(Integer.parseInt(record.get(10)));
        vehicle.setBaseMsrp(Integer.parseInt(record.get(11)));
        vehicle.setLegislativeDistrict(record.get(12));
        vehicle.setDolVehicleId(Integer.parseInt(record.get(13)));
        vehicle.setVehicleLocation(fromCsvPoint(record.get(14)));
        vehicle.setElectricUtility(record.get(15));
        vehicle.setCensusTract(record.get(16));

        return vehicle;
    }

    @Bean
    CommandLineRunner loadData(@Value("classpath:/data/Electric_Vehicle_Population_Data.csv") File vehiclesCsv) {
        return args -> {

            vehicleRepository.deleteAll();
            log.info("Parsing " + vehiclesCsv);
            Reader in = new FileReader(vehiclesCsv);
            Iterable<CSVRecord> records = CSVFormat.RFC4180.builder().setHeader().setSkipHeaderRecord(true)
                    .build()
                    .parse(in);
            List<Vehicle> vehicles = Streams.stream(records)
                    .map(RedisOmDgsApplication::fromCsv)
                    .toList();

            log.info("Saving " + vehicles.size() + " vehicles");
            vehicleRepository.saveAll(vehicles);
        };
    }
}