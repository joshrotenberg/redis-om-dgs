package com.example.redisomdgs.filters;

import com.example.redisomdgs.codegen.types.*;
import com.redis.om.spring.metamodel.indexed.GeoField;
import com.redis.om.spring.metamodel.indexed.NumericField;
import com.redis.om.spring.metamodel.indexed.TextTagField;
import com.redis.om.spring.search.stream.SearchStream;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;

public class VehicleFilter {

    private static void applyGeoFilter(SearchStream<Vehicle> stream, GeoField<Vehicle, Point> field, GeoFilter filter) {
        if (filter.getX() != null && filter.getY() != null && filter.getDistance() != null) {
            Point point = new Point(filter.getX(), filter.getY());
            Distance distance = new Distance(filter.getDistance());
            stream.filter(field.near(point, distance));
        }
    }

    private static void applyStringFilter(SearchStream<Vehicle> stream, TextTagField<Vehicle, String> field, StringFilter filter) {
        if (filter.getStartsWith() != null) {
            stream.filter(field.startsWith(filter.getStartsWith()));
        }

        if (filter.getEndsWith() != null) {
            stream.filter(field.endsWith(filter.getEndsWith()));
        }
    }

    private static void applyIntFilter(SearchStream<Vehicle> stream, NumericField<Vehicle, Integer> field, IntFilter filter) {
        if (filter.getEq() != null) {
            stream.filter(field.eq(filter.getEq()));
        }

        if (filter.getGt() != null) {
            stream.filter(field.gt(filter.getGt()));
        }

        if (filter.getLt() != null) {
            stream.filter(field.lt(filter.getLt()));
        }
    }

    public static void applyFilters(SearchStream<Vehicle> stream, Filter filter) {
        if (filter != null) {
            if (filter.getNear() != null) {
                applyGeoFilter(stream, Vehicle$.VEHICLE_LOCATION, filter.getNear());
            }

            if (filter.getModelYear() != null) {
                applyIntFilter(stream, Vehicle$.MODEL_YEAR, filter.getModelYear());
            }

            if (filter.getElectricRange() != null) {
                applyIntFilter(stream, Vehicle$.ELECTRIC_RANGE, filter.getElectricRange());
            }

            if (filter.getMake() != null) {
                applyStringFilter(stream, Vehicle$.MAKE, filter.getMake());
            }

            if (filter.getModel() != null) {
                applyStringFilter(stream, Vehicle$.MODEL, filter.getModel());
            }
        }
    }
}
