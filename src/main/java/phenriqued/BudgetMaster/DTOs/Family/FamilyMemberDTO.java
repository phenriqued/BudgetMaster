package phenriqued.BudgetMaster.DTOs.Family;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FamilyMemberDTO(
        @NotBlank
        String email,
        @NotNull
        Long role
) {
}
