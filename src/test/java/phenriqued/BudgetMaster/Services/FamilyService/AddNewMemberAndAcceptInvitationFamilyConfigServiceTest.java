package phenriqued.BudgetMaster.Services.FamilyService;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import phenriqued.BudgetMaster.DTOs.Family.AddFamilyMemberDTO;
import phenriqued.BudgetMaster.DTOs.Family.RequestCreateFamilyDTO;
import phenriqued.BudgetMaster.DTOs.Family.RoleIdFamilyDTO;
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
class AddNewMemberAndAcceptInvitationFamilyConfigServiceTest {

    @InjectMocks
    private FamilyConfigService familyConfigService;
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

    private User user = new User(new RegisterUserDTO("teste", "teste@email.com", "Teste123!"), "Teste123!", new Role(1L, RoleName.USER));
    private User userMember = new User(new RegisterUserDTO("test Member", "testeII@email.com", "Teste123!"), "Teste123!", new Role(1L, RoleName.USER));
    private Family familyTest = new Family(new RequestCreateFamilyDTO("Family Test", List.of()));

    @BeforeEach
    public void setupEach(){
        UserFamily userOwner = new UserFamily(user, familyTest, RoleFamily.OWNER);
        familyTest.addUserFamily(userOwner);
    }

    @Test
    @DisplayName("should send the invitation email to a user successfully")
    void addFamilyMemberByEmailSuccess() {
        ReflectionTestUtils.setField(familyTest, "id", 1L);
        ReflectionTestUtils.setField(user, "id", 1L);
        var userDetails = new UserDetailsImpl(user);
        AddFamilyMemberDTO addFamilyMemberDTO = new AddFamilyMemberDTO("testeII@email.com", 2L);

        when(familyRepository.findById(1L)).thenReturn(Optional.of(familyTest));
        when(userFamilyRepository.existsByUserAndFamily(eq(user), eq(familyTest))).thenReturn(true);
        when(userFamilyRepository.existsByFamilyAndUserAndRoleFamily(familyTest, user, RoleFamily.OWNER)).thenReturn(true);
        when(userService.findUserByEmail(eq(addFamilyMemberDTO.email()))).thenReturn(userMember);
        when(userFamilyRepository.existsByUserAndFamily(eq(userMember), any(Family.class))).thenReturn(false);

        familyConfigService.addFamilyMemberByEmail(1L, addFamilyMemberDTO, userDetails);

        verify(familyEmailService, times(1)).invitedMember(userMember, familyTest, RoleFamily.MEMBER, user);
    }
    @Test
    @DisplayName("should throw BudgetMasterSecurityException when it is not the owner inviting")
    void shouldNotAddFamilyMemberByEmailWhenIsNotOwnerInviting() {
        ReflectionTestUtils.setField(familyTest, "id", 1L);
        ReflectionTestUtils.setField(user, "id", 1L);
        var userDetails = new UserDetailsImpl(user);
        AddFamilyMemberDTO addFamilyMemberDTO = new AddFamilyMemberDTO("testeII@email.com", 2L);

        when(familyRepository.findById(1L)).thenReturn(Optional.of(familyTest));
        when(userFamilyRepository.existsByUserAndFamily(eq(user), eq(familyTest))).thenReturn(true);
        when(userFamilyRepository.existsByFamilyAndUserAndRoleFamily(familyTest, user, RoleFamily.OWNER)).thenReturn(false);

        Exception exception = assertThrows(BudgetMasterSecurityException.class, () ->
                familyConfigService.addFamilyMemberByEmail(1L, addFamilyMemberDTO, userDetails));

        verify(familyEmailService, never()).invitedMember(userMember, familyTest, RoleFamily.MEMBER, user);
        assertEquals("You do not have permission to perform the operation", exception.getMessage());
    }
    @Test
    @DisplayName("should throw BusinessRuleException when Member Family already part of the Family")
    void shouldNotAddFamilyMemberByEmailWhenMemberAlreadyPartOfFamily() {
        ReflectionTestUtils.setField(familyTest, "id", 1L);
        ReflectionTestUtils.setField(user, "id", 1L);
        var userDetails = new UserDetailsImpl(user);
        AddFamilyMemberDTO addFamilyMemberDTO = new AddFamilyMemberDTO("testeII@email.com", 2L);

        when(familyRepository.findById(1L)).thenReturn(Optional.of(familyTest));
        when(userFamilyRepository.existsByUserAndFamily(eq(user), eq(familyTest))).thenReturn(true);
        when(userFamilyRepository.existsByFamilyAndUserAndRoleFamily(familyTest, user, RoleFamily.OWNER)).thenReturn(true);
        when(userService.findUserByEmail(eq(addFamilyMemberDTO.email()))).thenReturn(userMember);
        when(userFamilyRepository.existsByUserAndFamily(eq(userMember), any(Family.class))).thenReturn(true);

        Exception exception = assertThrows(BusinessRuleException.class, () ->
                familyConfigService.addFamilyMemberByEmail(1L, addFamilyMemberDTO, userDetails));

        verify(familyEmailService, never()).invitedMember(userMember, familyTest, RoleFamily.MEMBER, user);
        assertEquals("The user already belongs to the family", exception.getMessage());
    }

    @Test
    @DisplayName("should generate invitation URL with token successfully")
    void addFamilyMembersByQrCode() {
        ReflectionTestUtils.setField(familyTest, "id", 1L);
        ReflectionTestUtils.setField(user, "id", 1L);
        var userDetails = new UserDetailsImpl(user);
        RoleIdFamilyDTO roleIdFamilyDTO = new RoleIdFamilyDTO(2L); // MEMBER

        when(familyRepository.findById(1L)).thenReturn(Optional.of(familyTest));
        when(userFamilyRepository.existsByUserAndFamily(eq(user), eq(familyTest))).thenReturn(true);
        when(userFamilyRepository.existsByFamilyAndUserAndRoleFamily(familyTest, user, RoleFamily.OWNER)).thenReturn(true);
        when(tokenService.generatedTokenJwtAtFamily(familyTest, RoleFamily.MEMBER.id)).thenReturn("validated-token");

        String result = familyConfigService.addFamilyMembersByQrCode(1L, roleIdFamilyDTO, userDetails);

        assertEquals("http://localhost:8080/families/1/invitations/accept?access=validated-token", result);
        verify(tokenService, times(1)).generatedTokenJwtAtFamily(familyTest, RoleFamily.MEMBER.id);
        verify(userFamilyRepository, times(1)).existsByUserAndFamily(user, familyTest);
        verify(userFamilyRepository, times(1)).existsByFamilyAndUserAndRoleFamily(familyTest, user, RoleFamily.OWNER);
    }
    @Test
    @DisplayName("should throw BusinessRuleException when send the invitation to a member with the owner role")
    void shouldNotAddFamilyMembersByQrCodeWhenSendInvitationToMemberWithOwnerRole() {
        ReflectionTestUtils.setField(familyTest, "id", 1L);
        ReflectionTestUtils.setField(user, "id", 1L);
        var userDetails = new UserDetailsImpl(user);
        RoleIdFamilyDTO roleIdFamilyDTO = new RoleIdFamilyDTO(1L); // OWNER

        when(familyRepository.findById(1L)).thenReturn(Optional.of(familyTest));
        when(userFamilyRepository.existsByUserAndFamily(eq(user), eq(familyTest))).thenReturn(true);
        when(userFamilyRepository.existsByFamilyAndUserAndRoleFamily(familyTest, user, RoleFamily.OWNER)).thenReturn(true);

        Exception exception = assertThrows(BusinessRuleException.class, () -> familyConfigService.addFamilyMembersByQrCode(1L, roleIdFamilyDTO, userDetails));

        assertEquals("Unable to add a member as OWNER at this time", exception.getMessage());
        verify(tokenService, never()).generatedTokenJwtAtFamily(familyTest, RoleFamily.MEMBER.id);
    }

    @Test
    @DisplayName("should accept an invite when the token is valid")
    void acceptInvitationByQrCode() {
        ReflectionTestUtils.setField(familyTest, "id", 1L);
        ReflectionTestUtils.setField(userMember, "id", 2L);
        DecodedJWT decodedTokenMock = mock(DecodedJWT.class);
        Claim mockClaimFamilyId = mock(Claim.class);
        Claim mockClaimRoleId = mock(Claim.class);
        var userDetails = new UserDetailsImpl(userMember);
        var userFamily = new UserFamily(userMember, familyTest, RoleFamily.MEMBER);

        when(familyRepository.findById(1L)).thenReturn(Optional.of(familyTest));
        when(userFamilyRepository.existsByUserAndFamily(eq(userMember), eq(familyTest))).thenReturn(false);
        when(tokenService.validationTokenJwtAtFamily(anyString())).thenReturn(decodedTokenMock);
        when(mockClaimFamilyId.asLong()).thenReturn(1L);
        when(decodedTokenMock.getClaim("familyId")).thenReturn(mockClaimFamilyId);
        when(mockClaimRoleId.asLong()).thenReturn(2L);
        when(decodedTokenMock.getClaim("roleId")).thenReturn(mockClaimRoleId);
        when(userFamilyRepository.save(eq(userFamily))).thenReturn(userFamily);

        familyConfigService.acceptInvitationByQrCode(1L, "valid-token", userDetails);

        verify(userFamilyRepository, times(1)).save(any(UserFamily.class));
        ArgumentCaptor<UserFamily> userFamilyCaptor = ArgumentCaptor.forClass(UserFamily.class);
        verify(userFamilyRepository).save(userFamilyCaptor.capture());
        UserFamily capturedUserFamily = userFamilyCaptor.getValue();
        assertEquals(userMember, capturedUserFamily.getUser());
        assertEquals(familyTest, capturedUserFamily.getFamily());
        assertEquals(RoleFamily.MEMBER, capturedUserFamily.getRoleFamily());
        assertTrue(familyTest.getUserFamilies().contains(capturedUserFamily));
    }

    @Test
    @DisplayName("should throw BudgetMasterUnauthorizedException when the token is invalid")
    void shouldNotAcceptInvitationByQrCodeWhenTokenIsInvalid() {
        var userDetails = new UserDetailsImpl(userMember);

        when(familyRepository.findById(1L)).thenReturn(Optional.of(familyTest));
        when(userFamilyRepository.existsByUserAndFamily(eq(userMember), eq(familyTest))).thenReturn(false);
        when(tokenService.validationTokenJwtAtFamily(anyString())).thenThrow(new BudgetMasterUnauthorizedException("Token is invalid."));

        Exception exception = assertThrows(BudgetMasterUnauthorizedException.class, () ->
                familyConfigService.acceptInvitationByQrCode(1L, "invalid-token", userDetails));
        assertEquals("Token is invalid.", exception.getMessage());
    }

}