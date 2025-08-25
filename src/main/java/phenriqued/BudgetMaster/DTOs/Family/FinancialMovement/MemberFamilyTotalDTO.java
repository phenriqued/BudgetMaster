package phenriqued.BudgetMaster.DTOs.Family.FinancialMovement;

import java.math.BigDecimal;

public record MemberFamilyTotalDTO(
        String name,
        BigDecimal total) {
}
