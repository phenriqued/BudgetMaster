package phenriqued.BudgetMaster.DTOs.Expense;

import phenriqued.BudgetMaster.Models.ExpenseEntity.Category.ExpenseCategory;
import phenriqued.BudgetMaster.Models.ExpenseEntity.Category.SpendingPriority;

public record ResponseCategoryDTO(
        Long id,
        String name,
        SpendingPriority spendingPriority) {
    public ResponseCategoryDTO(ExpenseCategory entity){
        this(entity.getId(), entity.getName(), entity.getSpendingPriority());
    }
}
