package org.iesalixar.daw2.rafaelvegalibrero.ticketlogger.forms;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class UserForm {
    public interface Create extends Default { }

    private Long id;
    @NotBlank(message="{validation.user.username.required}")
    @Size(max=40, message="{validation.user.username.size}")
    private String username;

    @ToString.Exclude
    @NotBlank(groups=Create.class, message="{validation.user.password.required}")
    @Size(max=72, message="{validation.user.password.size}")
    private String password = "";

    private boolean active = true;
    private boolean accountNonLocked = true;
    @NotNull(message="{validation.user.date.required}")
    @DateTimeFormat(pattern="yyyy-MM-dd'T'HH:mm")
    private LocalDateTime lastPasswordChange = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
    @NotNull(message="{validation.user.attempts.required}")
    @Min(value=0, message="{validation.user.attempts.min}")
    private Integer failedLoginAttempts = 0;
    private boolean emailVerified;
    private boolean mustChangePassword;
    @NotEmpty(message="{validation.user.roles.required}")
    private List<@NotNull @Positive Long> roleIds = new ArrayList<>();
}
