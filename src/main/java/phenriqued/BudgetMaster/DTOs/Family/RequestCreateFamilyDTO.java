package phenriqued.BudgetMaster.DTOs.Family;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RequestCreateFamilyDTO(
        @NotBlank
        String name,
        @NotNull
        List<FamilyMemberDTO> members
) {
}
