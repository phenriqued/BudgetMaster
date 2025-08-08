package phenriqued.BudgetMaster.Services.ExpenseService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import phenriqued.BudgetMaster.DTOs.Expense.RequestCreateExpenseDTO;
import phenriqued.BudgetMaster.DTOs.Expense.RequestUpdateExpenseDTO;
import phenriqued.BudgetMaster.DTOs.Expense.ResponseAllExpenseDTO;
import phenriqued.BudgetMaster.DTOs.Expense.ResponseExpenseDTO;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BusinessRuleException;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Models.ExpenseEntity.Expense;
import phenriqued.BudgetMaster.Repositories.ExpenseRepository.ExpenseRepository;

import java.math.BigDecimal;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseCategoryService categoryService;


    public ExpenseService(ExpenseRepository expenseRepository, ExpenseCategoryService categoryService) {
        this.expenseRepository = expenseRepository;
        this.categoryService = categoryService;
    }
    @Transactional
    public Expense createExpense(@Valid RequestCreateExpenseDTO createExpense, UserDetailsImpl userDetails){
        var user = userDetails.getUser();
        var category = categoryService.findByIdAndUser(createExpense.categoryId(), user.getId());
        if(expenseRepository.existsByDescriptionIgnoreCaseAndUser(createExpense.description(), user))
            throw new BusinessRuleException("Unable to create an expense with the same description. Check or update the description.");

        return expenseRepository.save(new Expense(createExpense, category, user));
    }

    public ResponseAllExpenseDTO getAllExpense(Pageable pageable, UserDetailsImpl userDetails){
        var user = userDetails.getUser();
        var expensesByUser = expenseRepository.findAllByUser(pageable, user);
        var expensesDTO = expensesByUser.stream().map(ResponseExpenseDTO::new).toList();
        var total = calculateTotal(expensesByUser);
        return new ResponseAllExpenseDTO(expensesDTO, total);
    }
    public ResponseAllExpenseDTO getAllExpenseByCategory(Pageable pageable, String category, UserDetailsImpl userDetails) {
        var user = userDetails.getUser();
        var expenseCategory = categoryService.findByName(category, user);
        var expensesByUserAndCategory = expenseRepository.findAllByUserAndExpenseCategory(pageable, user, expenseCategory);
        var total = calculateTotal(expensesByUserAndCategory);
        return new ResponseAllExpenseDTO(expensesByUserAndCategory.stream().map(ResponseExpenseDTO::new).toList(), total);
    }
    public ResponseExpenseDTO getExpenseById(Long id, UserDetailsImpl userDetails){
        return expenseRepository.findById(id)
                .filter(expense -> expense.getUser().getId().equals(userDetails.getUser().getId()))
                .map(ResponseExpenseDTO::new)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found!"));
    }
    @Transactional
    public void updateExpense(Long id, RequestUpdateExpenseDTO updateExpenseDTO, UserDetailsImpl userDetails) {
        var user = userDetails.getUser();
        var expense = expenseRepository.findById(id)
                .filter(expenseEntity -> expenseEntity.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new EntityNotFoundException("Expense not found!"));

        if(updateExpenseDTO.categoryId() != null)
            expense.setExpenseCategory(categoryService.findByIdAndUser(updateExpenseDTO.categoryId(), user.getId()));

        expense.update(updateExpenseDTO);
        expenseRepository.save(expense);
    }
    @Transactional
    public void deleteExpense(Long id, UserDetailsImpl userDetails) {
        var user = userDetails.getUser();
        var expense = expenseRepository.findById(id)
                .filter(expenseEntity -> expenseEntity.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new EntityNotFoundException("Expense not found!"));
        expenseRepository.deleteById(expense.getId());
    }

    private BigDecimal calculateTotal(Page<Expense> expenses){
        return expenses.stream().map(Expense::getAmount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    }


}
