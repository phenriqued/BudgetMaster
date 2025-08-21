package phenriqued.BudgetMaster.DTOs.Family;

import jakarta.validation.constraints.NotNull;

public record FamilyMemberIdDTO(
        @NotNull
        Long memberId) {
}
