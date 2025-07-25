package phenriqued.BudgetMaster.DTOs.Security.TwoFactorAuth;

import jakarta.validation.constraints.NotBlank;

public record RequestValid2faDTO(
        @NotBlank
        String code,
        @NotBlank
        String type2fa) {
}
