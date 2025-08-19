package phenriqued.BudgetMaster.Services.FamilyService;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import phenriqued.BudgetMaster.DTOs.Family.UpdateFamilyNameDTO;
import phenriqued.BudgetMaster.Infra.Email.FamilyEmailService;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BudgetMasterSecurityException;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Models.FamilyEntity.Family;
import phenriqued.BudgetMaster.Models.FamilyEntity.RoleFamily;
import phenriqued.BudgetMaster.Models.UserEntity.User;
import phenriqued.BudgetMaster.Repositories.FamilyRepositories.FamilyRepository;
import phenriqued.BudgetMaster.Repositories.FamilyRepositories.UserFamilyRepository;

@Service
public class FamilyConfigService {

    private final FamilyRepository familyRepository;
    private final UserFamilyRepository userFamilyRepository;
    private final FamilyEmailService familyEmailService;

    public FamilyConfigService(FamilyRepository familyRepository, UserFamilyRepository userFamilyRepository, FamilyEmailService familyEmailService) {
        this.familyRepository = familyRepository;
        this.userFamilyRepository = userFamilyRepository;
        this.familyEmailService = familyEmailService;
    }

    public void updateFamilyName(Long id, UpdateFamilyNameDTO updateDTO, UserDetailsImpl userDetails) {
        var user = userDetails.getUser();
        var family = validateFamilyAccess(id, user);
        ensureUserIsFamilyOwner(family, user);

        family.setName(updateDTO.newName());
        familyRepository.flush();
    }

    private void ensureUserIsFamilyOwner(Family family, User user){
        if(!userFamilyRepository.existsByFamilyAndUserAndRoleFamily(family, user, RoleFamily.OWNER))
            throw new BudgetMasterSecurityException("You do not have permission to perform the operation");
    }

    private Family validateFamilyAccess(Long id, User user){
        var family = familyRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Family Id not found!"));
        if (!userFamilyRepository.existsByUserAndFamily(user, family)) throw new BudgetMasterSecurityException("family id does not belong to user");
        return family;
    }
}
