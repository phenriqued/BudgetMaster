package phenriqued.BudgetMaster.Services.FamilyService;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import phenriqued.BudgetMaster.DTOs.Family.FamilyMemberDTO;
import phenriqued.BudgetMaster.DTOs.Family.RequestCreateFamilyDTO;
import phenriqued.BudgetMaster.DTOs.Family.ResponseCreatedFamilyDTO;
import phenriqued.BudgetMaster.DTOs.Family.ResponseUserFamilyDTO;
import phenriqued.BudgetMaster.Infra.Email.FamilyEmailService;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BusinessRuleException;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Models.FamilyEntity.Family;
import phenriqued.BudgetMaster.Models.FamilyEntity.RoleFamily;
import phenriqued.BudgetMaster.Models.FamilyEntity.UserFamily;
import phenriqued.BudgetMaster.Repositories.FamilyRepositories.FamilyRepository;
import phenriqued.BudgetMaster.Repositories.FamilyRepositories.UserFamilyRepository;
import phenriqued.BudgetMaster.Services.UserServices.UserService;

import java.util.ArrayList;
import java.util.List;

@Service
public class FamilyService {

    private final FamilyRepository familyRepository;
    private final UserFamilyRepository userFamilyRepository;
    private final FamilyEmailService familyEmailService;
    private final UserService userService;

    public FamilyService(FamilyRepository familyRepository, UserFamilyRepository userFamilyRepository, FamilyEmailService familyEmailService, UserService userService) {
        this.familyRepository = familyRepository;
        this.userFamilyRepository = userFamilyRepository;
        this.familyEmailService = familyEmailService;
        this.userService = userService;
    }

    public ResponseCreatedFamilyDTO createFamily(UserDetailsImpl userDetails, RequestCreateFamilyDTO createFamilyDTO){
        var user = userDetails.getUser();
        int ownerCount =  user.getFamily().stream().filter(userFamily -> userFamily.getRoleFamily().equals(RoleFamily.OWNER)).toList().size();
        if(ownerCount >= 5){
            throw new BusinessRuleException("The user has more than five \"Families\" created");
        }

        var family = familyRepository.save(new Family(createFamilyDTO));
        UserFamily userFamily = userFamilyRepository.save(new UserFamily(user, family, RoleFamily.OWNER));
        family.addUserFamily(userFamily);

        var usersNotFound = invitedFamilyMembers(createFamilyDTO.members(), family, userFamily);

        familyRepository.flush();
        var userFamilyDTO = new ResponseUserFamilyDTO(userFamily);
        return new ResponseCreatedFamilyDTO(family, List.of(userFamilyDTO), usersNotFound);
    }

    private List<String> invitedFamilyMembers(List<FamilyMemberDTO> memberList, Family family, UserFamily userOwner){
        List<String> emailsNotFound = new ArrayList<>();
        for (FamilyMemberDTO member : memberList){
            var role = RoleFamily.valueOf(member.role().toUpperCase());
            try{
                var userInvited = userService.findUserByEmail(member.email());
                familyEmailService.invitedMember(userInvited, family, role, userOwner);
            }catch (UsernameNotFoundException e){
                emailsNotFound.add(member.email());
            }
        }
        if (emailsNotFound.isEmpty()) emailsNotFound.add("invitations were sent to all members");

        return emailsNotFound;
    }

}
