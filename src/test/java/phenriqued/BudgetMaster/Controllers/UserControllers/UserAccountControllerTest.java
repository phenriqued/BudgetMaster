package phenriqued.BudgetMaster.Controllers.UserControllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import phenriqued.BudgetMaster.DTOs.Login.RegisterUserDTO;
import phenriqued.BudgetMaster.DTOs.Security.PasswordDTOs.RequestConfirmationPasswordUser;
import phenriqued.BudgetMaster.DTOs.Security.PasswordDTOs.RequestOnlyNewPasswordChangeDTO;
import phenriqued.BudgetMaster.DTOs.Security.PasswordDTOs.RequestPasswordChangeUserDTO;
import phenriqued.BudgetMaster.Infra.Email.EmailService;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Models.Security.Token.TokenType;
import phenriqued.BudgetMaster.Models.UserEntity.Role.RoleName;
import phenriqued.BudgetMaster.Models.UserEntity.User;
import phenriqued.BudgetMaster.Repositories.RoleRepository.RoleRepository;
import phenriqued.BudgetMaster.Repositories.Security.SecurityUserTokenRepository;
import phenriqued.BudgetMaster.Repositories.UserRepository.UserRepository;
import phenriqued.BudgetMaster.Services.Security.TokensService.TokenService;
import phenriqued.BudgetMaster.Services.UserServices.UserService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class UserAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserService userService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private SecurityUserTokenRepository tokenRepository;
    @MockitoBean
    private EmailService emailService;
    //setup
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    private PasswordEncoder encoder = new BCryptPasswordEncoder();
    private RegisterUserDTO userData = new RegisterUserDTO("teste", "teste@email.com", "Teste123@");

    @BeforeEach
    void setup(){
        var role = roleRepository.findByName(RoleName.USER).orElseThrow();
        var user = new User(userData, encoder.encode(userData.password()), role);
        user.setIsActive();
        userRepository.save(user);
    }
    @AfterEach
    void afterEach(){
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("should deactivate the user when password is correct")
    void shouldDisableUser() throws Exception{
        var user = userRepository.findByEmail(userData.email()).orElseThrow();
        var tokens = tokenService.generatedRefreshTokenAndTokenJWT(new UserDetailsImpl(user), TokenType.WEB, "teste");
        String json = new ObjectMapper().writeValueAsString(new RequestConfirmationPasswordUser("Teste123@"));

        mockMvc.perform(put("/account/manager/disable")
                        .header("Authorization", "Bearer " + tokens.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNoContent());

        user = userRepository.findByEmail(userData.email()).orElseThrow();
        assertFalse(user.getIsActive());
        assertEquals(1, user.getSecurityUserTokens().size());
        assertEquals(TokenType.OPEN_ID, user.getSecurityUserTokens().get(0).getTokenType());
        verify(emailService, times(1)).sendMail(any(), any(), any());
    }

    @Test
    @DisplayName("should not deactivate the user when password isn't correct")
    void shouldNotDisableUserWhenPasswordIsIncorrect() throws Exception{
        var user = userRepository.findByEmail(userData.email()).orElseThrow();
        var tokens = tokenService.generatedRefreshTokenAndTokenJWT(new UserDetailsImpl(user), TokenType.WEB, "teste");
        String json = new ObjectMapper().writeValueAsString(new RequestConfirmationPasswordUser("IncorrectPassword123!"));

        mockMvc.perform(put("/account/manager/disable")
                        .header("Authorization", "Bearer " + tokens.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Invalid Password!"));

        user = userRepository.findByEmail(userData.email()).orElseThrow();
        assertTrue(user.getIsActive());
    }

    @Test
    @DisplayName("should activate and change the user's password, after accessing the email recovery link")
    void changePasswordToActivatedAccount() throws Exception{
        var user = userRepository.findByEmail(userData.email()).orElseThrow();
        userService.disableUser(new RequestConfirmationPasswordUser("Teste123@"), user);
        var token = tokenRepository.findByIdentifierAndUser("open-id-"+user.getId()+"security-management", user).orElseThrow();
        String json = new ObjectMapper().writeValueAsString(new RequestOnlyNewPasswordChangeDTO("Abc321!"));


        mockMvc.perform(put("/account/manager/change-password-to-activate?code="+token.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "http://localhost:8080/login/signin"));

        user = userRepository.findByEmail(userData.email()).orElseThrow();
        assertTrue(user.getIsActive());
        assertTrue(user.getSecurityUserTokens().isEmpty());
        assertFalse(encoder.matches("Teste123@", user.getPassword()));
    }

    @Test
    @DisplayName("should not activate and change the user's password, when new user's password is the same old password")
    void shouldNotChangePasswordToActivatedAccountWhenPasswordIsSame() throws Exception{
        var user = userRepository.findByEmail(userData.email()).orElseThrow();
        userService.disableUser(new RequestConfirmationPasswordUser("Teste123@"), user);
        var token = tokenRepository.findByIdentifierAndUser("open-id-"+user.getId()+"security-management", user).orElseThrow();
        String json = new ObjectMapper().writeValueAsString(new RequestOnlyNewPasswordChangeDTO("Teste123@"));


        mockMvc.perform(put("/account/manager/change-password-to-activate?code="+token.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("New password cannot match current password!"));

        user = userRepository.findByEmail(userData.email()).orElseThrow();
        assertFalse(user.getIsActive());
        assertFalse(user.getSecurityUserTokens().isEmpty());
        assertTrue(encoder.matches("Teste123@", user.getPassword()));
    }

    @Test
    @DisplayName("should not activate and change the user's password, when o code link is incorrect")
    void shouldNotChangePasswordToActivatedAccountWhenCodeIsIncorrect() throws Exception{
        var user = userRepository.findByEmail(userData.email()).orElseThrow();
        userService.disableUser(new RequestConfirmationPasswordUser("Teste123@"), user);
        String json = new ObjectMapper().writeValueAsString(new RequestOnlyNewPasswordChangeDTO("Abc321!"));

        mockMvc.perform(put("/account/manager/change-password-to-activate?code=IncorrectCode123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());

        user = userRepository.findByEmail(userData.email()).orElseThrow();
        assertFalse(user.getIsActive());
        assertFalse(user.getSecurityUserTokens().isEmpty());
        assertTrue(encoder.matches("Teste123@", user.getPassword()));
    }

    @Test
    @DisplayName("should change the password when the data is correct and there is a new password")
    void editPassword() throws Exception{
        var user = userRepository.findByEmail(userData.email()).orElseThrow();
        var tokens = tokenService.generatedRefreshTokenAndTokenJWT(new UserDetailsImpl(user), TokenType.WEB, "teste");
        String json = new ObjectMapper().writeValueAsString(new RequestPasswordChangeUserDTO("Teste123@", "Abc321!"));

        mockMvc.perform(put("/account/manager/edit-password")
                        .header("Authorization", "Bearer "+tokens.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNoContent());
        user = userRepository.findByEmail(userData.email()).orElseThrow();
        assertFalse(encoder.matches("Teste123@", user.getPassword()));
    }
    @Test
    @DisplayName("should not change the password when the password is incorrect")
    void shouldNotEditPasswordWhenOldPasswordIsIncorrect() throws Exception{
        var user = userRepository.findByEmail(userData.email()).orElseThrow();
        var tokens = tokenService.generatedRefreshTokenAndTokenJWT(new UserDetailsImpl(user), TokenType.WEB, "teste");
        String json = new ObjectMapper().writeValueAsString(new RequestPasswordChangeUserDTO("IncorrectPassword", "Abc321!"));

        mockMvc.perform(put("/account/manager/edit-password")
                        .header("Authorization", "Bearer "+tokens.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
        user = userRepository.findByEmail(userData.email()).orElseThrow();
        assertTrue(encoder.matches("Teste123@", user.getPassword()));
    }
    @Test
    @DisplayName("should not change the password when the password and new password is the same")
    void shouldNotEditPasswordWhenOldPasswordAndNewIsSame() throws Exception{
        var user = userRepository.findByEmail(userData.email()).orElseThrow();
        var tokens = tokenService.generatedRefreshTokenAndTokenJWT(new UserDetailsImpl(user), TokenType.WEB, "teste");
        String json = new ObjectMapper().writeValueAsString(new RequestPasswordChangeUserDTO("Teste123@", "Teste123@"));

        mockMvc.perform(put("/account/manager/edit-password")
                        .header("Authorization", "Bearer "+tokens.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
        user = userRepository.findByEmail(userData.email()).orElseThrow();
        assertTrue(encoder.matches("Teste123@", user.getPassword()));
    }

}