package phenriqued.BudgetMaster.DTOs.EmergencyReserve;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ResponseEmergencyReserveDTO(
        BigDecimal idealReserve,
        String currency,
        LocalDate calculateAt) {
}
