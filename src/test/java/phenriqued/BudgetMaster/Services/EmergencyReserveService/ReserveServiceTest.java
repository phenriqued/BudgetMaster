package phenriqued.BudgetMaster.Services.EmergencyReserveService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import phenriqued.BudgetMaster.DTOs.Expense.RequestCreateExpenseDTO;
import phenriqued.BudgetMaster.DTOs.Income.RequestNewIncome;
import phenriqued.BudgetMaster.DTOs.Login.RegisterUserDTO;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BusinessRuleException;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Models.ExpenseEntity.Category.ExpenseCategory;
import phenriqued.BudgetMaster.Models.ExpenseEntity.Category.SpendingPriority;
import phenriqued.BudgetMaster.Models.ExpenseEntity.Expense;
import phenriqued.BudgetMaster.Models.IncomeEntity.Income;
import phenriqued.BudgetMaster.Models.UserEntity.Role.Role;
import phenriqued.BudgetMaster.Models.UserEntity.Role.RoleName;
import phenriqued.BudgetMaster.Models.UserEntity.User;
import phenriqued.BudgetMaster.Repositories.ExpenseRepository.ExpenseRepository;
import phenriqued.BudgetMaster.Repositories.IncomeRepository.IncomeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReserveServiceTest {

    @Mock
    private IncomeRepository incomeRepository;
    @Mock
    private ExpenseRepository expenseRepository;
    @InjectMocks
    private ReserveService reserveService;
    User user = new User(new RegisterUserDTO("teste", "teste@email.com", "Teste123"), "Teste123", new Role(1L, RoleName.USER));
    Income income = new Income(new RequestNewIncome("salary", "1500"), user);
    Expense expense = new Expense(new RequestCreateExpenseDTO("count", "500", 1L),
            new ExpenseCategory("teste", SpendingPriority.ESSENTIAL, null), user);


    @Test
    @DisplayName("should return the emergency reserve value to 6 months")
    void getTotalEmergencyReserve() {
        when(expenseRepository.findAllByUser(user)).thenReturn(List.of(expense));
        var resultDTO = reserveService.getTotalEmergencyReserve(new UserDetailsImpl(user));

        BigDecimal sixMothReserve = expense.getAmount().multiply(new BigDecimal(6));
        assertEquals(sixMothReserve, resultDTO.idealReserve());
        assertEquals(LocalDate.now(), resultDTO.calculateAt());
    }
    @Test
    @DisplayName("should return an exception when there is no expense registered")
    void shouldNotReturnTotalEmergencyReserve() {
        when(expenseRepository.findAllByUser(user)).thenReturn(List.of());

        Exception exception = assertThrows(BusinessRuleException.class,
                () -> reserveService.getTotalEmergencyReserve(new UserDetailsImpl(user)));

        assertEquals("No expenses and/or income registered", exception.getMessage());
    }

    @Test
    @DisplayName("should return the emergency reserve progress when there are registered income and expenses")
    void getEmergencyReserveProgress() {
        when(expenseRepository.findAllByUser(user)).thenReturn(List.of(expense));
        when(incomeRepository.findAllByUser(user)).thenReturn(List.of(income));
        var resultDTO = reserveService.getEmergencyReserveProgress(new UserDetailsImpl(user));

        assertEquals("3", resultDTO.estimatedMonths());
        assertEquals(new BigDecimal(1000), resultDTO.monthlySaving());
        assertEquals(LocalDate.now().plusMonths(3L), resultDTO.estimatedCompletionDate());
    }
    @Test
    @DisplayName("should not progress when there are no registered incomes or expenses")
    void shouldNotReturnEmergencyReserveProgress() {
        when(expenseRepository.findAllByUser(user)).thenReturn(List.of());
        when(incomeRepository.findAllByUser(user)).thenReturn(List.of());

        Exception exception = assertThrows(BusinessRuleException.class,
                () -> reserveService.getEmergencyReserveProgress(new UserDetailsImpl(user)));
        assertEquals("No expenses and/or income registered", exception.getMessage());
    }

    @Test
    @DisplayName("should return progress to emergency fund when there is a savings saved by the user")
    void getSimulationEmergencyReserveProgress() {
        when(expenseRepository.findAllByUser(user)).thenReturn(List.of(expense));
        var resultDTO = reserveService.getSimulationEmergencyReserveProgress(250, new UserDetailsImpl(user));

        assertEquals("12", resultDTO.estimatedMonths());
        assertEquals(new BigDecimal(250), resultDTO.monthlySaving());
        assertEquals(LocalDate.now().plusMonths(12L), resultDTO.estimatedCompletionDate());
    }
    @Test
    @DisplayName("should not return progress to emergency fund when user saved savings is negative")
    void shouldNotSimulationEmergencyReserveProgressWhenMonthlySavingIsNegative() {
        Exception exception = assertThrows(BusinessRuleException.class, () ->
                reserveService.getSimulationEmergencyReserveProgress(-250, new UserDetailsImpl(user)));
        assertEquals("Cannot save a negative or zero value", exception.getMessage());
    }
    @Test
    @DisplayName("should not return progress to emergency fund when there are no expenses")
    void shouldNotSimulationEmergencyReserveProgressWhenThereAreNoExpenses() {
        when(expenseRepository.findAllByUser(user)).thenReturn(List.of());
        Exception exception = assertThrows(BusinessRuleException.class, () ->
                reserveService.getSimulationEmergencyReserveProgress(250, new UserDetailsImpl(user)));
        assertEquals("No expenses and/or income registered", exception.getMessage());
    }
}