package lk.sliit.it3130.account_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "drivers")
public class Driver extends Account {

    @NotBlank
    @Column(nullable = false, unique = true, length = 20)
    private String licenseNumber;

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
}