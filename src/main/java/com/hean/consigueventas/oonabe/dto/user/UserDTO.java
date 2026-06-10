package com.hean.consigueventas.oonabe.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private Boolean active;
    private LocalDateTime disabledAt;
    private LocalDateTime createdAt;
    private Set<String> roles; // Solo nombres de los roles
}
