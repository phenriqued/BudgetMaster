package phenriqued.BudgetMaster.DTOs.Family;

import phenriqued.BudgetMaster.Models.FamilyEntity.Family;

import java.util.List;
import java.util.Map;

public record ResponseCreatedFamilyDTO(
        Long id,
        String name,
        List<ResponseUserFamilyDTO> users,
        Map<String, String> invitesNotSent

        ) {
    public ResponseCreatedFamilyDTO(Family entity, List<ResponseUserFamilyDTO> userList, Map<String, String> invitesNotSent){
        this(entity.getId(), entity.getName(), userList, invitesNotSent);
    }

}
