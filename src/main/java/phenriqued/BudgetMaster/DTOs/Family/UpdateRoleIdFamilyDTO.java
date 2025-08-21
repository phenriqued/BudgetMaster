package phenriqued.BudgetMaster.DTOs.Family;

import jakarta.validation.constraints.NotNull;

public record UpdateRoleIdFamilyDTO(
        @NotNull
        Long roleId,
        @NotNull
        Long memberId
) {
}
