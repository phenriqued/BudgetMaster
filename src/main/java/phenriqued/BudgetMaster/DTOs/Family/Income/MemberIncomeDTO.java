package phenriqued.BudgetMaster.DTOs.Family.Income;

import java.math.BigDecimal;

public record MemberIncomeDTO(
        String name,
        BigDecimal totalIncome) {
}
