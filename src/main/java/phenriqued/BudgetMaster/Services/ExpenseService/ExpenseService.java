package phenriqued.BudgetMaster.Services.ExpenseService;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import phenriqued.BudgetMaster.DTOs.Expense.RequestCreateExpenseDTO;
import phenriqued.BudgetMaster.DTOs.Expense.ResponseAllExpenseDTO;
import phenriqued.BudgetMaster.DTOs.Expense.ResponseExpenseDTO;
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

    public Expense createExpense(RequestCreateExpenseDTO createExpense, UserDetailsImpl userDetails){
        var user = userDetails.getUser();
        var category = categoryService.findByIdAndUser(createExpense.categoryId(), user.getId());

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

    private BigDecimal calculateTotal(Page<Expense> expenses){
        return expenses.stream().map(Expense::getAmount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    }
}
