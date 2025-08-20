package phenriqued.BudgetMaster.Services.FamilyService;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import phenriqued.BudgetMaster.DTOs.Family.AddFamilyMemberDTO;
import phenriqued.BudgetMaster.DTOs.Family.RoleIdFamilyDTO;
import phenriqued.BudgetMaster.DTOs.Family.UpdateFamilyNameDTO;
import phenriqued.BudgetMaster.Infra.Email.FamilyEmailService;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BudgetMasterSecurityException;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BusinessRuleException;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Models.FamilyEntity.Family;
import phenriqued.BudgetMaster.Models.FamilyEntity.RoleFamily;
import phenriqued.BudgetMaster.Models.FamilyEntity.UserFamily;
import phenriqued.BudgetMaster.Models.UserEntity.User;
import phenriqued.BudgetMaster.Repositories.FamilyRepositories.FamilyRepository;
import phenriqued.BudgetMaster.Repositories.FamilyRepositories.UserFamilyRepository;
import phenriqued.BudgetMaster.Services.Security.TokensService.TokenService;
import phenriqued.BudgetMaster.Services.UserServices.UserService;

@Service
public class FamilyConfigService {

    private final FamilyRepository familyRepository;
    private final UserFamilyRepository userFamilyRepository;
    private final UserService userService;
    private final FamilyEmailService familyEmailService;
    private final TokenService tokenService;


    public FamilyConfigService(FamilyRepository familyRepository, UserFamilyRepository userFamilyRepository, UserService userService, FamilyEmailService familyEmailService, TokenService tokenService) {
        this.familyRepository = familyRepository;
        this.userFamilyRepository = userFamilyRepository;
        this.userService = userService;
        this.familyEmailService = familyEmailService;
        this.tokenService = tokenService;
    }

    public void updateFamilyName(Long id, UpdateFamilyNameDTO updateDTO, UserDetailsImpl userDetails) {
        var user = userDetails.getUser();
        var family = validateFamilyAccess(id, user);
        ensureUserIsFamilyOwner(family, user);

        family.setName(updateDTO.newName());
        familyRepository.flush();
    }

    public void addFamilyMemberByEmail(Long id, AddFamilyMemberDTO addFamilyMemberDTO, UserDetailsImpl userDetails) {
        var principalUser = userDetails.getUser();
        var family = validateFamilyAccess(id, principalUser);
        ensureUserIsFamilyOwner(family, principalUser);
        var userMember = userService.findUserByEmail(addFamilyMemberDTO.email());
        validateUserInTheFamily(userMember, family);
        var roleFamily = validateNonOwnerRoleId(addFamilyMemberDTO.roleId());

        familyEmailService.invitedMember(userMember, family, roleFamily, principalUser);
    }

    public String addFamilyMembersByQrCode(Long id, RoleIdFamilyDTO roleIdFamilyDTO, UserDetailsImpl userDetails) {
        var user = userDetails.getUser();
        var family = validateFamilyAccess(id, user);
        ensureUserIsFamilyOwner(family, user);
        var roleFamily = validateNonOwnerRoleId(roleIdFamilyDTO.roleId());
        String accessToken = tokenService.generatedTokenJwtAtFamily(family, roleFamily.id);
        return "http://localhost:8080/families/"+id+"/accept-invitation?access="+accessToken;
    }

    public void acceptInvitationByQrCode(Long id, String access, UserDetailsImpl userDetails) {
        var user = userDetails.getUser();
        var family = familyRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Family Id not found!"));
        var token = tokenService.validationTokenJwtAtFamily(access);
        if(! token.getClaim("familyId").asLong().equals(family.getId())) throw new BudgetMasterSecurityException("Unable to access family");
        var role = validateNonOwnerRoleId(token.getClaim("roleId").asLong());

        var userFamily = userFamilyRepository.save(new UserFamily(user, family, role));
        family.addUserFamily(userFamily);
        familyRepository.flush();
    }

    private void ensureUserIsFamilyOwner(Family family, User user){
        if(!userFamilyRepository.existsByFamilyAndUserAndRoleFamily(family, user, RoleFamily.OWNER))
            throw new BudgetMasterSecurityException("You do not have permission to perform the operation");
    }
    private RoleFamily validateNonOwnerRoleId(Long roleId){
        var roleFamily = RoleFamily.fromId(roleId).orElseThrow(() -> new EntityNotFoundException("Role Family not found!"));
        if(roleFamily.equals(RoleFamily.OWNER)) throw new BusinessRuleException("Unable to add a member as OWNER at this time");
        return roleFamily;
    }
    private Family validateFamilyAccess(Long id, User user) {
        var family = familyRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Family Id not found!"));
        if (!userFamilyRepository.existsByUserAndFamily(user, family))
            throw new BudgetMasterSecurityException("family id does not belong to user");
        return family;
    }
    private void validateUserInTheFamily(User user, Family family) {
        if (userFamilyRepository.existsByUserAndFamily(user, family))
            throw new BusinessRuleException("The user already belongs to the family");
    }
}
