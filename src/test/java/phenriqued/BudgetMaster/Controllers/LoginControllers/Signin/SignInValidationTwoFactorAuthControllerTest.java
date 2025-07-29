package phenriqued.BudgetMaster.Controllers.LoginControllers.Signin;

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
import phenriqued.BudgetMaster.DTOs.Login.SignInDTO;
import phenriqued.BudgetMaster.DTOs.Security.TwoFactorAuth.Request2faActiveDTO;
import phenriqued.BudgetMaster.DTOs.Security.TwoFactorAuth.RequestValidSignIn2faDTO;
import phenriqued.BudgetMaster.Infra.Email.EmailService;
import phenriqued.BudgetMaster.Models.Security.TwoFactorAuthentication.Type2FA;
import phenriqued.BudgetMaster.Models.UserEntity.Role.RoleName;
import phenriqued.BudgetMaster.Models.UserEntity.User;
import phenriqued.BudgetMaster.Repositories.RoleRepository.RoleRepository;
import phenriqued.BudgetMaster.Repositories.Security.TwoFactorAuthRepository;
import phenriqued.BudgetMaster.Repositories.UserRepository.UserRepository;
import phenriqued.BudgetMaster.Services.LoginService.SignInService;
import phenriqued.BudgetMaster.Services.Security.TwoFactorAuthServices.TwoFactorAuthService;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class SignInValidationTwoFactorAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TwoFactorAuthService twoFactorAuthService;
    @Autowired
    private TwoFactorAuthRepository twoFactorAuthRepository;
    @Autowired
    private SignInService signInService;
    @MockitoBean
    private EmailService emailService;

    //setup
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    private RegisterUserDTO userData = new RegisterUserDTO("teste", "teste@email.com", "Teste123@");
    private PasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setup(){
        var role = roleRepository.findByName(RoleName.USER).orElseThrow();
        var password = encoder.encode(userData.password());
        var user = new User(userData, password, role);
        user.setIsActive();
        userRepository.save(user);
        twoFactorAuthService.createTwoFactorAuth(new Request2faActiveDTO("EMAIL"), "teste@email.com");
        var twoFactor = twoFactorAuthRepository.findByUserAndType2FA(user, Type2FA.EMAIL).orElseThrow();
        twoFactor.setActive();
        twoFactorAuthRepository.save(twoFactor);
    }
    @AfterEach
    void afterEach(){
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("should return 200 ok when a user validates the account with 2FA")
    void validationTwoFactorAuthentication() throws Exception  {
        //Arrange
        var signInData = new SignInDTO("teste@email.com", "Teste123@", "WEB", "teste");
        var securityData = signInService.logIntoAccount(signInData);
        var user = userRepository.findByEmail(signInData.email()).orElseThrow();
        var twoFactor = twoFactorAuthRepository.findByUserAndType2FA(user, Type2FA.EMAIL).orElseThrow();
        String json = new ObjectMapper()
                .writeValueAsString(new RequestValidSignIn2faDTO(twoFactor.getSecret(), "EMAIL", securityData.securityUserToken2fa(),"WEB", "teste"));

        mockMvc.perform(put("/login/validation2fa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.refreshToken").isString());
    }
    @Test
    @DisplayName("should return 302 Found when a user validates the account with expired code 2FA")
    void shouldReturn302WhenUserValidatesWithExpiredCode() throws Exception  {
        //Arrange
        var signInData = new SignInDTO("teste@email.com", "Teste123@", "WEB", "teste");
        var securityData = signInService.logIntoAccount(signInData);
        var user = userRepository.findByEmail(signInData.email()).orElseThrow();
        var twoFactor = twoFactorAuthRepository.findByUserAndType2FA(user, Type2FA.EMAIL).orElseThrow();
        twoFactor.setExpirationAt(LocalDateTime.now().minusMinutes(10));
        twoFactorAuthRepository.save(twoFactor);
        String json = new ObjectMapper()
                .writeValueAsString(new RequestValidSignIn2faDTO(twoFactor.getSecret(), "EMAIL", securityData.securityUserToken2fa(),"WEB", "teste"));

        mockMvc.perform(put("/login/validation2fa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isFound())
                .andExpect(header().string("Location",
                        "http://localhost:8080/account/two-factor-auth/resend-code?code="+twoFactor.getSecret()));
        verify(emailService, times(2)).sendMail(any(), any(), any());
    }

    @Test
    @DisplayName("should return 400 Bad Request when a user validates the account with invalid code 2FA")
    void shouldReturn400WhenUserValidatesWithInvalidCode() throws Exception  {
        //Arrange
        var signInData = new SignInDTO("teste@email.com", "Teste123@", "WEB", "teste");
        var securityData = signInService.logIntoAccount(signInData);
        String json = new ObjectMapper()
                .writeValueAsString(new RequestValidSignIn2faDTO("invalidCode", "EMAIL", securityData.securityUserToken2fa(),"WEB", "teste"));

        mockMvc.perform(put("/login/validation2fa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

}