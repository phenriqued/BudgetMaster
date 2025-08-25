package phenriqued.BudgetMaster.Services.FamilyService;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import phenriqued.BudgetMaster.DTOs.EmergencyReserve.ResponseEmergencyReserveDTO;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Models.FamilyEntity.UserFamily;
import phenriqued.BudgetMaster.Services.EmergencyReserveService.ReserveService;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@AllArgsConstructor
public class FamilyEmergencyReserveService {

    private final ReserveService reserveService;
    private final FamilyConfigService familyConfigService;

    public ResponseEmergencyReserveDTO getTotalReserveFamily(Long idFamily, UserDetailsImpl userDetails){
        var user = userDetails.getUser();
        var family = familyConfigService.validateFamilyAccess(idFamily, user);
        var idealReserve = family.getUserFamilies().stream()
                .map(UserFamily::getUser)
                .map(reserveService::getIdealReserve)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ResponseEmergencyReserveDTO(idealReserve, "BRL", LocalDate.now());
    }


}
