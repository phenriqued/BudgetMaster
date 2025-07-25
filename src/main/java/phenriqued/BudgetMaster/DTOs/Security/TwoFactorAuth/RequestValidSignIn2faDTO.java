package phenriqued.BudgetMaster.DTOs.Security.TwoFactorAuth;

import jakarta.validation.constraints.NotBlank;

public record RequestValidSignIn2faDTO(
        @NotBlank
        String code,
        @NotBlank
        String type2fa,
        @NotBlank
        String securityUserToken2fa,
        String tokenType,
        String identifier
) {
}
