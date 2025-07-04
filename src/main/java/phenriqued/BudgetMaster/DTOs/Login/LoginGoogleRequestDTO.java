package phenriqued.BudgetMaster.DTOs.Login;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginGoogleRequestDTO(
        @NotNull
        @NotBlank
        String token,
        @NotNull
        @NotBlank
        String tokenType,
        @NotNull
        @NotBlank
        String identifier) {
}
