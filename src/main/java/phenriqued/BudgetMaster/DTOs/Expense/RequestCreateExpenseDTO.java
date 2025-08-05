package phenriqued.BudgetMaster.DTOs.Expense;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RequestCreateExpenseDTO(
        @NotBlank
        String description,
        @NotBlank
        @Pattern(regexp = "^\\d+(\\.\\d{1,2})?$",
                message = "the value must correspond to one or more positive numeric digits followed by a period and finally up to two numeric digits")
        String amount,
        @NotBlank
        Long categoryId
) {
}
