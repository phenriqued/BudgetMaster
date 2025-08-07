package phenriqued.BudgetMaster.DTOs.Income;

import jakarta.validation.constraints.NotBlank;
import phenriqued.BudgetMaster.Models.IncomeEntity.Income;

import java.time.format.DateTimeFormatter;

public record ResponseIncomesDTO(
        @NotBlank
        Long id,
        @NotBlank
        String description,
        @NotBlank
        String amount,
        @NotBlank
        String entryDate
) {
    public ResponseIncomesDTO(Income entity){
        this(entity.getId(), entity.getDescription(),entity.getAmount().toString(),
                entity.getEntryDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
    }
}
