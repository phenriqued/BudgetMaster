package phenriqued.BudgetMaster.DTOs.Security.Token;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RequestTokenDTO(
        @NotBlank
        @NotNull
        String tokenType,
        @NotBlank
        @NotNull
        String identifier
) {
}
