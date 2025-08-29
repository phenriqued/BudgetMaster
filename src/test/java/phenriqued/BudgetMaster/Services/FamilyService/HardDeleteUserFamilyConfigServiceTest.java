package phenriqued.BudgetMaster.Services.FamilyService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import phenriqued.BudgetMaster.DTOs.Family.RequestCreateFamilyDTO;
import phenriqued.BudgetMaster.DTOs.Login.RegisterUserDTO;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HardDeleteUserFamilyConfigServiceTest {

    @InjectMocks
    private FamilyConfigService familyConfigService;
    @Mock
    private FamilyRepository familyRepository;
    @Mock
    private UserFamilyRepository userFamilyRepository;

    private User user = new User(new RegisterUserDTO("teste", "teste@email.com", "Teste123!"), "Teste123!", new Role(1L, RoleName.USER));
    private User userMember = new User(new RegisterUserDTO("test Member", "testeII@email.com", "Teste123!"), "Teste123!", new Role(1L, RoleName.USER));
    private User userViewer = new User(new RegisterUserDTO("test Viewer", "testeIII@email.com", "Teste123!"), "Teste123!", new Role(1L, RoleName.USER));
    private Family familyTest = new Family(new RequestCreateFamilyDTO("Family Test", List.of()));
    UserFamily userOwner = new UserFamily(user, familyTest, RoleFamily.OWNER);
    UserFamily userFamilyMember = new UserFamily(userMember, familyTest, RoleFamily.MEMBER);
    Family familyTestII = new Family(new RequestCreateFamilyDTO("Family Test II", List.of()));
    UserFamily userOwnerII = new UserFamily(user, familyTestII, RoleFamily.MEMBER);

    @Test
    @DisplayName("should delete all userFamily when user is not owner in any family")
    void hardDeleteUserFamilyWhenNotOwner() {
        when(userFamilyRepository.findAllByUser(user)).thenReturn(List.of(userOwnerII));

        familyConfigService.hardDeleteUserFamily(user);

        verify(userFamilyRepository).deleteAllByUser(user);
        verifyNoInteractions(familyRepository);
    }
    @Test
    @DisplayName("should promote oldest member to OWNER when user is deleted as owner")
    void hardDeleteUserFamilyWhenOwnerAndHasMembers() {
        familyTest.addUserFamily(userOwner);
        familyTest.addUserFamily(userFamilyMember);
        ReflectionTestUtils.setField(userOwner, "id", 1L);
        ReflectionTestUtils.setField(userFamilyMember, "id", 2L);

        when(userFamilyRepository.findAllByUser(user)).thenReturn(List.of(userOwner));
        when(userFamilyRepository.findByUserAndFamily(user, familyTest)).thenReturn(Optional.of(userOwner));

        familyConfigService.hardDeleteUserFamily(user);

        assertEquals(RoleFamily.OWNER, userFamilyMember.getRoleFamily());
        verify(userFamilyRepository).deleteById(1L);
        verify(familyRepository, never()).deleteById(any());
    }
    @Test
    @DisplayName("should delete family when owner is deleted and no other members exist")
    void hardDeleteUserFamilyWhenOwnerAndNoMembers() {
        familyTest.addUserFamily(userOwner);
        ReflectionTestUtils.setField(userOwner, "id", 1L);
        ReflectionTestUtils.setField(familyTest, "id", 99L);

        when(userFamilyRepository.findAllByUser(user)).thenReturn(List.of(userOwner));
        when(userFamilyRepository.findByUserAndFamily(user, familyTest)).thenReturn(Optional.of(userOwner));

        familyConfigService.hardDeleteUserFamily(user);

        verify(familyRepository).deleteById(99L);
        verify(userFamilyRepository, never()).deleteAllByUser(user);
    }


}