package phenriqued.BudgetMaster.Services.FamilyService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import phenriqued.BudgetMaster.DTOs.Family.FamilyMemberDTO;
import phenriqued.BudgetMaster.DTOs.Family.RequestCreateFamilyDTO;
import phenriqued.BudgetMaster.DTOs.Login.RegisterUserDTO;
import phenriqued.BudgetMaster.Infra.Email.FamilyEmailService;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BusinessRuleException;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Models.FamilyEntity.Family;
import phenriqued.BudgetMaster.Models.FamilyEntity.RoleFamily;
import phenriqued.BudgetMaster.Models.FamilyEntity.UserFamily;
import phenriqued.BudgetMaster.Models.UserEntity.Role.Role;
import phenriqued.BudgetMaster.Models.UserEntity.Role.RoleName;
import phenriqued.BudgetMaster.Models.UserEntity.User;
import phenriqued.BudgetMaster.Repositories.FamilyRepositories.FamilyRepository;
import phenriqued.BudgetMaster.Repositories.FamilyRepositories.UserFamilyRepository;
import phenriqued.BudgetMaster.Services.UserServices.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateFamilyTest {

    @InjectMocks
    private FamilyService familyService;
    @Mock
    private FamilyRepository familyRepository;
    @Mock
    private UserFamilyRepository userFamilyRepository;
    @Mock
    private UserService userService;
    @Mock
    private FamilyEmailService familyEmailService;
    @Mock
    private User user = new User(new RegisterUserDTO("teste", "teste@email.com", "Teste123!"), "Teste123!", new Role(1L, RoleName.USER));
    private User userTest = new User(new RegisterUserDTO("teste I", "testeI@email.com", "Teste123!"), "Teste123!", new Role(1L, RoleName.USER));
    private RequestCreateFamilyDTO familyDto = new RequestCreateFamilyDTO("Family Test", List.of(new FamilyMemberDTO("testeI@email.com", 2L)));

    @Test
    @DisplayName("should create a Family when the creation data is correct.")
    void createFamilySuccess() {
        var userDetails = new UserDetailsImpl(user);

        var familyMock = new Family(familyDto);
        var userFamilyMock = new UserFamily(user, familyMock, RoleFamily.OWNER);

        when(familyRepository.save(any(Family.class))).thenReturn(familyMock);
        when(userFamilyRepository.save(any(UserFamily.class))).thenReturn(userFamilyMock);
        when(userService.findUserByEmail("testeI@email.com")).thenReturn(userTest);

        var response = familyService.createFamily(userDetails, familyDto);

        verify(familyRepository, times(1)).save(any(Family.class));
        verify(userFamilyRepository, times(1)).save(any(UserFamily.class));
        verify(familyEmailService, times(1)).invitedMember(eq(userTest), any(Family.class), eq(RoleFamily.MEMBER), eq(user));
        assertNotNull(response);
        assertEquals("Family Test", response.name());
        assertEquals(1, response.invitesNotSent().size());
        assertEquals("invitations were sent to all members!", response.invitesNotSent().get("Invitations sent successfully"));
    }

    @Test
    @DisplayName("should not create a family when the user already has 5 or more families created with Owner")
    void notCreateFamilyWhenUserHasMoreThanFiveFamilies() {
        var userDetails = new UserDetailsImpl(user);
        var familyMock = new Family(familyDto);
        var userFamilyMockI = new UserFamily(user, familyMock, RoleFamily.OWNER);
        var userFamilyMockII = new UserFamily(user, familyMock, RoleFamily.OWNER);
        var userFamilyMockIII = new UserFamily(user, familyMock, RoleFamily.OWNER);
        var userFamilyMockIV = new UserFamily(user, familyMock, RoleFamily.OWNER);
        var userFamilyMockV = new UserFamily(user, familyMock, RoleFamily.OWNER);


        when(user.getFamily()).thenReturn(List.of(userFamilyMockI, userFamilyMockII, userFamilyMockIII, userFamilyMockIV, userFamilyMockV));

        Exception exception = assertThrows(BusinessRuleException.class , () ->familyService.createFamily(userDetails, familyDto));
        assertEquals("The user has more than five \"Families\" created", exception.getMessage());
    }

    @Test
    @DisplayName("should not create a family when the user does not add at least one user")
    void notCreateFamilyWhenUserNotAddOneUser() {
        var userDetails = new UserDetailsImpl(user);
        var wrongFamilyCreateDTO = new RequestCreateFamilyDTO("Family Test", List.of());

        Exception exception = assertThrows(BusinessRuleException.class , () -> familyService.createFamily(userDetails, wrongFamilyCreateDTO));
        assertEquals("it is necessary to add at least one member to the family", exception.getMessage());
    }

    @Test
    @DisplayName("should not create a family when the user does not add at least one valid user")
    void notCreateFamilyWhenUserNotAddOneValidUser() {
        var userDetails = new UserDetailsImpl(user);
        var wrongFamilyCreateDTO = new RequestCreateFamilyDTO("Family Test", List.of(new FamilyMemberDTO("userDontExist@gmail.com", 2L)));

        when(userService.findUserByEmail(eq("userDontExist@gmail.com"))).thenThrow(UsernameNotFoundException.class);

        Exception exception = assertThrows(BusinessRuleException.class , () -> familyService.createFamily(userDetails, wrongFamilyCreateDTO));
        assertEquals("Unable to create a family because only invalid users were added", exception.getMessage());
    }

}