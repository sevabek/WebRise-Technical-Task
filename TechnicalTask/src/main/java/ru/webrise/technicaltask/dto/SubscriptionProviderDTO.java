package ru.webrise.technicaltask.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionProviderDTO {

    @Length(max = 50, message = "Service name must be under 50 characters")
    @NotBlank
    @Size(max = 50, message = "Service name cannot be more than 50 symbols")
    private String name;

    @NotNull
    private BigDecimal price;
}
