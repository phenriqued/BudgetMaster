package phenriqued.BudgetMaster.Controllers.FamilyControllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import phenriqued.BudgetMaster.DTOs.EmergencyReserve.ResponseEmergencyReserveDTO;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Services.FamilyService.FamilyEmergencyReserveService;

@RestController
@RequestMapping("/families/{id}/emergency-reserve")
public class FamilyEmergencyReserveController {

    private final FamilyEmergencyReserveService service;

    public FamilyEmergencyReserveController(FamilyEmergencyReserveService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ResponseEmergencyReserveDTO> getFamilyEmergencyReserve(@PathVariable(value = "id") Long id,
                                                                                 @AuthenticationPrincipal UserDetailsImpl userDetails){
        return ResponseEntity.ok(service.getTotalReserveFamily(id, userDetails));
    }

}
