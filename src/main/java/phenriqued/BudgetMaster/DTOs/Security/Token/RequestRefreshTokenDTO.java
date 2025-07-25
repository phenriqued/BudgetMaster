package phenriqued.BudgetMaster.DTOs.Security.Token;

import jakarta.validation.constraints.NotBlank;

public record RequestRefreshTokenDTO(
        @NotBlank
        String refreshToken,
        @NotBlank
        String tokenType,
        @NotBlank
        String identifier
) {
}
