package phenriqued.BudgetMaster.Services.FamilyService;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import phenriqued.BudgetMaster.DTOs.Family.*;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FamilyService {

    private final FamilyRepository familyRepository;
    private final UserFamilyRepository userFamilyRepository;
    private final FamilyEmailService familyEmailService;
    private final UserService userService;
    private final TokenService tokenService;

    public FamilyService(FamilyRepository familyRepository, UserFamilyRepository userFamilyRepository, FamilyEmailService familyEmailService, UserService userService, TokenService tokenService) {
        this.familyRepository = familyRepository;
        this.userFamilyRepository = userFamilyRepository;
        this.familyEmailService = familyEmailService;
        this.userService = userService;
        this.tokenService = tokenService;
    }

    public List<ResponseAllFamiliesDTO> getAllFamiliesByUser(UserDetailsImpl userDetails) {
        return userFamilyRepository.findAllFamilyByUser(userDetails.getUser()).stream()
                .map(ResponseAllFamiliesDTO::new).toList();
    }

    public ResponseGetFamilyDTO getFamilyById(Long id, UserDetailsImpl userDetails) {
        var user = userDetails.getUser();
        var family = validateFamilyAccess(id, user);

        var userMemberFamily = userFamilyRepository.findAllByFamily(family).stream().map(ResponseUserFamilyDTO::new).toList();
        return new ResponseGetFamilyDTO(family.getName(), userMemberFamily);
    }

    @Transactional
    public ResponseCreatedFamilyDTO createFamily(UserDetailsImpl userDetails, RequestCreateFamilyDTO createFamilyDTO){
        var user = userDetails.getUser();
        int ownerCount =  user.getFamily().stream().filter(userFamily -> userFamily.getRoleFamily().equals(RoleFamily.OWNER)).toList().size();
        if(ownerCount >= 5){
            throw new BusinessRuleException("The user has more than five \"Families\" created");
        }

        var family = familyRepository.save(new Family(createFamilyDTO));
        UserFamily userFamily = userFamilyRepository.save(new UserFamily(user, family, RoleFamily.OWNER));
        family.addUserFamily(userFamily);

        var invitesNotSent = invitedFamilyMembers(createFamilyDTO.members(), family, userFamily);

        familyRepository.flush();
        var userFamilyDTO = new ResponseUserFamilyDTO(userFamily);
        return new ResponseCreatedFamilyDTO(family, List.of(userFamilyDTO), invitesNotSent);
    }

    public void acceptFamilyInvitation(String tokenFamily) {
        var decodedToken = tokenService.validationTokenJwtAtFamily(tokenFamily);
        var user = userService.findUserByEmail(decodedToken.getSubject());

        Family family = familyRepository.findById(decodedToken.getClaim("familyId").asLong())
                .orElseThrow(() -> new EntityNotFoundException("Family id not found!"));
        RoleFamily role = RoleFamily.fromId(decodedToken.getClaim("roleId").asLong())
                .orElseThrow(() -> new EntityNotFoundException("Role Family id not found!"));

        if (userFamilyRepository.existsByUserAndFamily(user, family)) throw new BusinessRuleException("the user is already part of the "+family.getName());

        UserFamily userFamily = userFamilyRepository.save(new UserFamily(user, family, role));
        user.getFamily().add(userFamily);
        family.addUserFamily(userFamily);
        familyRepository.flush();
    }

    @Transactional
    public void deleteFamilyByIdAndUser(Long id, UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        var family = validateFamilyAccess(id, user);

        if(!userFamilyRepository.existsByFamilyAndUserAndRoleFamily(family, user, RoleFamily.OWNER))
            throw new BudgetMasterSecurityException("only Owner can delete.");

        var familyMembers = userFamilyRepository.findAllByFamily(family);
        family.getUserFamilies().removeAll(familyMembers);
        familyRepository.deleteById(family.getId());
        familyMembers.forEach(userFamily -> familyEmailService.deletionNotice(userFamily.getUser(), family));
    }

    private Map<String, String> invitedFamilyMembers(List<FamilyMemberDTO> memberList, Family family, UserFamily userOwner){
        Map<String, String> invitesNotSent = new HashMap<>();
        for (FamilyMemberDTO member : memberList){
            try{
                var role = RoleFamily.fromId(member.role()).orElseThrow(() -> new EntityNotFoundException("Role Family Id not found!"));
                if(role.equals(RoleFamily.OWNER)) throw new BusinessRuleException("Unable to add a member as OWNER at this time");
                var userInvited = userService.findUserByEmail(member.email());
                familyEmailService.invitedMember(userInvited, family, role, userOwner);
            }catch (UsernameNotFoundException e){
                invitesNotSent.put(member.email(), "Non-existent email or username, check email!");
            }catch (BusinessRuleException | EntityNotFoundException e){
                invitesNotSent.put(member.email(), "Role Family - "+member.role()+" - "+ e.getMessage());
            }
        }
        if (invitesNotSent.isEmpty()) invitesNotSent.put("Invitations sent successfully", "invitations were sent to all members!");
        return invitesNotSent;
    }

    private Family validateFamilyAccess(Long id, User user){
        var family = familyRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Family Id not found!"));
        if (!userFamilyRepository.existsByUserAndFamily(user, family)) throw new BudgetMasterSecurityException("family id does not belong to user");
        return family;
    }
}
