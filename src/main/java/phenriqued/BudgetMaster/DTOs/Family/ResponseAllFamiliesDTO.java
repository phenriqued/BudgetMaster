package phenriqued.BudgetMaster.DTOs.Family;

import phenriqued.BudgetMaster.Models.FamilyEntity.Family;

public record ResponseAllFamiliesDTO(
        Long id,
        String name) {
    public ResponseAllFamiliesDTO (Family entity){
        this(entity.getId(), entity.getName());
    }
}
