package phenriqued.BudgetMaster.DTOs.Expense;

public record RequestUpdateExpenseDTO(
        String description,
        String amount,
        Long categoryId) {
}
