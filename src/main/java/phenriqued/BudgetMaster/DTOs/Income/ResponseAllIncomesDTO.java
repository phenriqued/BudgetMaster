package phenriqued.BudgetMaster.DTOs.Income;

import java.math.BigDecimal;
import java.util.List;

public record ResponseAllIncomesDTO(
        List<ResponseIncomesDTO> incomes,
        BigDecimal total
) {

}
