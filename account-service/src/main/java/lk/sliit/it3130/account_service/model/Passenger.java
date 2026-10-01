package lk.sliit.it3130.account_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "passengers")
public class Passenger extends Account {
    // Common fields inherited from Account.
    // Passenger-specific fields can be added later if needed.
}