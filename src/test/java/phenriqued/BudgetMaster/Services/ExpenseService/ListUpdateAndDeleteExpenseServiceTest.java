package phenriqued.BudgetMaster.Services.ExpenseService;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import phenriqued.BudgetMaster.DTOs.Expense.RequestCreateExpenseDTO;
import phenriqued.BudgetMaster.DTOs.Expense.RequestUpdateExpenseDTO;
import phenriqued.BudgetMaster.DTOs.Login.RegisterUserDTO;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Models.ExpenseEntity.Category.ExpenseCategory;
import phenriqued.BudgetMaster.Models.ExpenseEntity.Category.SpendingPriority;
import phenriqued.BudgetMaster.Models.ExpenseEntity.Expense;
import phenriqued.BudgetMaster.Models.UserEntity.Role.Role;
import phenriqued.BudgetMaster.Models.UserEntity.Role.RoleName;
import phenriqued.BudgetMaster.Models.UserEntity.User;
import phenriqued.BudgetMaster.Repositories.ExpenseRepository.ExpenseRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListUpdateAndDeleteExpenseServiceTest {

    @Mock
    private ExpenseCategoryService categoryService;
    @Mock
    private ExpenseRepository expenseRepository;
    @InjectMocks
    private ExpenseService expenseService;
    private User user = new User(new RegisterUserDTO("teste", "teste@email.com", "Teste123!"), "Teste123!", new Role(1L, RoleName.USER));

    @Test
    @DisplayName("should list all expenses and total expenses")
    void getAllExpense(){
        var category = new ExpenseCategory("teste", SpendingPriority.ESSENTIAL, null);
        var expenseOne = new Expense(new RequestCreateExpenseDTO("spend","100", 1L), category, user);
        var expenseTwo = new Expense(new RequestCreateExpenseDTO("spend two","100", 1L), category, user);
        var listExpense = List.of(expenseOne, expenseTwo);
        PageRequest pageable = PageRequest.of(0, 10);

        when(expenseRepository.findAllByUser(pageable, user)).thenReturn(new PageImpl<>(listExpense, pageable, listExpense.size()));
        var resultDTO = expenseService.getAllExpense(pageable, new UserDetailsImpl(user));

        assertEquals(2, resultDTO.expenses().size());
        assertEquals(new BigDecimal(200), resultDTO.total());
    }

    @Test
    @DisplayName("should be to update an expense, when all data is correct.")
    void updateExpense() {
        var category = new ExpenseCategory("teste", SpendingPriority.ESSENTIAL, null);
        var expense = new Expense(new RequestCreateExpenseDTO("spend","100", 1L), category, user);

        user.setId(1L);
        var updateDTO = new RequestUpdateExpenseDTO("Salary", "300", 1L);
        when(expenseRepository.findById(any(Long.class))).thenReturn(Optional.of(expense));
        when(categoryService.findByIdAndUser(updateDTO.categoryId(), user.getId())).thenReturn(category);

        expenseService.updateExpense(1L,updateDTO, new UserDetailsImpl(user));

        verify(expenseRepository, times(1)).save(any(Expense.class));
        assertEquals("Salary", expense.getDescription());
        assertEquals(new BigDecimal(300), expense.getAmount());
    }
    @Test
    @DisplayName("should not update an expense when there is no expense corresponding to the ID or it does not belong to the user")
    void shouldNotUpdateExpenseWhenExpenseDontExistsOrNotBelongUser() {
        var updateDTO = new RequestUpdateExpenseDTO("Salary", "300", 1L);
        when(expenseRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> expenseService.updateExpense(1L,updateDTO, new UserDetailsImpl(user)));
        verify(expenseRepository, never()).save(any(Expense.class));
        assertEquals("Expense not found!", exception.getMessage());
    }
    @Test
    @DisplayName("should not update an expense when there is no category corresponding to the ID or it does not belong to the user")
    void shouldNotUpdateExpenseWhenCategoryDontExistsOrNotBelongUser() {
        var category = new ExpenseCategory("teste", SpendingPriority.ESSENTIAL, null);
        var expense = new Expense(new RequestCreateExpenseDTO("spend","100", 1L), category, user);

        user.setId(1L);
        var updateDTO = new RequestUpdateExpenseDTO("Salary", "300", 1L);
        when(expenseRepository.findById(any(Long.class))).thenReturn(Optional.of(expense));
        when(categoryService.findByIdAndUser(updateDTO.categoryId(), user.getId())).thenThrow(new EntityNotFoundException("Category not found!"));

        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> expenseService.updateExpense(1L,updateDTO, new UserDetailsImpl(user)));
        verify(expenseRepository, never()).save(any(Expense.class));
        assertEquals("Category not found!", exception.getMessage());
    }

    @Test
    @DisplayName("should delete an expense when the expense corresponds to the user")
    void deleteExpense() {
        var category = new ExpenseCategory("teste", SpendingPriority.ESSENTIAL, null);
        var expense = new Expense(new RequestCreateExpenseDTO("spend","100", 1L), category, user);
        user.setId(1L);
        expense.setId(1L);

        when(expenseRepository.findById(any(Long.class))).thenReturn(Optional.of(expense));

        expenseService.deleteExpense(1L, new UserDetailsImpl(user));

        verify(expenseRepository, times(1)).deleteById(eq(1L));
    }
    @Test
    @DisplayName("should not delete an expense when there is no expense corresponding to the ID or it does not belong to the user")
    void shouldNotDeleteExpenseWhenDontExistsOrNotBelongUser() {;
        when(expenseRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> expenseService.deleteExpense(1L, new UserDetailsImpl(user)));
        verify(expenseRepository, never()).deleteById(any());
        assertEquals("Expense not found!", exception.getMessage());
    }

}