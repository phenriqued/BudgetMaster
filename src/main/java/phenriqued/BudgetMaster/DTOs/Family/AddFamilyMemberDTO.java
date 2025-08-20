package phenriqued.BudgetMaster.DTOs.Family;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddFamilyMemberDTO(
        @NotBlank
        String email,
        @NotNull
        Long roleId
) {
}
