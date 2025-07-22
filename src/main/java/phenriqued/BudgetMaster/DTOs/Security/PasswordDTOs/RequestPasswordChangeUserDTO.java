package phenriqued.BudgetMaster.DTOs.Security.PasswordDTOs;

import jakarta.validation.constraints.NotNull;
import phenriqued.BudgetMaster.Infra.Security.PasswordValidation.PasswordValid;

public record RequestPasswordChangeUserDTO(
        @NotNull
        String password,
        @NotNull
        @PasswordValid
        String newPassword
) {
}
