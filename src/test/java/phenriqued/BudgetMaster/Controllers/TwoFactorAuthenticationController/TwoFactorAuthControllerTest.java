package phenriqued.BudgetMaster.Controllers.TwoFactorAuthenticationController;

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
import phenriqued.BudgetMaster.DTOs.Security.TwoFactorAuth.Request2faActiveDTO;
import phenriqued.BudgetMaster.DTOs.Security.TwoFactorAuth.RequestValid2faDTO;
import phenriqued.BudgetMaster.Infra.Email.EmailService;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Models.Security.Token.TokenType;
import phenriqued.BudgetMaster.Models.Security.TwoFactorAuthentication.Type2FA;
import phenriqued.BudgetMaster.Models.UserEntity.Role.RoleName;
import phenriqued.BudgetMaster.Models.UserEntity.User;
import phenriqued.BudgetMaster.Repositories.RoleRepository.RoleRepository;
import phenriqued.BudgetMaster.Repositories.Security.TwoFactorAuthRepository;
import phenriqued.BudgetMaster.Repositories.UserRepository.UserRepository;
import phenriqued.BudgetMaster.Services.Security.TokensService.TokenService;
import phenriqued.BudgetMaster.Services.Security.TwoFactorAuthServices.TwoFactorAuthService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class TwoFactorAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private TwoFactorAuthRepository twoFactorAuthRepository;
    @Autowired
    private TwoFactorAuthService twoFactorAuthService;
    @MockitoBean
    private EmailService emailService;

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
    @DisplayName("should start activating two-factor authentication")
    void activeTwoFactorAuthentication() throws Exception{
        var user = userRepository.findByEmail(userData.email()).orElseThrow();
        var token = tokenService.generatedRefreshTokenAndTokenJWT(new UserDetailsImpl(user), TokenType.WEB, "teste");
        String json = new ObjectMapper().writeValueAsString(new Request2faActiveDTO("EMAIL"));

        mockMvc.perform(post("/account/two-factor-auth/initiate")
                .header("Authorization", "Bearer " + token.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("Code sent successfully"));

        user = userRepository.findByEmail(userData.email()).orElseThrow();
        var twoFactorAuth = twoFactorAuthRepository.findByUserAndType2FA(user, Type2FA.EMAIL);
        verify(emailService, times(1)).sendMail(any(), any(), any());
        assertTrue(twoFactorAuth.isPresent());
        assertFalse(user.getTwoFactorAuths().isEmpty());
        assertFalse(twoFactorAuth.get().getIsActive());
    }

    @Test
    @DisplayName("should check the code and if 2FA is not activated, you should activate it.")
    void verifyTwoFactorAuthentication() throws Exception{
        var user = userRepository.findByEmail(userData.email()).orElseThrow();
        var token = tokenService.generatedRefreshTokenAndTokenJWT(new UserDetailsImpl(user), TokenType.WEB, "teste");
        twoFactorAuthService.createTwoFactorAuth(new Request2faActiveDTO("EMAIL"), user.getEmail());
        var twoFactorAuth = twoFactorAuthRepository.findByUserAndType2FA(user, Type2FA.EMAIL).orElseThrow();
        String json = new ObjectMapper().writeValueAsString(new RequestValid2faDTO(twoFactorAuth.getSecret(), "EMAIL"));

        mockMvc.perform(put("/account/two-factor-auth/validate")
                        .header("Authorization", "Bearer " + token.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNoContent());

        user = userRepository.findByEmail(userData.email()).orElseThrow();
        twoFactorAuth = twoFactorAuthRepository.findByUserAndType2FA(user, Type2FA.EMAIL).orElseThrow();
        assertTrue(user.isTwoFactorAuthEnabled());
        assertTrue(twoFactorAuth.getIsActive());
    }
    @Test
    @DisplayName("should check the code and the code is invalid do not activate 2fa")
    void shouldCheckCodeWhenCodeIsInvalidNotActive2FA() throws Exception{
        var user = userRepository.findByEmail(userData.email()).orElseThrow();
        var token = tokenService.generatedRefreshTokenAndTokenJWT(new UserDetailsImpl(user), TokenType.WEB, "teste");
        twoFactorAuthService.createTwoFactorAuth(new Request2faActiveDTO("EMAIL"), user.getEmail());
        String json = new ObjectMapper().writeValueAsString(new RequestValid2faDTO("InvalidCode123", "EMAIL"));

        mockMvc.perform(put("/account/two-factor-auth/validate")
                        .header("Authorization", "Bearer " + token.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("invalid code, check the code entered."));

        user = userRepository.findByEmail(userData.email()).orElseThrow();
        var twoFactorAuth = twoFactorAuthRepository.findByUserAndType2FA(user, Type2FA.EMAIL).orElseThrow();
        assertFalse(user.isTwoFactorAuthEnabled());
        assertFalse(twoFactorAuth.getIsActive());
    }
    @Test
    @DisplayName("should check the code for expired code should forward to resend the code")
    void shouldCheckCodeWhenCodeIsExpiredResendCode() throws Exception{
        var user = userRepository.findByEmail(userData.email()).orElseThrow();
        var token = tokenService.generatedRefreshTokenAndTokenJWT(new UserDetailsImpl(user), TokenType.WEB, "teste");
        twoFactorAuthService.createTwoFactorAuth(new Request2faActiveDTO("EMAIL"), user.getEmail());
        var twoFactorAuth = twoFactorAuthRepository.findByUserAndType2FA(user, Type2FA.EMAIL).orElseThrow();
        twoFactorAuth.setExpirationAt(LocalDateTime.now().minusMinutes(10));
        twoFactorAuthRepository.save(twoFactorAuth);
        String json = new ObjectMapper().writeValueAsString(new RequestValid2faDTO(twoFactorAuth.getSecret(), "EMAIL"));

        mockMvc.perform(put("/account/two-factor-auth/validate")
                        .header("Authorization", "Bearer " + token.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "http://localhost:8080/account/two-factor-auth/resend-code?code="+twoFactorAuth.getSecret()));

        user = userRepository.findByEmail(userData.email()).orElseThrow();
        twoFactorAuth = twoFactorAuthRepository.findByUserAndType2FA(user, Type2FA.EMAIL).orElseThrow();
        assertFalse(user.isTwoFactorAuthEnabled());
        assertFalse(twoFactorAuth.getIsActive());
    }

    @Test
    @DisplayName("should send a new code for a code that is already expired")
    void resendCodeTwoFactorAuthentication() throws Exception {
        var user = userRepository.findByEmail(userData.email()).orElseThrow();
        twoFactorAuthService.createTwoFactorAuth(new Request2faActiveDTO("EMAIL"), user.getEmail());
        var twoFactorAuth = twoFactorAuthRepository.findByUserAndType2FA(user, Type2FA.EMAIL).orElseThrow();

        mockMvc.perform(post("/account/two-factor-auth/resend-code?code="+twoFactorAuth.getSecret()))
                .andExpect(status().isNoContent());

        verify(emailService, times(2)).sendMail(any(), any(), any());
    }
    @Test
    @DisplayName("should not send a new code for a code that isn't expired")
    void shouldNotSendNewCodeWhenCodeIsNotExpired() throws Exception {
        var user = userRepository.findByEmail(userData.email()).orElseThrow();
        twoFactorAuthService.createTwoFactorAuth(new Request2faActiveDTO("EMAIL"), user.getEmail());
        var twoFactorAuth = twoFactorAuthRepository.findByUserAndType2FA(user, Type2FA.EMAIL).orElseThrow();
        twoFactorAuth.setExpirationAt(LocalDateTime.now().minusMinutes(10));
        twoFactorAuthRepository.save(twoFactorAuth);

        mockMvc.perform(post("/account/two-factor-auth/resend-code?code="+twoFactorAuth.getSecret()))
                .andExpect(status().isBadRequest());

        verify(emailService, times(1)).sendMail(any(), any(), any());
    }
}