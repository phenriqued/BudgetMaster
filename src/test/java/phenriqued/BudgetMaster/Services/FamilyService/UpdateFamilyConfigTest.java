package phenriqued.BudgetMaster.Services.FamilyService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import phenriqued.BudgetMaster.DTOs.Family.FamilyMemberDTO;
import phenriqued.BudgetMaster.DTOs.Family.RequestCreateFamilyDTO;
import phenriqued.BudgetMaster.DTOs.Family.UpdateFamilyNameDTO;
import phenriqued.BudgetMaster.DTOs.Family.UpdateRoleIdFamilyDTO;
import phenriqued.BudgetMaster.DTOs.Login.RegisterUserDTO;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BudgetMasterSecurityException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateFamilyConfigTest {

    @InjectMocks
    private FamilyConfigService familyConfigService;
    @Mock
    private FamilyRepository familyRepository;
    @Mock
    private UserFamilyRepository userFamilyRepository;
    @Mock
    private UserService userService;

    private User user = new User(new RegisterUserDTO("teste", "teste@email.com", "Teste123!"), "Teste123!", new Role(1L, RoleName.USER));
    private User userMember = new User(new RegisterUserDTO("test Member", "testeII@email.com", "Teste123!"), "Teste123!", new Role(1L, RoleName.USER));
    private Family familyTest = new Family(new RequestCreateFamilyDTO("Family Test",
            List.of(new FamilyMemberDTO("teste@email.com", 1L), new FamilyMemberDTO("testeII@email.com", 3L))));

    @Test
    @DisplayName("should update the family name")
    void updateFamilyNameSuccess() {
        ReflectionTestUtils.setField(familyTest, "id", 1L);
        var userDetails = new UserDetailsImpl(user);
        var updateNameFamilyDTO = new UpdateFamilyNameDTO("New family name");

        when(familyRepository.findById(1L)).thenReturn(Optional.of(familyTest));
        when(userFamilyRepository.existsByUserAndFamily(eq(user), eq(familyTest))).thenReturn(true);
        when(userFamilyRepository.existsByFamilyAndUserAndRoleFamily(eq(familyTest), eq(user), eq(RoleFamily.OWNER))).thenReturn(true);

        familyConfigService.updateFamilyName(1L, updateNameFamilyDTO, userDetails);

        verify(familyRepository, times(1)).flush();
        assertEquals("New family name", familyTest.getName());
    }
    @Test
    @DisplayName("should not update the family name when it is not the owner user changing the name")
    void NotUpdateFamilyNameWhenNotOwnerUserChangingName() {
        ReflectionTestUtils.setField(familyTest, "id", 1L);
        var userDetails = new UserDetailsImpl(user);
        var updateNameFamilyDTO = new UpdateFamilyNameDTO("New family name");

        when(familyRepository.findById(1L)).thenReturn(Optional.of(familyTest));
        when(userFamilyRepository.existsByUserAndFamily(eq(user), eq(familyTest))).thenReturn(true);
        when(userFamilyRepository.existsByFamilyAndUserAndRoleFamily(eq(familyTest), eq(user), eq(RoleFamily.OWNER))).thenReturn(false);

        Exception exception = assertThrows(BudgetMasterSecurityException.class, () -> familyConfigService.updateFamilyName(1L, updateNameFamilyDTO, userDetails));

        verify(familyRepository, never()).flush();
        assertNotEquals("New family name", familyTest.getName());
        assertEquals("You do not have permission to perform the operation", exception.getMessage());
    }
    @Test
    @DisplayName("should not update the family name when it is not the owner user changing the name")
    void NotUpdateFamilyNameWhenDTONameIsNull() {
        ReflectionTestUtils.setField(familyTest, "id", 1L);
        var userDetails = new UserDetailsImpl(user);
        var updateNameFamilyDTO = new UpdateFamilyNameDTO("");

        when(familyRepository.findById(1L)).thenReturn(Optional.of(familyTest));
        when(userFamilyRepository.existsByUserAndFamily(eq(user), eq(familyTest))).thenReturn(true);
        when(userFamilyRepository.existsByFamilyAndUserAndRoleFamily(eq(familyTest), eq(user), eq(RoleFamily.OWNER))).thenReturn(true);

        Exception exception = assertThrows(NullPointerException.class, () -> familyConfigService.updateFamilyName(1L, updateNameFamilyDTO, userDetails));

        verify(familyRepository, never()).flush();
        assertNotEquals("New family name", familyTest.getName());
        assertEquals("Family Name cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("should update a user's Role when the owner makes the request")
    void updateFamilyRole() {
        ReflectionTestUtils.setField(familyTest, "id", 1L);
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(userMember, "id", 2L);
        var userDetails = new UserDetailsImpl(user);
        var updateRoleIdFamilyDTO = new UpdateRoleIdFamilyDTO(3L, 2L);
        var userFamilyUpdateRole = new UserFamily(userMember, familyTest, RoleFamily.MEMBER);

        when(userService.findUserById(eq(2L))).thenReturn(userMember);
        when(familyRepository.findById(eq(1L))).thenReturn(Optional.of(familyTest));
        when(userFamilyRepository.existsByUserAndFamily(eq(user), eq(familyTest))).thenReturn(true);
        when(userFamilyRepository.existsByFamilyAndUserAndRoleFamily(eq(familyTest), eq(user), eq(RoleFamily.OWNER))).thenReturn(true);
        when(userFamilyRepository.findByUserAndFamily(eq(userMember), eq(familyTest))).thenReturn(Optional.of(userFamilyUpdateRole));

        familyConfigService.updateFamilyRole(1L, updateRoleIdFamilyDTO, userDetails);

        verify(userFamilyRepository, times(1)).flush();
        assertNotEquals(RoleFamily.MEMBER, userFamilyUpdateRole.getRoleFamily());
        assertEquals(RoleFamily.VIEWER, userFamilyUpdateRole.getRoleFamily());
    }
    @Test
    @DisplayName("should update the family owner")
    void updateFamilyRoleOwner() {
        ReflectionTestUtils.setField(familyTest, "id", 1L);
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(userMember, "id", 2L);
        var userDetails = new UserDetailsImpl(user);
        var updateRoleIdFamilyDTO = new UpdateRoleIdFamilyDTO(1L, 2L);
        var userFamilyUpdateRole = new UserFamily(userMember, familyTest, RoleFamily.MEMBER);
        var userFamilyOwner = new UserFamily(user, familyTest, RoleFamily.OWNER);

        when(userService.findUserById(eq(2L))).thenReturn(userMember);
        when(familyRepository.findById(eq(1L))).thenReturn(Optional.of(familyTest));
        when(userFamilyRepository.existsByUserAndFamily(eq(user), eq(familyTest))).thenReturn(true);
        when(userFamilyRepository.existsByFamilyAndUserAndRoleFamily(eq(familyTest), eq(user), eq(RoleFamily.OWNER))).thenReturn(true);
        when(userFamilyRepository.findByUserAndFamily(eq(userMember), eq(familyTest))).thenReturn(Optional.of(userFamilyUpdateRole));
        when(userFamilyRepository.findByUserAndFamily(eq(user), eq(familyTest))).thenReturn(Optional.of(userFamilyOwner));

        familyConfigService.updateFamilyRole(1L, updateRoleIdFamilyDTO, userDetails);

        verify(userFamilyRepository, times(1)).flush();
        assertNotEquals(RoleFamily.OWNER, userFamilyOwner.getRoleFamily());
        assertEquals(RoleFamily.OWNER, userFamilyUpdateRole.getRoleFamily());
        assertEquals(RoleFamily.MEMBER, userFamilyOwner.getRoleFamily());
    }
    @Test
    @DisplayName("should not update when not owner changing family role")
    void notUpdateFamilyRoleWhenNotOwnerChangingFamilyRole() {
        ReflectionTestUtils.setField(familyTest, "id", 1L);
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(userMember, "id", 2L);
        var userDetails = new UserDetailsImpl(user);
        var updateRoleIdFamilyDTO = new UpdateRoleIdFamilyDTO(3L, 2L);
        var userFamilyUpdateRole = new UserFamily(userMember, familyTest, RoleFamily.MEMBER);

        when(userService.findUserById(eq(2L))).thenReturn(userMember);
        when(familyRepository.findById(eq(1L))).thenReturn(Optional.of(familyTest));
        when(userFamilyRepository.existsByUserAndFamily(eq(user), eq(familyTest))).thenReturn(true);
        when(userFamilyRepository.existsByFamilyAndUserAndRoleFamily(eq(familyTest), eq(user), eq(RoleFamily.OWNER))).thenReturn(false);

        Exception exception = assertThrows(BudgetMasterSecurityException.class, () -> familyConfigService.updateFamilyRole(1L, updateRoleIdFamilyDTO, userDetails));

        verify(userFamilyRepository, never()).flush();
        assertEquals(RoleFamily.MEMBER, userFamilyUpdateRole.getRoleFamily());
        assertNotEquals(RoleFamily.VIEWER, userFamilyUpdateRole.getRoleFamily());
        assertEquals("You do not have permission to perform the operation", exception.getMessage());
    }
    @Test
    @DisplayName("updateFamilyRole_shouldThrowException_whenOwnerTriesToChangeOwnRole")
    void notUpdateFamilyRoleWhenOwnerTriesToChangeOwnRole() {
        var userDetails = new UserDetailsImpl(user);
        var updateRoleIdFamilyDTO = new UpdateRoleIdFamilyDTO(2L, 1L);
        var userFamilyUpdateRole = new UserFamily(user, familyTest, RoleFamily.OWNER);
        ReflectionTestUtils.setField(familyTest, "id", 1L);
        ReflectionTestUtils.setField(user, "id", 1L);
        System.out.println("User id: " + user.getId());

        Exception exception = assertThrows(BusinessRuleException.class, () -> familyConfigService.updateFamilyRole(1L, updateRoleIdFamilyDTO, userDetails));

        verify(userFamilyRepository, never()).flush();
        assertEquals(RoleFamily.OWNER, userFamilyUpdateRole.getRoleFamily());
        assertNotEquals(RoleFamily.MEMBER, userFamilyUpdateRole.getRoleFamily());
        assertEquals("It is not possible to change one's role in the family without passing it on to another member.", exception.getMessage());
    }


}