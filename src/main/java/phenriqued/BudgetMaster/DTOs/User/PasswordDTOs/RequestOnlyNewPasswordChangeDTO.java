package phenriqued.BudgetMaster.DTOs.User.PasswordDTOs;

import jakarta.validation.constraints.NotNull;
import phenriqued.BudgetMaster.Infra.Security.PasswordValidation.PasswordValid;

public record RequestOnlyNewPasswordChangeDTO(
        @NotNull
        @PasswordValid
        String newPassword) {
}
