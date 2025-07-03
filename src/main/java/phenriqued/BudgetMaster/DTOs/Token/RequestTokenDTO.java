package phenriqued.BudgetMaster.DTOs.Token;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RequestTokenDTO(
        @NotBlank
        @NotNull
        String deviceType,
        @NotBlank
        @NotNull
        String deviceIdentifier
) {
}
