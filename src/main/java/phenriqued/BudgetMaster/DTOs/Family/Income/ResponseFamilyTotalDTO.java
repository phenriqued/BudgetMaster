package phenriqued.BudgetMaster.DTOs.Family.Income;

import java.math.BigDecimal;
import java.util.List;

public record ResponseFamilyTotalDTO(
        List<MemberFamilyTotalDTO> memberList,
        BigDecimal total) {
}
