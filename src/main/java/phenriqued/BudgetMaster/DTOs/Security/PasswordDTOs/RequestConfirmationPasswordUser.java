package phenriqued.BudgetMaster.DTOs.Security.PasswordDTOs;

import jakarta.validation.constraints.NotNull;

public record RequestConfirmationPasswordUser(
        @NotNull
        String password) {
}
