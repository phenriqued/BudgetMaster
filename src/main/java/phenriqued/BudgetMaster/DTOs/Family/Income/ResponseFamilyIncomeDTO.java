package phenriqued.BudgetMaster.DTOs.Family.Income;

import java.math.BigDecimal;
import java.util.List;

public record ResponseFamilyIncomeDTO(
        List<MemberIncomeDTO> memberIncomeList,
        BigDecimal totalIncome) {
}
