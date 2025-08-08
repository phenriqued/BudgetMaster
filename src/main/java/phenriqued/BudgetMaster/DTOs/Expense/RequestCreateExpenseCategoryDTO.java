package phenriqued.BudgetMaster.DTOs.Expense;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RequestCreateExpenseCategoryDTO(
        @NotBlank
        String name,
        @NotNull
        Long spendingPriorityId) {

}
