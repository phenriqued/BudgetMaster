package phenriqued.BudgetMaster.DTOs.Expense;

import jakarta.validation.constraints.NotBlank;
import phenriqued.BudgetMaster.Models.ExpenseEntity.Expense;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public record ResponseExpenseDTO(
        @NotBlank
        String description,
        @NotBlank
        BigDecimal amount,
        @NotBlank
        String expenseCategory,
        @NotBlank
        String entryDate
) {
    public ResponseExpenseDTO(Expense entity){
        this(entity.getDescription(), entity.getAmount(), entity.getExpenseCategory().getName(),
                entity.getEntryDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
    }
}
