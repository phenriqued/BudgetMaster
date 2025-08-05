package phenriqued.BudgetMaster.Services.ExpenseService;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import phenriqued.BudgetMaster.DTOs.Expense.RequestCreateExpenseCategoryDTO;
import phenriqued.BudgetMaster.DTOs.Expense.ResponseCategoryDTO;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BusinessRuleException;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Models.ExpenseEntity.Category.ExpenseCategory;
import phenriqued.BudgetMaster.Models.ExpenseEntity.Category.SpendingPriority;
import phenriqued.BudgetMaster.Repositories.ExpenseRepository.ExpenseCategoryRepository;

import java.util.List;

@Service
public class ExpenseCategoryService {

    private final ExpenseCategoryRepository categoryRepository;

    public ExpenseCategoryService(ExpenseCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public ExpenseCategory findByIdAndUser(Long id, Long userId){
        return categoryRepository.findById(id).
                filter(cat -> cat.getUser() == null || cat.getUser().getId().equals(userId))
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
    }

    public List<ResponseCategoryDTO> listAllCategoryByUser(UserDetailsImpl userDetails){
        return categoryRepository.findAllByUser(userDetails.getUser()).stream().map(ResponseCategoryDTO::new).toList();
    }


    public ExpenseCategory createExpenseCategory(RequestCreateExpenseCategoryDTO createCategoryDTO, UserDetailsImpl userDetails) {
        var user = userDetails.getUser();
        var newCategory = new ExpenseCategory(createCategoryDTO.name(), SpendingPriority.valueOf(createCategoryDTO.spendingPriority()), user);

        var existCategory = categoryRepository.findAllByUser(user).stream().
                filter(cat -> cat.getName().equalsIgnoreCase(newCategory.getName())).toList();

        if(! existCategory.isEmpty())
            throw new BusinessRuleException("Unable to create category, the category name already exists.");
        return categoryRepository.save(newCategory);
    }
}
