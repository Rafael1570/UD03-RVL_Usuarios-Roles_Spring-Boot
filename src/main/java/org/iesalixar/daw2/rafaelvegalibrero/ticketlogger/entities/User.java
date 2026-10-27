package org.iesalixar.daw2.rafaelvegalibrero.ticketlogger.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.ToString;

@Data
public class User {
    private Long id;
    private String username;
    @ToString.Exclude
    private String passwordHash;
    private Boolean active;
    private Boolean accountNonLocked;
    private LocalDateTime lastPasswordChange;
    private LocalDateTime passwordExpiresAt;
    private Integer failedLoginAttempts;
    private Boolean emailVerified;
    private Boolean mustChangePassword;
    private List<Role> roles = new ArrayList<>();
}
