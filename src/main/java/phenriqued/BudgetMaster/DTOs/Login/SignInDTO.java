package phenriqued.BudgetMaster.DTOs.Login;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record SignInDTO(
        @Email
        @NotNull
        String email,
        @NotNull
        String password
) {
}
