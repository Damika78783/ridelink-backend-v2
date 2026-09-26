package lk.sliit.it3130.account_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "admins")
public class Admin extends Account {
    // No extra fields for now — inherits id, email, password, fullName, phoneNumber, status, createdAt
}