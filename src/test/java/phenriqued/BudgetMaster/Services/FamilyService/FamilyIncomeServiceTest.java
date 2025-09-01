package phenriqued.BudgetMaster.Services.FamilyService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import phenriqued.BudgetMaster.DTOs.Family.RequestCreateFamilyDTO;
import phenriqued.BudgetMaster.DTOs.Income.RequestNewIncome;
import phenriqued.BudgetMaster.DTOs.Login.RegisterUserDTO;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Models.FamilyEntity.Family;
import phenriqued.BudgetMaster.Models.FamilyEntity.RoleFamily;
import phenriqued.BudgetMaster.Models.FamilyEntity.UserFamily;
import phenriqued.BudgetMaster.Models.IncomeEntity.Income;
import phenriqued.BudgetMaster.Models.UserEntity.Role.Role;
import phenriqued.BudgetMaster.Models.UserEntity.Role.RoleName;
import phenriqued.BudgetMaster.Models.UserEntity.User;
import phenriqued.BudgetMaster.Repositories.IncomeRepository.IncomeRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FamilyIncomeServiceTest {

    @InjectMocks
    private FamilyIncomeService familyIncomeService;
    @Mock
    private FamilyConfigService familyConfigService;
    @Mock
    private IncomeRepository incomeRepository;

    private Family familyTest;
    private User userTest = new User(new RegisterUserDTO("teste", "teste@email.com", "Teste123!"), "Teste123!", new Role(1L, RoleName.USER));
    private User userTestII = new User(new RegisterUserDTO("testeII", "testeII@email.com", "Teste123!"), "Teste123!", new Role(1L, RoleName.USER));
    private User userTestIII = new User(new RegisterUserDTO("testeIII", "testeIII@email.com", "Teste123!"), "Teste123!", new Role(1L, RoleName.USER));
    UserFamily userOwner;
    UserFamily userMember;
    UserFamily userViewer;

    @BeforeEach
    void setup(){
        familyTest = new Family(new RequestCreateFamilyDTO("Family Test", List.of()));
        userOwner = new UserFamily(userTest, familyTest, RoleFamily.OWNER);
        userMember = new UserFamily(userTestII, familyTest, RoleFamily.MEMBER);
        userViewer = new UserFamily(userTestIII, familyTest, RoleFamily.VIEWER);

        familyTest.addUserFamily(userOwner);
        familyTest.addUserFamily(userMember);
        familyTest.addUserFamily(userViewer);

        ReflectionTestUtils.setField(familyTest, "id", 1L);

        ReflectionTestUtils.setField(userTest, "id", 1L);
        ReflectionTestUtils.setField(userTestII, "id", 2L);
        ReflectionTestUtils.setField(userTestIII, "id", 3L);

        ReflectionTestUtils.setField(userOwner, "id", 1L);
        ReflectionTestUtils.setField(userMember, "id", 2L);
        ReflectionTestUtils.setField(userViewer, "id", 3L);
    }

    @Test
    @DisplayName("should Calculate Total incomes For Owner And Member")
    void getAllIncomeFamilySuccess() {
        Income incomeOwner = new Income(new RequestNewIncome("Salary", "3000"), userTest);
        Income incomeMember = new Income(new RequestNewIncome("Salary", "3500"), userTestII);

        when(familyConfigService.validateFamilyAccess(1L, userTest)).thenReturn(familyTest);
        when(incomeRepository.findAllByUser(userTest)).thenReturn(List.of(incomeOwner));
        when(incomeRepository.findAllByUser(userTestII)).thenReturn(List.of(incomeMember));

        var result = familyIncomeService.getAllIncomeFamily(1L, new UserDetailsImpl(userTest));

        verify(incomeRepository, times(2)).findAllByUser(any(User.class));
        assertEquals(new BigDecimal("6500"), result.total());
        assertEquals(3, familyTest.getUserFamilies().size());
        assertEquals(2, result.memberList().size());
        assertTrue(result.memberList().stream().anyMatch(m -> m.name().equals("teste") && m.total().equals(new BigDecimal("3000"))));
        assertTrue(result.memberList().stream().anyMatch(m -> m.name().equals("testeII") && m.total().equals(new BigDecimal("3500"))));
    }
    @Test
    @DisplayName("should return ZERO when the users has no incomes")
    void shouldReturnZeroWhenUserNoHasIncomes() {
        when(familyConfigService.validateFamilyAccess(1L, userTest)).thenReturn(familyTest);
        when(incomeRepository.findAllByUser(userTest)).thenReturn(List.of());
        when(incomeRepository.findAllByUser(userTestII)).thenReturn(List.of());

        var result = familyIncomeService.getAllIncomeFamily(1L, new UserDetailsImpl(userTest));

        verify(incomeRepository, times(2)).findAllByUser(any(User.class));
        assertEquals(BigDecimal.ZERO, result.total());
        assertEquals(3, familyTest.getUserFamilies().size());
        assertEquals(2, result.memberList().size());
    }
}