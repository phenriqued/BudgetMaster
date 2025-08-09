package phenriqued.BudgetMaster.Services.ExpenseService;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import phenriqued.BudgetMaster.DTOs.Expense.RequestCreateExpenseCategoryDTO;
import phenriqued.BudgetMaster.DTOs.Expense.RequestUpdateExpenseCategoryDTO;
import phenriqued.BudgetMaster.DTOs.Login.RegisterUserDTO;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BusinessRuleException;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Models.ExpenseEntity.Category.ExpenseCategory;
import phenriqued.BudgetMaster.Models.ExpenseEntity.Category.SpendingPriority;
import phenriqued.BudgetMaster.Models.UserEntity.Role.Role;
import phenriqued.BudgetMaster.Models.UserEntity.Role.RoleName;
import phenriqued.BudgetMaster.Models.UserEntity.User;
import phenriqued.BudgetMaster.Repositories.ExpenseRepository.ExpenseCategoryRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseCategoryServiceTest {

    @Mock
    private ExpenseCategoryRepository categoryRepository;
    @InjectMocks
    private ExpenseCategoryService expenseCategoryService;
    private User user = new User(new RegisterUserDTO("teste", "teste@email.com", "Teste123!"), "Teste123!", new Role(1L, RoleName.USER));

    @Test
    @DisplayName("a new category should be created when the creation data is correct")
    void createExpenseCategory() {
        var createCategory = new RequestCreateExpenseCategoryDTO("New category", 1L);
        var category = new ExpenseCategory(createCategory.name(), SpendingPriority.ESSENTIAL, user);

        when(categoryRepository.existsByNameAndUser(createCategory.name(), user)).thenReturn(false);
        expenseCategoryService.createExpenseCategory(createCategory, new UserDetailsImpl(user));

        verify(categoryRepository, times(1)).save(eq(category));
    }
    @Test
    @DisplayName("a new category should not be created when there is a non-existent spending priority")
    void shouldNotCreateExpenseCategoryWhenDataIsIncorrect() {
        var createCategory = new RequestCreateExpenseCategoryDTO("New category", 9999L);
        var category = new ExpenseCategory(createCategory.name(), SpendingPriority.ESSENTIAL, user);

        Exception exception = assertThrows(BusinessRuleException.class,
                () -> expenseCategoryService.createExpenseCategory(createCategory, new UserDetailsImpl(user)));
        verify(categoryRepository, never()).save(eq(category));
        assertEquals("There is no id corresponding to the spending priority", exception.getMessage());
    }
    @Test
    @DisplayName("a new category should not be created when a category with the same name already exists")
    void shouldNotCreateExpenseCategoryWhenExistsCategoryWithSameName() {
        var createCategory = new RequestCreateExpenseCategoryDTO("New category", 1L);
        var category = new ExpenseCategory(createCategory.name(), SpendingPriority.ESSENTIAL, user);

        when(categoryRepository.existsByNameAndUser(createCategory.name(), user)).thenReturn(true);

        Exception exception = assertThrows(BusinessRuleException.class,
                () -> expenseCategoryService.createExpenseCategory(createCategory, new UserDetailsImpl(user)));
        verify(categoryRepository, never()).save(eq(category));
        assertEquals("Unable to create category, the category name already exists.", exception.getMessage());
    }

    @Test
    @DisplayName("a category should be updated when the data is correct")
    void updateCategory() {
        var createCategory = new RequestCreateExpenseCategoryDTO("New category", 1L);
        var category = new ExpenseCategory(createCategory.name(), SpendingPriority.ESSENTIAL, user);
        ReflectionTestUtils.setField(category, "id", 1L);
        ReflectionTestUtils.setField(user, "id", 1L);
        var updateCategory = new RequestUpdateExpenseCategoryDTO("Update Category", 2L);

        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        expenseCategoryService.updateCategory(category.getId(), updateCategory, new UserDetailsImpl(user));

        verify(categoryRepository, times(1)).save(eq(category));
        assertEquals("Update Category", updateCategory.name());
        assertEquals(SpendingPriority.NONESSENTIAL.id, updateCategory.spendingPriorityId());
    }
    @Test
    @DisplayName("should not update when the category does not exist or does not belong to the user")
    void shouldNotUpdateCategoryWhenCategoryNotExistsOrNotBelongUser() {
        ReflectionTestUtils.setField(user, "id", 1L);
        var updateCategory = new RequestUpdateExpenseCategoryDTO("Update Category", 2L);

        when(categoryRepository.findById(any(Long.class))).thenReturn(Optional.empty());
        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> expenseCategoryService.updateCategory(999L, updateCategory, new UserDetailsImpl(user)));
        verify(categoryRepository, never()).save(any());
        assertEquals("Category not found", exception.getMessage());
    }

    @Test
    @DisplayName("should delete when the category exists and belongs to the user")
    void deleteCategory() {
        var createCategory = new RequestCreateExpenseCategoryDTO("New category", 1L);
        var category = new ExpenseCategory(createCategory.name(), SpendingPriority.ESSENTIAL, user);
        ReflectionTestUtils.setField(category, "id", 1L);
        ReflectionTestUtils.setField(user, "id", 1L);

        when(categoryRepository.findByIdAndUserNotNull(category.getId())).thenReturn(Optional.of(category));
        expenseCategoryService.deleteCategory(category.getId(), new UserDetailsImpl(user));

        verify(categoryRepository, times(1)).deleteById(eq(category.getId()));
    }
    @Test
    @DisplayName("should not delete when the category does not exist or does not belong to the user")
    void shouldNotDeleteCategoryWhenCategoryNotExistsOrNotBelongUser() {
        ReflectionTestUtils.setField(user, "id", 1L);

        when(categoryRepository.findByIdAndUserNotNull(any(Long.class))).thenReturn(Optional.empty());
        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> expenseCategoryService.deleteCategory(999L, new UserDetailsImpl(user)));
        verify(categoryRepository, never()).deleteById(any(Long.class));
        assertEquals("category not found or unable to delete a global category.", exception.getMessage());
    }
}