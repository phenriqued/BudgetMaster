package phenriqued.BudgetMaster.Services.ExpenseService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import phenriqued.BudgetMaster.DTOs.Expense.RequestCreateExpenseDTO;
import phenriqued.BudgetMaster.DTOs.Login.RegisterUserDTO;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BusinessRuleException;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Models.ExpenseEntity.Category.ExpenseCategory;
import phenriqued.BudgetMaster.Models.ExpenseEntity.Category.SpendingPriority;
import phenriqued.BudgetMaster.Models.ExpenseEntity.Expense;
import phenriqued.BudgetMaster.Models.UserEntity.Role.Role;
import phenriqued.BudgetMaster.Models.UserEntity.Role.RoleName;
import phenriqued.BudgetMaster.Models.UserEntity.User;
import phenriqued.BudgetMaster.Repositories.ExpenseRepository.ExpenseRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateExpenseServiceTest {

    @Mock
    private ExpenseCategoryService categoryService;
    @Mock
    private ExpenseRepository expenseRepository;
    @InjectMocks
    private ExpenseService expenseService;
    private User user = new User(new RegisterUserDTO("teste", "teste@email.com", "Teste123!"), "Teste123!", new Role(1L, RoleName.USER));

    private Validator validator;

    @BeforeEach
    void setup(){
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("should be to create an expense, when there is no repeated description.")
    void createExpense() {
        var expenseCategory = new ExpenseCategory("teste", SpendingPriority.ESSENTIAL, null);
        var newExpense = new RequestCreateExpenseDTO("spent", "100", 1L);

        when(categoryService.findByIdAndUser(newExpense.categoryId(), user.getId())).thenReturn(expenseCategory);
        when(expenseRepository.existsByDescriptionIgnoreCaseAndUser(newExpense.description(), user)).thenReturn(false);
        when(expenseRepository.save(any(Expense.class))).thenReturn(new Expense(newExpense, expenseCategory, user));
        var expense = expenseService.createExpense(newExpense, new UserDetailsImpl(user));

        verify(expenseRepository, times(1)).save(any(Expense.class));
        assertEquals(expense.getUser().getName(), user.getName());
        assertEquals(expense.getDescription(), newExpense.description());
        assertEquals(expense.getExpenseCategory().getName(), expenseCategory.getName());
    }
    @Test
    @DisplayName("should not create an expense when the category does not exist")
    void shouldNotCreateExpenseWhenCategoryNotExists() {
        var newExpense = new RequestCreateExpenseDTO("Not exists", "100", 999L);

        when(categoryService.findByIdAndUser(newExpense.categoryId(), user.getId()))
                .thenThrow(new EntityNotFoundException("Category not found"));

        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> expenseService.createExpense(newExpense, new UserDetailsImpl(user)));
        assertEquals("Category not found", exception.getMessage());
    }
    @Test
    @DisplayName("should not create an expense when a description already exists")
    void shouldNotCreateExpenseWhenDescriptionAlreadyExists() {
        var expenseCategory = new ExpenseCategory("teste", SpendingPriority.ESSENTIAL, null);
        var newExpense = new RequestCreateExpenseDTO("Not exists", "100", 1L);

        when(categoryService.findByIdAndUser(newExpense.categoryId(), user.getId())).thenReturn(expenseCategory);
        when(expenseRepository.existsByDescriptionIgnoreCaseAndUser(newExpense.description(), user)).thenReturn(true);

        Exception exception = assertThrows(BusinessRuleException.class,
                () -> expenseService.createExpense(newExpense, new UserDetailsImpl(user)));
        assertEquals("Unable to create an expense with the same description. Check or update the description.", exception.getMessage());
    }
    @Test
    @DisplayName("should not create an expense when amount is incorrect")
    void shouldNotCreateExpenseWhenAmountDataIsIncorrect() {
        var newExpenseDTO = new RequestCreateExpenseDTO("spent", "-120", 1L);
        var violations = validator.validate(newExpenseDTO);

        assertFalse(violations.isEmpty());
        assertEquals("the value must correspond to one or more positive numeric digits followed by a period and finally up to two numeric digits",
                violations.iterator().next().getMessage());
    }
}