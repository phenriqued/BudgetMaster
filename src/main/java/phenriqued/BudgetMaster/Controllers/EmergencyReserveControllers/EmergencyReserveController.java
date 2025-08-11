package phenriqued.BudgetMaster.Controllers.EmergencyReserveControllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import phenriqued.BudgetMaster.DTOs.EmergencyReserve.ResponseEmergencyReserveDTO;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BusinessRuleException;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Services.EmergencyReserveService.ReserveService;

@RestController
@RequestMapping("/emergency-reserve")
public class EmergencyReserveController {

    private final ReserveService service;

    public EmergencyReserveController(ReserveService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ResponseEmergencyReserveDTO> getEmergencyReserve(@AuthenticationPrincipal UserDetailsImpl userDetails){
        try{
            return ResponseEntity.ok(service.getTotalEmergencyReserve(userDetails));
        }catch (BusinessRuleException e){
            return ResponseEntity.noContent().build();
        }
    }
    @GetMapping("/progress")
    public ResponseEntity<?> getEmergencyReserveProgress(@AuthenticationPrincipal UserDetailsImpl userDetails){
        try {
            return ResponseEntity.ok(service.getEmergencyReserveProgress(userDetails));
        }catch (BusinessRuleException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/simulation")
    public ResponseEntity<?> getSimulationEmergencyReserveProgress(@RequestParam(value = "monthlySaving") Integer monthlySaving,
                                                                   @AuthenticationPrincipal UserDetailsImpl userDetails){
        try {
            return ResponseEntity.ok(service.getSimulationEmergencyReserveProgress(monthlySaving, userDetails));
        }catch (BusinessRuleException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }



}
