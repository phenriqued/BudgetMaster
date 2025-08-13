package phenriqued.BudgetMaster.DTOs.Family;

import jakarta.validation.constraints.NotBlank;

public record FamilyMemberDTO(
        @NotBlank
        String email,
        @NotBlank
        String role
) {
}
