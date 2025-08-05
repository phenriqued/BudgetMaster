package phenriqued.BudgetMaster.Services.ExpenseService;

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
        var total = expensesByUser.stream().map(Expense::getAmount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        return new ResponseAllExpenseDTO(expensesDTO, total);
    }

}
