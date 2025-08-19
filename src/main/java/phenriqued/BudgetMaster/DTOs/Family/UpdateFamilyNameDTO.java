package phenriqued.BudgetMaster.DTOs.Family;

import jakarta.validation.constraints.NotBlank;

public record UpdateFamilyNameDTO(
        @NotBlank
        String newName) {
}
