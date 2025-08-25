package phenriqued.BudgetMaster.Controllers.FamilyControllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import phenriqued.BudgetMaster.DTOs.Family.FinancialMovement.ResponseFamilyTotalDTO;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Services.FamilyService.FamilyExpenseService;

@RestController
@RequestMapping("/families/{id}/expense")
public class FamilyExpenseController {

    private final FamilyExpenseService familyExpenseService;

    public FamilyExpenseController(FamilyExpenseService familyExpenseService) {
        this.familyExpenseService = familyExpenseService;
    }

    @GetMapping
    public ResponseEntity<ResponseFamilyTotalDTO> listAllExpenseFamily(@PathVariable(value = "id") Long id,
                                                                       @AuthenticationPrincipal UserDetailsImpl userDetails){
        return ResponseEntity.ok(familyExpenseService.getAllIncomeFamily(id, userDetails));
    }

}
