package phenriqued.BudgetMaster.DTOs.Income;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import phenriqued.BudgetMaster.Models.IncomeEntity.Income;

public record RequestNewIncome(
        @NotBlank
        String description,
        @NotBlank
        @Pattern(regexp = "^\\d+(\\.\\d{1,2})?$",
                message = "the value must correspond to one or more positive numeric digits followed by a period and finally up to two numeric digits")
        String amount
) {
    public RequestNewIncome(Income entity){
        this(entity.getDescription(), entity.getAmount().toString());
    }
}
