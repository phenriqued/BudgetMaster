package phenriqued.BudgetMaster.Services.EmergencyReserveService;

import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import phenriqued.BudgetMaster.DTOs.EmergencyReserve.ResponseEmergencyReserveDTO;
import phenriqued.BudgetMaster.DTOs.EmergencyReserve.ResponseEmergencyReserveProgressDTO;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BusinessRuleException;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Models.ExpenseEntity.Category.SpendingPriority;
import phenriqued.BudgetMaster.Models.ExpenseEntity.Expense;
import phenriqued.BudgetMaster.Models.IncomeEntity.Income;
import phenriqued.BudgetMaster.Models.UserEntity.User;
import phenriqued.BudgetMaster.Repositories.ExpenseRepository.ExpenseRepository;
import phenriqued.BudgetMaster.Repositories.IncomeRepository.IncomeRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
public class ReserveService {

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;

    public ReserveService(IncomeRepository incomeRepository, ExpenseRepository expenseRepository) {
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
    }

    public ResponseEmergencyReserveDTO getTotalEmergencyReserve(UserDetailsImpl userDetails) {
        var user = userDetails.getUser();
        var idealReserve = getIdealReserve(user);
        if(idealReserve.equals(BigDecimal.ZERO)){
            throw new BusinessRuleException("No expenses and/or income registered");
        }
        return new ResponseEmergencyReserveDTO(idealReserve, "BRL", LocalDate.now());
    }

    public ResponseEmergencyReserveProgressDTO getEmergencyReserveProgress(UserDetailsImpl userDetails) {
        var user = userDetails.getUser();
        BigDecimal expenseEssential = getExpenseEssential(user);
        BigDecimal allIncomes = getAllIncomes(user);
        BigDecimal total = getIdealReserve(user);
        BigDecimal value = allIncomes.subtract(expenseEssential);
        if(total.equals(BigDecimal.ZERO)){
            throw new BusinessRuleException("It is not possible to generate progress for the management of the emergency reserve, when there are no expenses generated");
        }
        String progress = total.divide(value, 0, RoundingMode.HALF_UP).toString();
        LocalDate estimatedCompletionDate = LocalDate.now().plusMonths(Long.parseLong(progress));
        return new ResponseEmergencyReserveProgressDTO(value, progress, estimatedCompletionDate);
    }
    public ResponseEmergencyReserveProgressDTO getSimulationEmergencyReserveProgress(@NotNull Integer monthlySaving, UserDetailsImpl userDetails) {
        if (monthlySaving <= 0) throw new BusinessRuleException("Cannot save a negative or zero value");
        var user = userDetails.getUser();

        BigDecimal mothSaving = new BigDecimal(monthlySaving);
        BigDecimal emergencyTotal = getIdealReserve(user);
        String resultInMoth = emergencyTotal.divide(mothSaving, 0, RoundingMode.HALF_UP).toString();
        LocalDate estimatedCompletionDate = LocalDate.now().plusMonths(Long.parseLong(resultInMoth));
        return new ResponseEmergencyReserveProgressDTO(mothSaving, resultInMoth, estimatedCompletionDate);
    }


    private BigDecimal getIdealReserve(User user){
        BigDecimal amount = expenseRepository.findAllByUser(user).stream().map(Expense::getAmount)
                .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);

        return amount.multiply(new BigDecimal(6));
    }
    private BigDecimal getExpenseEssential(User user){
        return expenseRepository.findAllByUser(user).stream()
                .filter(essential -> essential.getExpenseCategory().getSpendingPriority().equals(SpendingPriority.ESSENTIAL))
                .map(Expense::getAmount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    }
    private BigDecimal getAllIncomes(User user){
        return incomeRepository.findAllByUser(user).stream().map(Income::getAmount)
                .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    }

}
