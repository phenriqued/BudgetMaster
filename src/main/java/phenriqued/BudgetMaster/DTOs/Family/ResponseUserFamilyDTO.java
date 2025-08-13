package phenriqued.BudgetMaster.DTOs.Family;

import phenriqued.BudgetMaster.Models.FamilyEntity.RoleFamily;
import phenriqued.BudgetMaster.Models.FamilyEntity.UserFamily;

import java.time.format.DateTimeFormatter;

public record ResponseUserFamilyDTO(
        String username,
        RoleFamily role,
        String joinedAt) {
    public ResponseUserFamilyDTO(UserFamily entity){
        this(entity.getUser().getName(), entity.getRoleFamily(),
                entity.getJoinedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }
}
