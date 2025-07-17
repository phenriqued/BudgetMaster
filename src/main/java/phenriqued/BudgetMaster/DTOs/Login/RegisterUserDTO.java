package phenriqued.BudgetMaster.DTOs.Login;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.method.P;
import phenriqued.BudgetMaster.Infra.Security.PasswordValidation.PasswordValid;

public record RegisterUserDTO(
        @NotNull
        @NotBlank
        String name,
        @Email
        @NotNull
        String email,
        @NotNull
        @NotBlank
        @PasswordValid
        String password
) {
}
