package phenriqued.BudgetMaster.DTOs.Login;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterUserDTO(
        @NotNull
        @NotBlank
        String name,
        @Email
        @NotNull
        String email,
        @NotNull
        @NotBlank
        String password,
        @NotNull
        @NotBlank
        String deviceType,
        @NotNull
        @NotBlank
        String deviceIdentifier
) {
}
