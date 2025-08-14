package phenriqued.BudgetMaster.Models.FamilyEntity;

import java.util.Arrays;
import java.util.Optional;

public enum RoleFamily {

    OWNER(1L),
    MEMBER(2L),
    VIEWER(3L);

    public final Long id;

    RoleFamily(long id) {
        this.id = id;
    }

    public static Optional<RoleFamily> fromId(Long id){
        return Arrays.stream(RoleFamily.values())
                .filter(role -> role.id.equals(id))
                .findFirst();
    }
}
