package phenriqued.BudgetMaster.DTOs.Family.Income;

import java.math.BigDecimal;

public record MemberFamilyTotalDTO(
        String name,
        BigDecimal total) {
}
