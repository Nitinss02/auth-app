package com.auth.auth_app.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.util.UUID;
@Data
@Entity
public class Role {
    @Id
    private UUID id = UUID.randomUUID();
    private String roleName;
}
