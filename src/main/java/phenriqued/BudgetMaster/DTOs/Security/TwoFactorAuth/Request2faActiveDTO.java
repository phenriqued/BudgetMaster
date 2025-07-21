package phenriqued.BudgetMaster.DTOs.Security.TwoFactorAuth;

import jakarta.validation.constraints.NotBlank;

public record Request2faActiveDTO(
        @NotBlank
        String type2FA
) {
    public Request2faActiveDTO(String type2FA){
        this.type2FA = type2FA.toUpperCase();
    }

}
