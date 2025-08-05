package phenriqued.BudgetMaster.DTOs.Expense;

import jakarta.validation.constraints.NotBlank;

public record RequestCreateExpenseCategoryDTO(
        @NotBlank
        String name,
        @NotBlank
        String spendingPriority) {

}
