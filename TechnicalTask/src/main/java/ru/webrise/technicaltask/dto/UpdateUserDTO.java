package ru.webrise.technicaltask.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserDTO {

    @NotBlank
    @Size(max = 50, message = "Username cannot be more than 50 symbols")
    private String username;

    @Email
    @NotNull
    @Size(max = 100, message = "Email cannot be more than 100 symbols")
    private String email;

    @Size(max = 100, message = "Full name cannot be more than 100 symbols")
    private String fullName;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
