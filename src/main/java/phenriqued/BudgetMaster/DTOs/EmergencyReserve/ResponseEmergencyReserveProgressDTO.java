package phenriqued.BudgetMaster.DTOs.EmergencyReserve;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ResponseEmergencyReserveProgressDTO(
        BigDecimal monthlySaving,
        String estimatedMonths,
        LocalDate estimatedCompletionDate) {
}
