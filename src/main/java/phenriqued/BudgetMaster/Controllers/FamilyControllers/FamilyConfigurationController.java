package phenriqued.BudgetMaster.Controllers.FamilyControllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import phenriqued.BudgetMaster.DTOs.Family.UpdateFamilyNameDTO;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Services.FamilyService.FamilyConfigService;


@RestController
@RequestMapping("/family/settings")
public class FamilyConfigurationController {

    private final FamilyConfigService service;

    public FamilyConfigurationController(FamilyConfigService service) {
        this.service = service;
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateFamilyName(@PathVariable("id") Long id, @RequestBody UpdateFamilyNameDTO updateDTO,
                                             @AuthenticationPrincipal UserDetailsImpl userDetails){
        service.updateFamilyName(id, updateDTO, userDetails);
        return ResponseEntity.noContent().build();
    }

}
