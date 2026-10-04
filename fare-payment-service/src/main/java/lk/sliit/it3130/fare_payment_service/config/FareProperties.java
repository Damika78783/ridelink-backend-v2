package lk.sliit.it3130.fare_payment_service.config;

import lk.sliit.it3130.fare_payment_service.model.VehicleType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "fare")
public class FareProperties {

    private BigDecimal baseFare = BigDecimal.valueOf(150.00);
    private BigDecimal perKmRate = BigDecimal.valueOf(80.00);
    private BigDecimal perMinRate = BigDecimal.valueOf(5.00);
    private BigDecimal minimumFare = BigDecimal.valueOf(200.00);
    private String currency = "LKR";

    private Map<String, Double> multipliers = new HashMap<>();

    public double getMultiplier(VehicleType vehicleType) {
        if (vehicleType == null) {
            return 1.0;
        }
        String key = vehicleType.name().toLowerCase();
        return multipliers.getOrDefault(key, 1.0);
    }
}
