package com.ridelink.drivervehicle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@SpringBootApplication
public class DriverVehicleServiceApplication {

    public static void main(String[] args) {
        loadDotenv();
        SpringApplication.run(DriverVehicleServiceApplication.class, args);
    }

    private static void loadDotenv() {
        File[] candidateFiles = new File[] {
                new File(".env"),
                new File("../.env"),
                new File("driver-vehicle-service/.env")
        };

        for (File file : candidateFiles) {
            if (file.exists() && file.isFile()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#")) {
                            continue;
                        }
                        int eqIdx = line.indexOf('=');
                        if (eqIdx > 0) {
                            String key = line.substring(0, eqIdx).trim();
                            String val = line.substring(eqIdx + 1).trim();
                            if (System.getProperty(key) == null && System.getenv(key) == null) {
                                System.setProperty(key, val);
                            }
                        }
                    }
                } catch (IOException ignored) {
                }
                break;
            }
        }
    }
}

