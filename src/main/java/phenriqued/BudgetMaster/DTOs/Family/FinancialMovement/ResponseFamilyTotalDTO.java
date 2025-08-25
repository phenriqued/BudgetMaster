package phenriqued.BudgetMaster.DTOs.Family.FinancialMovement;

import java.math.BigDecimal;
import java.util.List;

public record ResponseFamilyTotalDTO(
        List<MemberFamilyTotalDTO> memberList,
        BigDecimal total) {
}
