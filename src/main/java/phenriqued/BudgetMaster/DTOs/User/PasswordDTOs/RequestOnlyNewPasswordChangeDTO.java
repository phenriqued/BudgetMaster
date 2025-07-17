package phenriqued.BudgetMaster.DTOs.User.PasswordDTOs;

import jakarta.validation.constraints.NotNull;

public record RequestOnlyNewPasswordChangeDTO(
        @NotNull
        String newPassword) {
}
