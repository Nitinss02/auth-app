package com.auth.auth_app.dtos;

import com.auth.auth_app.models.Provider;
import com.auth.auth_app.models.Role;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private UUID id;
    private String email;
    private String name;
    private String password;
    private String image;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();
    private boolean enable = true;
    private Provider provider = Provider.LOCAL;
    private Set<Role> roles= new HashSet<>();
}
