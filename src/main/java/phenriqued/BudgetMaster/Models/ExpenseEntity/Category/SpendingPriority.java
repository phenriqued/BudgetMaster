package phenriqued.BudgetMaster.Models.ExpenseEntity.Category;

import java.util.Arrays;
import java.util.Optional;

public enum SpendingPriority {

    ESSENTIAL(1L),
    NONESSENTIAL(2L);

    public final Long id;
    SpendingPriority(Long id){
        this.id = id;
    }

    public static Optional<SpendingPriority> fromId(Long id) {
        return Arrays.stream(SpendingPriority.values())
                .filter(priority -> priority.id.equals(id))
                .findFirst();
    }

}
