package phenriqued.BudgetMaster.DTOs.Family;

import jakarta.validation.constraints.NotNull;

public record RoleIdFamilyDTO(
        @NotNull
        Long roleId) {
}
