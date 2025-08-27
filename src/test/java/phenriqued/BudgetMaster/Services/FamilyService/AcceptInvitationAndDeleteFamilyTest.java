package phenriqued.BudgetMaster.Services.FamilyService;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import phenriqued.BudgetMaster.DTOs.Family.FamilyMemberDTO;
import phenriqued.BudgetMaster.DTOs.Family.RequestCreateFamilyDTO;
import phenriqued.BudgetMaster.DTOs.Login.RegisterUserDTO;
import phenriqued.BudgetMaster.Infra.Email.FamilyEmailService;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BudgetMasterSecurityException;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BudgetMasterUnauthorizedException;
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
import phenriqued.BudgetMaster.Services.Security.TokensService.TokenService;
import phenriqued.BudgetMaster.Services.UserServices.UserService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcceptInvitationAndDeleteFamilyTest {

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
    private TokenService tokenService;
    @Mock
    private User user = new User(new RegisterUserDTO("teste", "teste@email.com", "Teste123!"), "Teste123!", new Role(1L, RoleName.USER));
    private User userTest = new User(new RegisterUserDTO("teste I", "testeI@email.com", "Teste123!"), "Teste123!", new Role(1L, RoleName.USER));
    private Family familyTest = new Family(new RequestCreateFamilyDTO("Family Test", List.of(new FamilyMemberDTO("teste@email.com", 1L))));

    @Test
    @DisplayName("should accept a family invitation successfully")
    void acceptFamilyInvitationSuccess() {
        DecodedJWT decodedTokenMock = mock(DecodedJWT.class);
        Claim mockClaimFamilyId = mock(Claim.class);
        Claim mockClaimRoleId = mock(Claim.class);
        ReflectionTestUtils.setField(familyTest, "id", 1L);
        UserFamily savedUserFamily = new UserFamily(userTest, familyTest, RoleFamily.MEMBER);

        when(tokenService.validationTokenJwtAtFamily(anyString())).thenReturn(decodedTokenMock);
        when(mockClaimFamilyId.asLong()).thenReturn(1L);
        when(decodedTokenMock.getClaim("familyId")).thenReturn(mockClaimFamilyId);
        when(mockClaimRoleId.asLong()).thenReturn(2L);
        when(decodedTokenMock.getClaim("roleId")).thenReturn(mockClaimRoleId);
        when(decodedTokenMock.getSubject()).thenReturn("testeI@email.com");
        when(userService.findUserByEmail(eq("testeI@email.com"))).thenReturn(userTest);
        when(familyRepository.findById(eq(1L))).thenReturn(Optional.of(familyTest));
        when(userFamilyRepository.existsByUserAndFamily(userTest, familyTest)).thenReturn(false);
        when(userFamilyRepository.save(any(UserFamily.class))).thenReturn(savedUserFamily);

        familyService.acceptFamilyInvitation("some-valid-token");

        verify(userFamilyRepository, times(1)).save(any(UserFamily.class));
        ArgumentCaptor<UserFamily> userFamilyCaptor = ArgumentCaptor.forClass(UserFamily.class);
        verify(userFamilyRepository).save(userFamilyCaptor.capture());
        UserFamily capturedUserFamily = userFamilyCaptor.getValue();
        assertEquals(userTest, capturedUserFamily.getUser());
        assertEquals(familyTest, capturedUserFamily.getFamily());
        assertEquals(RoleFamily.MEMBER, capturedUserFamily.getRoleFamily());
        assertTrue(userTest.getFamily().contains(capturedUserFamily));
        assertTrue(familyTest.getUserFamilies().contains(capturedUserFamily));
    }

    @Test
    @DisplayName("should not accept the invitation when the user is already part of the family")
    void notAcceptFamilyInvitationWhenUserAlreadyPartFamily(){
        DecodedJWT decodedTokenMock = mock(DecodedJWT.class);
        Claim mockClaimFamilyId = mock(Claim.class);
        Claim mockClaimRoleId = mock(Claim.class);
        ReflectionTestUtils.setField(familyTest, "id", 1L);

        when(tokenService.validationTokenJwtAtFamily(anyString())).thenReturn(decodedTokenMock);
        when(mockClaimFamilyId.asLong()).thenReturn(1L);
        when(decodedTokenMock.getClaim("familyId")).thenReturn(mockClaimFamilyId);
        when(mockClaimRoleId.asLong()).thenReturn(2L);
        when(decodedTokenMock.getClaim("roleId")).thenReturn(mockClaimRoleId);
        when(decodedTokenMock.getSubject()).thenReturn("testeI@email.com");
        when(userService.findUserByEmail(eq("testeI@email.com"))).thenReturn(userTest);
        when(familyRepository.findById(eq(1L))).thenReturn(Optional.of(familyTest));
        when(userFamilyRepository.existsByUserAndFamily(userTest, familyTest)).thenReturn(true);

        Exception exception = assertThrows(BusinessRuleException.class,
                () -> familyService.acceptFamilyInvitation("some-valid-token"));
        verify(userFamilyRepository, never()).save(any());
        verify(familyRepository, never()).flush();
        assertEquals("the user is already part of the "+familyTest.getName(), exception.getMessage());
    }

    @Test
    @DisplayName("should not accept the invitation when the token is invalid")
    void notAcceptFamilyInvitationWhenTokenIsInvalid(){
        when(tokenService.validationTokenJwtAtFamily(anyString()))
                .thenThrow(new BudgetMasterUnauthorizedException("Token is invalid."));

        assertThrows(BudgetMasterUnauthorizedException.class, () ->
                familyService.acceptFamilyInvitation("some-invalid-token"));
        verify(userFamilyRepository, never()).save(any());
        verify(familyRepository, never()).flush();
    }

    @Test
    @DisplayName("should delete a family successfully when the owner user makes the request")
    void deleteFamilyByIdSuccess() {
        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        UserFamily userMemberFamily = new UserFamily(userTest, familyTest, RoleFamily.MEMBER);
        UserFamily userOwnerFamily = new UserFamily(user, familyTest, RoleFamily.OWNER);
        ReflectionTestUtils.setField(familyTest, "id", 1L);

        when(familyRepository.findById(eq(1L))).thenReturn(Optional.of(familyTest));
        when(userFamilyRepository.existsByUserAndFamily(user, familyTest)).thenReturn(true);
        when(userFamilyRepository.existsByFamilyAndUserAndRoleFamily(eq(familyTest), eq(user), eq(RoleFamily.OWNER))).thenReturn(true);
        when(userFamilyRepository.findAllByFamily(eq(familyTest))).thenReturn(List.of(userMemberFamily, userOwnerFamily));

        familyService.deleteFamilyByIdAndUser(1L, userDetails);

        verify(familyRepository, times(1)).deleteById(eq(1L));
        verify(familyEmailService, times(2)).deletionNotice(any(User.class), eq(familyTest));
    }
    @Test
    @DisplayName("should not delete the family when the user is not the owner")
    void notDeleteFamilyByIdWhenUserIsNotOwner() {
        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        ReflectionTestUtils.setField(familyTest, "id", 1L);

        when(familyRepository.findById(eq(1L))).thenReturn(Optional.of(familyTest));
        when(userFamilyRepository.existsByUserAndFamily(user, familyTest)).thenReturn(true);
        when(userFamilyRepository.existsByFamilyAndUserAndRoleFamily(eq(familyTest), eq(user), eq(RoleFamily.OWNER))).thenReturn(false);

        Exception exception = assertThrows(BudgetMasterSecurityException.class, () -> familyService.deleteFamilyByIdAndUser(1L, userDetails));
        verify(familyRepository, never()).deleteById(eq(1L));
        verify(familyEmailService, never()).deletionNotice(any(User.class), eq(familyTest));
        assertEquals("only Owner can delete.", exception.getMessage());
    }
}