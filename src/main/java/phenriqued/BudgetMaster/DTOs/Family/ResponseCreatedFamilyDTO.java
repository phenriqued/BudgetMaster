package phenriqued.BudgetMaster.DTOs.Family;

import phenriqued.BudgetMaster.Models.FamilyEntity.Family;

import java.util.List;

public record ResponseCreatedFamilyDTO(
        Long id,
        String name,
        List<ResponseUserFamilyDTO> users,
        List<String> invitesNotSent

        ) {
    public ResponseCreatedFamilyDTO(Family entity, List<ResponseUserFamilyDTO> userList, List<String> invitesNotSent){
        this(entity.getId(), entity.getName(), userList, invitesNotSent);
    }

}
