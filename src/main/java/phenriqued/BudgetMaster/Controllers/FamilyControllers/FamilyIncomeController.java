package phenriqued.BudgetMaster.Controllers.FamilyControllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import phenriqued.BudgetMaster.DTOs.Family.Income.ResponseFamilyTotalDTO;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Services.FamilyService.FamilyIncomeService;

@RestController
@RequestMapping("/families/{id}/income")
public class FamilyIncomeController {

    private final FamilyIncomeService familyIncomeService;

    public FamilyIncomeController(FamilyIncomeService familyIncomeService) {
        this.familyIncomeService = familyIncomeService;
    }

    @GetMapping
    public ResponseEntity<ResponseFamilyTotalDTO> listAllIncomeFamily(@PathVariable(value = "id") Long id,
                                                                      @AuthenticationPrincipal UserDetailsImpl userDetails){
        return ResponseEntity.ok(familyIncomeService.getAllIncomeFamily(id, userDetails));
    }



}
