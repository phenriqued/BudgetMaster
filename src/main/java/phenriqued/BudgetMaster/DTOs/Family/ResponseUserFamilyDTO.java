package phenriqued.BudgetMaster.DTOs.Family;

import phenriqued.BudgetMaster.Models.FamilyEntity.RoleFamily;
import phenriqued.BudgetMaster.Models.FamilyEntity.UserFamily;

import java.time.format.DateTimeFormatter;

public record ResponseUserFamilyDTO(
        Long publicId,
        String username,
        RoleFamily role,
        String joinedAt) {
    public ResponseUserFamilyDTO(UserFamily entity){
        this(entity.getUser().getId(), entity.getUser().getName(), entity.getRoleFamily(),
                entity.getJoinedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }
}
