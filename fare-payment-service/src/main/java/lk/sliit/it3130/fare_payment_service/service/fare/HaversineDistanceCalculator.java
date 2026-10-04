package lk.sliit.it3130.fare_payment_service.service.fare;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class HaversineDistanceCalculator {

    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Calculates the great-circle distance between two geographic coordinates using the Haversine formula.
     *
     * @param startLat Latitude of starting point in degrees
     * @param startLon Longitude of starting point in degrees
     * @param endLat   Latitude of ending point in degrees
     * @param endLon   Longitude of ending point in degrees
     * @return Distance in kilometers rounded to 2 decimal places
     */
    public double calculateDistanceKm(double startLat, double startLon, double endLat, double endLon) {
        if (startLat == endLat && startLon == endLon) {
            return 0.0;
        }

        double latDistance = Math.toRadians(endLat - startLat);
        double lonDistance = Math.toRadians(endLon - startLon);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(startLat)) * Math.cos(Math.toRadians(endLat))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double rawDistance = EARTH_RADIUS_KM * c;

        return BigDecimal.valueOf(rawDistance)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
