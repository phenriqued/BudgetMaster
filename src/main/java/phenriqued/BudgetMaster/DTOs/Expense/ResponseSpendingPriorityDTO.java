package phenriqued.BudgetMaster.DTOs.Expense;

import phenriqued.BudgetMaster.Models.ExpenseEntity.Category.SpendingPriority;

public record ResponseSpendingPriorityDTO(
        Long id,
        String name) {
    public ResponseSpendingPriorityDTO(SpendingPriority entity){
        this(entity.id, entity.name());
    }
}
