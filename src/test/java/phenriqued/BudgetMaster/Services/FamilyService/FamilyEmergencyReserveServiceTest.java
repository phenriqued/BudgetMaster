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
import phenriqued.BudgetMaster.DTOs.Login.RegisterUserDTO;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Models.FamilyEntity.Family;
import phenriqued.BudgetMaster.Models.FamilyEntity.RoleFamily;
import phenriqued.BudgetMaster.Models.FamilyEntity.UserFamily;
import phenriqued.BudgetMaster.Models.UserEntity.Role.Role;
import phenriqued.BudgetMaster.Models.UserEntity.Role.RoleName;
import phenriqued.BudgetMaster.Models.UserEntity.User;
import phenriqued.BudgetMaster.Services.EmergencyReserveService.ReserveService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FamilyEmergencyReserveServiceTest {

    @InjectMocks
    private FamilyEmergencyReserveService familyEmergencyReserveService;
    @Mock
    private ReserveService reserveService;
    @Mock
    private FamilyConfigService familyConfigService;

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
    @DisplayName("should calculate the emergency reserve of the owner and family member")
    void getTotalReserveFamilySuccess() {
        when(familyConfigService.validateFamilyAccess(1L, userTest)).thenReturn(familyTest);
        when(reserveService.getIdealReserve(userTest)).thenReturn(new BigDecimal("2000"));
        when(reserveService.getIdealReserve(userTestII)).thenReturn(new BigDecimal("2500"));

        var result = familyEmergencyReserveService.getTotalReserveFamily(1L, new UserDetailsImpl(userTest));

        verify(reserveService, times(2)).getIdealReserve(any(User.class));
        assertEquals(new BigDecimal("4500"), result.idealReserve());
        assertEquals("BRL", result.currency());
        assertEquals(LocalDate.now(), result.calculateAt());
    }
    @Test
    @DisplayName("should Return Zero When User No Has Expenses For Calculate Emergency Reserve")
    void shouldReturnZeroWhenUserNoHasExpensesForCalculateEmergencyReserve() {
        when(familyConfigService.validateFamilyAccess(1L, userTest)).thenReturn(familyTest);
        when(reserveService.getIdealReserve(userTest)).thenReturn(BigDecimal.ZERO);
        when(reserveService.getIdealReserve(userTestII)).thenReturn(BigDecimal.ZERO);

        var result = familyEmergencyReserveService.getTotalReserveFamily(1L, new UserDetailsImpl(userTest));

        verify(reserveService, times(2)).getIdealReserve(any(User.class));
        assertEquals(BigDecimal.ZERO, result.idealReserve());
        assertEquals("BRL", result.currency());
        assertEquals(LocalDate.now(), result.calculateAt());
    }


}