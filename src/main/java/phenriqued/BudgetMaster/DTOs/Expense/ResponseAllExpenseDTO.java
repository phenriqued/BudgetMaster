package phenriqued.BudgetMaster.DTOs.Expense;

import java.math.BigDecimal;
import java.util.List;

public record ResponseAllExpenseDTO(
    List<ResponseExpenseDTO> expenses,
    BigDecimal total) {
}
