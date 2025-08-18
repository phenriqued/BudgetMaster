package phenriqued.BudgetMaster.DTOs.Family;

import java.util.List;

public record ResponseGetFamilyDTO(
        String name,
        List<ResponseUserFamilyDTO> members) {

}
