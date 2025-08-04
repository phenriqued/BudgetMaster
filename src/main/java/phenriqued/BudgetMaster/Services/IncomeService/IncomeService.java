package phenriqued.BudgetMaster.Services.IncomeService;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import phenriqued.BudgetMaster.DTOs.Income.RequestNewIncome;
import phenriqued.BudgetMaster.DTOs.Income.RequestUpdateIncome;
import phenriqued.BudgetMaster.DTOs.Income.ResponseAllIncomesDTO;
import phenriqued.BudgetMaster.DTOs.Income.ResponseIncomesDTO;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BusinessRuleException;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Models.IncomeEntity.Income;
import phenriqued.BudgetMaster.Models.UserEntity.User;
import phenriqued.BudgetMaster.Repositories.IncomeRepository.IncomeRepository;

import java.math.BigDecimal;

@Service
public class IncomeService {

    private final IncomeRepository incomeRepository;

    public IncomeService(IncomeRepository incomeRepository) {
        this.incomeRepository = incomeRepository;
    }

    public Income createIncome(RequestNewIncome requestIncomeDTO, UserDetailsImpl userDetails) {
        var user = userDetails.getUser();
        if (incomeRepository.existsByDescriptionAndUser(requestIncomeDTO.description(), user))
            throw new BusinessRuleException("there is already an income with that description!");

        return incomeRepository.save(new Income(requestIncomeDTO, user));
    }

    public ResponseAllIncomesDTO listAllIncomes(Pageable pageable, UserDetailsImpl userDetails) {
        var income = incomeRepository.findAllByUser(pageable, userDetails.getUser());
        var incomesDTO = income.stream().map(ResponseIncomesDTO::new).toList();
        var total = income.stream().map(Income::getAmount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        return new ResponseAllIncomesDTO(incomesDTO, total);
    }

    public ResponseIncomesDTO getIncomeById(Long id, UserDetailsImpl userDetails) {
        var income = findById(id);
        checkUserAndIncome(income, userDetails.getUser());
        return new ResponseIncomesDTO(income);
    }
    public ResponseIncomesDTO getIncomeByDescription(String description, UserDetailsImpl userDetails) {
        var user = userDetails.getUser();
        var income = incomeRepository.findByDescriptionAndUser(description, user)
                .orElseThrow(() -> new BusinessRuleException("income not found!"));
        checkUserAndIncome(income, user);
        return new ResponseIncomesDTO(income);
    }

    public void updateIncome(Long id, RequestUpdateIncome requestIncomeDTO, UserDetailsImpl userDetails) {
        if (requestIncomeDTO.amount() != null && !requestIncomeDTO.amount().trim().isBlank()){
            if(!requestIncomeDTO.amount().matches("^\\d+(\\.\\d{1,2})?$"))
                throw new BusinessRuleException("the value must correspond to one or more positive numeric digits followed by a period and finally up to two numeric digits");
        }
        var user = userDetails.getUser();
        var income = findById(id);
        checkUserAndIncome(income, user);
        income.updateIncome(requestIncomeDTO);
        incomeRepository.save(income);
    }

    public void deleteIncome(Long id, UserDetailsImpl userDetails) {
        var income = findById(id);
        checkUserAndIncome(income, userDetails.getUser());
        incomeRepository.deleteById(id);
    }

    private void checkUserAndIncome(Income income, User user){
        if(! income.getUser().getId().equals(user.getId()))
            throw new EntityNotFoundException("Income not found");
    }
    private Income findById(Long id){
        return incomeRepository.findById(id).orElseThrow(() -> new BusinessRuleException("income not found. Check income id"));
    }

}
