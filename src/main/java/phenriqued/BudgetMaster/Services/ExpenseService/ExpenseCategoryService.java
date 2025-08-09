package phenriqued.BudgetMaster.Services.ExpenseService;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import phenriqued.BudgetMaster.DTOs.Expense.RequestCreateExpenseCategoryDTO;
import phenriqued.BudgetMaster.DTOs.Expense.RequestUpdateExpenseCategoryDTO;
import phenriqued.BudgetMaster.DTOs.Expense.ResponseCategoryDTO;
import phenriqued.BudgetMaster.DTOs.Expense.ResponseSpendingPriorityDTO;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BusinessRuleException;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Models.ExpenseEntity.Category.ExpenseCategory;
import phenriqued.BudgetMaster.Models.ExpenseEntity.Category.SpendingPriority;
import phenriqued.BudgetMaster.Models.UserEntity.User;
import phenriqued.BudgetMaster.Repositories.ExpenseRepository.ExpenseCategoryRepository;

import java.util.Arrays;
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
    public ExpenseCategory findByName(String name, User user) {
        return categoryRepository.findByNameAndUserOrPublic(name, user)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
    }
    public List<ResponseSpendingPriorityDTO> listSpendingPriority() {
        return Arrays.stream(SpendingPriority.values()).map(ResponseSpendingPriorityDTO::new).toList();
    }

    @Transactional
    public ExpenseCategory createExpenseCategory(RequestCreateExpenseCategoryDTO createCategoryDTO, UserDetailsImpl userDetails) {
        var user = userDetails.getUser();
        var spendingPriority = SpendingPriority.fromId(createCategoryDTO.spendingPriorityId())
                .orElseThrow(() ->new BusinessRuleException("There is no id corresponding to the spending priority"));

        var newCategory = new ExpenseCategory(createCategoryDTO.name(), spendingPriority, user);

        var existCategory = categoryRepository.existsByNameAndUser(newCategory.getName(), user);

        if(existCategory)
            throw new BusinessRuleException("Unable to create category, the category name already exists.");

        return categoryRepository.save(newCategory);
    }
    @Transactional
    public void updateCategory(Long id, RequestUpdateExpenseCategoryDTO updateCategoryDTO, UserDetailsImpl userDetails) {
        var user = userDetails.getUser();
        var category = findByIdAndUser(id, user.getId());
        category.setName(updateCategoryDTO.name());
        category.setSpendingPriority(updateCategoryDTO.spendingPriorityId());
        categoryRepository.save(category);
    }
    @Transactional
    public void deleteCategory(Long id, UserDetailsImpl userDetails) {
        var category = categoryRepository.findByIdAndUserNotNull(id)
                .filter(cat -> cat.getUser().getId().equals(userDetails.getUser().getId()))
                .orElseThrow(() -> new EntityNotFoundException("category not found or unable to delete a global category."));

        categoryRepository.deleteById(category.getId());
    }
}
