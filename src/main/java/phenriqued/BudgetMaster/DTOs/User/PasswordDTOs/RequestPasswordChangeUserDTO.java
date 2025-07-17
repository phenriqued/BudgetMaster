package phenriqued.BudgetMaster.DTOs.User.PasswordDTOs;

import jakarta.validation.constraints.NotNull;

public record RequestPasswordChangeUserDTO(
        @NotNull
        String password,
        @NotNull
        String newPassword
) {
}
