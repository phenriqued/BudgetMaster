package phenriqued.BudgetMaster.Services.FamilyService;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import phenriqued.BudgetMaster.DTOs.Family.Income.MemberFamilyTotalDTO;
import phenriqued.BudgetMaster.DTOs.Family.Income.ResponseFamilyTotalDTO;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Models.ExpenseEntity.Expense;
import phenriqued.BudgetMaster.Models.FamilyEntity.RoleFamily;
import phenriqued.BudgetMaster.Models.FamilyEntity.UserFamily;
import phenriqued.BudgetMaster.Repositories.ExpenseRepository.ExpenseRepository;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@Service
public class FamilyExpenseService {

    private final FamilyConfigService familyConfigService;
    private final ExpenseRepository expenseRepository;

    public ResponseFamilyTotalDTO getAllIncomeFamily(Long id, UserDetailsImpl userDetails) {
        var user = userDetails.getUser();
        var family = familyConfigService.validateFamilyAccess(id, user);

        List<MemberFamilyTotalDTO> memberExpenseList = family.getUserFamilies().stream()
                .filter(userFamily -> userFamily.getRoleFamily().equals(RoleFamily.OWNER) || userFamily.getRoleFamily().equals(RoleFamily.MEMBER))
                .map(UserFamily::getUser)
                .map(userExpense -> {
                    BigDecimal total = expenseRepository.findAllByUser(userExpense).stream().map(Expense::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new MemberFamilyTotalDTO(userExpense.getName(), total);
                }).toList();
        BigDecimal totalExpense = memberExpenseList.stream().map(MemberFamilyTotalDTO::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ResponseFamilyTotalDTO(memberExpenseList, totalExpense);
    }
}
