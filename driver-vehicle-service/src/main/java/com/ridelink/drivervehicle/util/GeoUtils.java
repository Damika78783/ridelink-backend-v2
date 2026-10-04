package com.ridelink.drivervehicle.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for simulated geo-distance computations using the Haversine formula.
 */
public final class GeoUtils {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private GeoUtils() {
    }

    /**
     * Computes the distance in kilometers between two latitude/longitude pairs.
     *
     * @param lat1 Latitude of point 1
     * @param lon1 Longitude of point 1
     * @param lat2 Latitude of point 2
     * @param lon2 Longitude of point 2
     * @return Distance in kilometers rounded to two decimal places
     */
    public static double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2.0) * Math.sin(dLat / 2.0)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2.0) * Math.sin(dLon / 2.0);

        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        double distance = EARTH_RADIUS_KM * c;

        return BigDecimal.valueOf(distance)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
