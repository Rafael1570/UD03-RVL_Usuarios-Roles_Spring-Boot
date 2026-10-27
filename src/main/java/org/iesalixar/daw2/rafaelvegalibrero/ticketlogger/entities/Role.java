package org.iesalixar.daw2.rafaelvegalibrero.ticketlogger.entities;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    private Long id;

    @NotBlank(message="{validation.role.name.required}")
    @Size(max=50, message="{validation.role.name.size}")
    private String name;
}
