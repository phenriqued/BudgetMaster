package phenriqued.BudgetMaster.Services.FamilyService;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import phenriqued.BudgetMaster.DTOs.Family.Income.MemberIncomeDTO;
import phenriqued.BudgetMaster.DTOs.Family.Income.ResponseFamilyIncomeDTO;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Models.FamilyEntity.RoleFamily;
import phenriqued.BudgetMaster.Models.FamilyEntity.UserFamily;
import phenriqued.BudgetMaster.Models.IncomeEntity.Income;
import phenriqued.BudgetMaster.Repositories.IncomeRepository.IncomeRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
public class FamilyIncomeService {

    private final FamilyConfigService familyConfigService;
    private final IncomeRepository incomeRepository;


    public ResponseFamilyIncomeDTO getAllIncomeFamily(Long id, UserDetailsImpl userDetails) {
        var user = userDetails.getUser();
        var family = familyConfigService.validateFamilyAccess(id, user);

        List<MemberIncomeDTO> memberIncomeList = family.getUserFamilies().stream()
                .filter(userFamily -> userFamily.getRoleFamily().equals(RoleFamily.MEMBER) || userFamily.getRoleFamily().equals(RoleFamily.OWNER))
                .map(UserFamily::getUser)
                .map(userMember -> {
                    BigDecimal memberTotal = incomeRepository.findAllByUser(userMember).stream().map(Income::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new MemberIncomeDTO(userMember.getName(), memberTotal);
                }).toList();

        BigDecimal totalIncome = memberIncomeList.stream()
                .map(MemberIncomeDTO::totalIncome).
                reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ResponseFamilyIncomeDTO(memberIncomeList, totalIncome);
    }


}
