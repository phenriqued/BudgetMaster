package phenriqued.BudgetMaster.DTOs.Expense;

public record RequestUpdateExpenseCategoryDTO(
        String name,
        Long spendingPriorityId) {
}
