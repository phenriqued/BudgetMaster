package phenriqued.BudgetMaster.Services.FamilyService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import phenriqued.BudgetMaster.DTOs.Family.FamilyMemberIdDTO;
import phenriqued.BudgetMaster.DTOs.Family.RequestCreateFamilyDTO;
import phenriqued.BudgetMaster.DTOs.Login.RegisterUserDTO;
import phenriqued.BudgetMaster.Infra.Email.FamilyEmailService;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BudgetMasterSecurityException;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Models.FamilyEntity.Family;
import phenriqued.BudgetMaster.Models.FamilyEntity.RoleFamily;
import phenriqued.BudgetMaster.Models.FamilyEntity.UserFamily;
import phenriqued.BudgetMaster.Models.UserEntity.Role.Role;
import phenriqued.BudgetMaster.Models.UserEntity.Role.RoleName;
import phenriqued.BudgetMaster.Models.UserEntity.User;
import phenriqued.BudgetMaster.Repositories.FamilyRepositories.FamilyRepository;
import phenriqued.BudgetMaster.Repositories.FamilyRepositories.UserFamilyRepository;
import phenriqued.BudgetMaster.Services.Security.TokensService.TokenService;
import phenriqued.BudgetMaster.Services.UserServices.UserService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteFamilyMemberAndLeaveFamilyConfigServiceTest {

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
    private User userViewer = new User(new RegisterUserDTO("test Viewer", "testeIII@email.com", "Teste123!"), "Teste123!", new Role(1L, RoleName.USER));
    private Family familyTest = new Family(new RequestCreateFamilyDTO("Family Test", List.of()));
    UserFamily userOwner = new UserFamily(user, familyTest, RoleFamily.OWNER);
    UserFamily userFamilyMember = new UserFamily(userMember, familyTest, RoleFamily.MEMBER);
    UserFamily userFamilyViewer = new UserFamily(userViewer, familyTest, RoleFamily.VIEWER);

    @BeforeEach
    public void setup(){
        ReflectionTestUtils.setField(familyTest, "id", 1L);
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(userMember, "id", 2L);
        ReflectionTestUtils.setField(userViewer, "id", 3L);
    }

    @Test
    @DisplayName("should delete a family member successfully when the owner makes the request")
    void deleteFamilyMemberSuccess() {
        familyTest.addUserFamily(userOwner);
        familyTest.addUserFamily(userFamilyMember);
        var userDetails = new UserDetailsImpl(user);
        var familyMemberIdDTO = new FamilyMemberIdDTO(2L);

        ReflectionTestUtils.setField(userFamilyMember, "id", 2L);

        when(userService.findUserById(familyMemberIdDTO.memberId())).thenReturn(userMember);
        when(familyRepository.findById(1L)).thenReturn(Optional.of(familyTest));
        when(userFamilyRepository.findByUserAndFamily(userMember, familyTest)).thenReturn(Optional.of(userFamilyMember));
        when(userFamilyRepository.existsByUserAndFamily(user, familyTest)).thenReturn(true);
        when(userFamilyRepository.existsByFamilyAndUserAndRoleFamily(familyTest, user, RoleFamily.OWNER)).thenReturn(true);

        familyConfigService.deleteFamilyMember(1L, familyMemberIdDTO, userDetails);

        verify(userFamilyRepository, times(1)).deleteById(eq(2L));
        assertFalse(familyTest.getUserFamilies().contains(userFamilyMember));
    }
    @Test
    @DisplayName("should throw BudgetMasterSecurityException when a user tries to delete themselves")
    void shouldNotDeleteFamilyMemberWhenUserTrieDeleteYourself() {
        familyTest.addUserFamily(userOwner);
        familyTest.addUserFamily(userFamilyMember);
        var userDetails = new UserDetailsImpl(user);
        var familyMemberIdDTO = new FamilyMemberIdDTO(1L);

        ReflectionTestUtils.setField(userFamilyMember, "id", 1L);

        when(userService.findUserById(familyMemberIdDTO.memberId())).thenReturn(user);
        when(familyRepository.findById(1L)).thenReturn(Optional.of(familyTest));
        when(userFamilyRepository.findByUserAndFamily(user, familyTest)).thenReturn(Optional.of(userOwner));
        when(userFamilyRepository.existsByUserAndFamily(user, familyTest)).thenReturn(true);
        when(userFamilyRepository.existsByFamilyAndUserAndRoleFamily(familyTest, user, RoleFamily.OWNER)).thenReturn(true);

        Exception exception = assertThrows(BudgetMasterSecurityException.class, () -> familyConfigService.deleteFamilyMember(1L, familyMemberIdDTO, userDetails));

        verify(userFamilyRepository, never()).deleteById(eq(1L));
        assertEquals("Unable to delete", exception.getMessage());
        assertEquals(2, familyTest.getUserFamilies().size());
    }
    @Test
    @DisplayName("should throw BudgetMasterSecurityException when a user is not a Owner Family")
    void shouldNotDeleteFamilyMemberWhenUserIsNotOwner() {
        familyTest.addUserFamily(userOwner);
        familyTest.addUserFamily(userFamilyMember);
        var userDetails = new UserDetailsImpl(user);
        var familyMemberIdDTO = new FamilyMemberIdDTO(2L);

        ReflectionTestUtils.setField(userFamilyMember, "id", 2L);

        when(userService.findUserById(familyMemberIdDTO.memberId())).thenReturn(userMember);
        when(familyRepository.findById(1L)).thenReturn(Optional.of(familyTest));
        when(userFamilyRepository.findByUserAndFamily(userMember, familyTest)).thenReturn(Optional.of(userFamilyMember));
        when(userFamilyRepository.existsByUserAndFamily(user, familyTest)).thenReturn(true);
        when(userFamilyRepository.existsByFamilyAndUserAndRoleFamily(familyTest, user, RoleFamily.OWNER)).thenReturn(false);

        Exception exception = assertThrows(BudgetMasterSecurityException.class, () -> familyConfigService.deleteFamilyMember(1L, familyMemberIdDTO, userDetails));

        verify(userFamilyRepository, never()).deleteById(eq(2L));
        assertTrue(familyTest.getUserFamilies().contains(userFamilyMember));
        assertEquals(2, familyTest.getUserFamilies().size());
        assertEquals("You do not have permission to perform the operation", exception.getMessage());
    }

    @Test
    @DisplayName("user member or viewer should leave the family when he is successfully part of the family")
    void leaveFamilySuccess() {
        familyTest.addUserFamily(userOwner);
        familyTest.addUserFamily(userFamilyMember);
        var userDetails = new UserDetailsImpl(userMember);

        ReflectionTestUtils.setField(userFamilyMember, "id", 2L);

        when(familyRepository.findById(1L)).thenReturn(Optional.of(familyTest));
        when(userFamilyRepository.existsByUserAndFamily(userMember, familyTest)).thenReturn(true);
        when(userFamilyRepository.findByUserAndFamily(userMember, familyTest)).thenReturn(Optional.of(userFamilyMember));

        familyConfigService.leaveFamily(1L, userDetails);

        verify(userFamilyRepository, times(1)).deleteById(2L);
        assertFalse(familyTest.getUserFamilies().contains(userFamilyMember));
    }
    @Test
    @DisplayName("an owner user should leave the family when he is part of the family passing the ownership position to a member")
    void leaveFamilyOwnerUserPassingOwnerToMember() {
        familyTest.addUserFamily(userOwner);
        familyTest.addUserFamily(userFamilyMember);
        familyTest.addUserFamily(userFamilyViewer);
        var userDetails = new UserDetailsImpl(user);

        ReflectionTestUtils.setField(userOwner, "id", 1L);

        when(familyRepository.findById(1L)).thenReturn(Optional.of(familyTest));
        when(userFamilyRepository.existsByUserAndFamily(user, familyTest)).thenReturn(true);
        when(userFamilyRepository.findByUserAndFamily(user, familyTest)).thenReturn(Optional.of(userOwner));

        familyConfigService.leaveFamily(1L, userDetails);

        verify(userFamilyRepository, times(1)).deleteById(1L);
        assertFalse(familyTest.getUserFamilies().contains(userOwner));
        assertEquals(RoleFamily.OWNER, userFamilyMember.getRoleFamily());
    }
    @Test
    @DisplayName("an owner user should leave the family when he is part of the family and when the family is empty he should be deleted")
    void leaveFamilyOwnerUserWhenFamilyIsEmptyShouldBeDeleted() {
        familyTest.addUserFamily(userOwner);
        var userDetails = new UserDetailsImpl(user);

        ReflectionTestUtils.setField(userOwner, "id", 1L);

        when(familyRepository.findById(1L)).thenReturn(Optional.of(familyTest));
        when(userFamilyRepository.existsByUserAndFamily(user, familyTest)).thenReturn(true);
        when(userFamilyRepository.findByUserAndFamily(user, familyTest)).thenReturn(Optional.of(userOwner));

        familyConfigService.leaveFamily(1L, userDetails);

        verify(familyRepository, times(1)).deleteById(1L);
    }
    @Test
    @DisplayName("an owner user should leave the family when he is part of the family and when the family has no member, only viewer the family should be deleted")
    void leaveFamilyOwnerUserWhenFamilyHasNoMemberOnlyViewerFamilyShouldBeDeleted() {
        familyTest.addUserFamily(userOwner);
        familyTest.addUserFamily(userFamilyViewer);
        var userDetails = new UserDetailsImpl(user);

        ReflectionTestUtils.setField(userOwner, "id", 1L);

        when(familyRepository.findById(1L)).thenReturn(Optional.of(familyTest));
        when(userFamilyRepository.existsByUserAndFamily(user, familyTest)).thenReturn(true);
        when(userFamilyRepository.findByUserAndFamily(user, familyTest)).thenReturn(Optional.of(userOwner));

        familyConfigService.leaveFamily(1L, userDetails);

        verify(familyRepository, times(1)).deleteById(1L);
    }

}