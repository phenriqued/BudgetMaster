package phenriqued.BudgetMaster.Services.IncomeService;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import phenriqued.BudgetMaster.DTOs.Income.RequestNewIncome;
import phenriqued.BudgetMaster.DTOs.Income.RequestUpdateIncome;
import phenriqued.BudgetMaster.DTOs.Login.RegisterUserDTO;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BusinessRuleException;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Models.IncomeEntity.Income;
import phenriqued.BudgetMaster.Models.UserEntity.Role.Role;
import phenriqued.BudgetMaster.Models.UserEntity.Role.RoleName;
import phenriqued.BudgetMaster.Models.UserEntity.User;
import phenriqued.BudgetMaster.Repositories.IncomeRepository.IncomeRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncomeServiceTest {

    @Mock
    private IncomeRepository incomeRepository;
    @InjectMocks
    private IncomeService incomeService;

    @Test
    @DisplayName("should be to create an income, when there is no repeated data.")
    void createIncome() {
        var user = new User(new RegisterUserDTO("teste", "teste@email.com", "Teste123"), "Teste123", new Role(1L, RoleName.USER));

        when(incomeRepository.existsByDescriptionAndUser("salario", user)).thenReturn(false);
        incomeService.createIncome(new RequestNewIncome("salario", "2000.00"), new UserDetailsImpl(user));

        verify(incomeRepository, times(1)).save(any());
    }
    @Test
    @DisplayName("should be to create an income, when there is no repeated data.")
    void shouldNotCreateIncome() {
        var user = new User(new RegisterUserDTO("teste", "teste@email.com", "Teste123"), "Teste123", new Role(1L, RoleName.USER));

        when(incomeRepository.existsByDescriptionAndUser("salario", user)).thenReturn(true);

        Exception exception = assertThrows(BusinessRuleException.class, () ->
                incomeService.createIncome(new RequestNewIncome("salario", "2000.00"), new UserDetailsImpl(user)));
        assertEquals("there is already an income with that description!", exception.getMessage());
        verify(incomeRepository, never()).save(any());
    }

    @Test
    @DisplayName("should be to list all incomes with total amount")
    void listAllIncomes() {
        var user = new User(new RegisterUserDTO("teste", "teste@email.com", "Teste123"), "Teste123", new Role(1L, RoleName.USER));
        var incomeOne = new Income(new RequestNewIncome("salary", "2000"), user);
        var incomeTwo = new Income(new RequestNewIncome("investments", "1000"), user);
        var listIncomes = List.of(incomeOne, incomeTwo);
        PageRequest pageable = PageRequest.of(0, 10);

        when(incomeRepository.findAllByUser(pageable, user)).thenReturn(new PageImpl<>(listIncomes, pageable, listIncomes.size()));
        var resultDTO = incomeService.listAllIncomes(pageable, new UserDetailsImpl(user));

        assertEquals(new BigDecimal("3000"), resultDTO.total());
        assertEquals(2, resultDTO.incomes().size());
    }

    @Test
    @DisplayName("should update income data according to the data entered in the DTO.")
    void updateIncome() {
        var user = new User(new RegisterUserDTO("teste", "teste@email.com", "Teste123"), "Teste123", new Role(1L, RoleName.USER));
        var income = new Income(new RequestNewIncome("salary", "2.000"), user);

        when(incomeRepository.findById(any(Long.class))).thenReturn(Optional.of(income));
        user.setId(1L);
        incomeService.updateIncome(1L, new RequestUpdateIncome("investimento em renda variavel", "500"), new UserDetailsImpl(user));

        assertEquals("investimento em renda variavel", income.getDescription());
        assertEquals(new BigDecimal("500"), income.getAmount());
        verify(incomeRepository, times(1)).save(any(Income.class));
    }
    @Test
    @DisplayName("should not update when the amount is incorrect")
    void shouldNotUpdateIncome() {
        var user = new User(new RegisterUserDTO("teste", "teste@email.com", "Teste123"), "Teste123", new Role(1L, RoleName.USER));
        var income = new Income(new RequestNewIncome("salary", "2000"), user);

        Exception exception = assertThrows(BusinessRuleException.class,
                () -> incomeService.updateIncome(1L, new RequestUpdateIncome("investimento errado", "MontanteErrado"), new UserDetailsImpl(user)));

        assertNotEquals("investimento em renda variavel", income.getDescription());
        assertEquals("the value must correspond to one or more positive numeric digits followed by a period and finally up to two numeric digits",
                exception.getMessage());
        verify(incomeRepository, never()).save(any(Income.class));
    }
    @Test
    @DisplayName("should not update when the amount is negative")
    void shouldNotUpdateIncomeWhenAmountIsNegative() {
        var user = new User(new RegisterUserDTO("teste", "teste@email.com", "Teste123"), "Teste123", new Role(1L, RoleName.USER));
        var income = new Income(new RequestNewIncome("salary", "2000"), user);

        Exception exception = assertThrows(BusinessRuleException.class,
                () -> incomeService.updateIncome(1L, new RequestUpdateIncome("tigrinho", "-1000"), new UserDetailsImpl(user)));
        assertEquals("the value must correspond to one or more positive numeric digits followed by a period and finally up to two numeric digits",
                exception.getMessage());
        assertNotEquals("tigrinho", income.getDescription());
        assertNotEquals(new BigDecimal("-100"), income.getAmount());
        verify(incomeRepository, never()).save(any(Income.class));
    }

    @Test
    @DisplayName("should delete an income")
    void deleteIncome() {
        var user = new User(new RegisterUserDTO("teste", "teste@email.com", "Teste123"), "Teste123", new Role(1L, RoleName.USER));
        var income = new Income(new RequestNewIncome("salary", "2000"), user);

        when(incomeRepository.findById(any(Long.class))).thenReturn(Optional.of(income));
        user.setId(1L);
        incomeService.deleteIncome(1L, new UserDetailsImpl(user));

        verify(incomeRepository, times(1)).deleteById(any(Long.class));
    }
    @Test
    @DisplayName("should not delete an income that does not exist")
    void shouldNotDeleteIncome() {
        var user = new User(new RegisterUserDTO("teste", "teste@email.com", "Teste123"), "Teste123", new Role(1L, RoleName.USER));

        when(incomeRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        Exception exception = assertThrows(BusinessRuleException.class,
                () -> incomeService.deleteIncome(1L, new UserDetailsImpl(user)));
        assertEquals("income not found. Check income id", exception.getMessage());
        verify(incomeRepository, never()).deleteById(any(Long.class));
    }
    @Test
    @DisplayName("you should not delete an income that does not belong to you")
    void shouldNotDeleteIncomeDoesNotBelongYou() {
        var user = new User(new RegisterUserDTO("teste", "teste@email.com", "Teste123"), "Teste123", new Role(1L, RoleName.USER));
        var userTwo = new User(new RegisterUserDTO("Joao", "JoaoTeste@email.com", "Teste123"), "Teste123", new Role(1L, RoleName.USER));
        var income = new Income(new RequestNewIncome("salary", "2000"), user);

        when(incomeRepository.findById(any(Long.class))).thenReturn(Optional.of(income));
        user.setId(1L);
        userTwo.setId(2L);

        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> incomeService.deleteIncome(1L, new UserDetailsImpl(userTwo)));
        assertEquals("Income not found", exception.getMessage());
        verify(incomeRepository, never()).deleteById(any(Long.class));
    }

}