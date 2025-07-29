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
import phenriqued.BudgetMaster.DTOs.Security.Token.RequestRefreshTokenDTO;
import phenriqued.BudgetMaster.DTOs.Security.TwoFactorAuth.Request2faActiveDTO;
import phenriqued.BudgetMaster.Infra.Email.EmailService;
import phenriqued.BudgetMaster.Models.Security.TwoFactorAuthentication.Type2FA;
import phenriqued.BudgetMaster.Models.UserEntity.Role.RoleName;
import phenriqued.BudgetMaster.Models.UserEntity.User;
import phenriqued.BudgetMaster.Repositories.RoleRepository.RoleRepository;
import phenriqued.BudgetMaster.Repositories.Security.TwoFactorAuthRepository;
import phenriqued.BudgetMaster.Repositories.UserRepository.UserRepository;
import phenriqued.BudgetMaster.Services.LoginService.SignInService;
import phenriqued.BudgetMaster.Services.Security.TwoFactorAuthServices.TwoFactorAuthService;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class SignInControllerTest {

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
    }
    @AfterEach
    void afterEach(){
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("should return 200 ok when user logs in and does not have 2FA active")
    void shouldReturnOKWhenUserLogIn() throws Exception {
        var signInData = new SignInDTO("teste@email.com", "Teste123@", "WEB", "teste");
        String json = new ObjectMapper().writeValueAsString(signInData);

        mockMvc.perform(post("/login/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(jsonPath("$.twoFactorAuthenticationMessage", nullValue()))
                .andExpect(jsonPath("$.securityUserToken2fa", nullValue()));
    }
    @Test
    @DisplayName("should return 200 ok and 2FA message when user logs in and have 2FA active")
    void shouldReturnOKAnd2faMessageWhenUserLogInAndHas2faActive() throws Exception {
        //Arrange
        twoFactorAuthService.createTwoFactorAuth(new Request2faActiveDTO("EMAIL"), "teste@email.com");
        var user = userRepository.findByEmail(userData.email()).orElseThrow();
        var twoFactor = twoFactorAuthRepository.findByUserAndType2FA(user, Type2FA.EMAIL).orElseThrow();
        twoFactor.setActive();
        twoFactorAuthRepository.save(twoFactor);
        var signInData = new SignInDTO("teste@email.com", "Teste123@", "WEB", "teste");
        String json = new ObjectMapper().writeValueAsString(signInData);
        //Action & Assert
        mockMvc.perform(post("/login/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", nullValue()))
                .andExpect(jsonPath("$.refreshToken", nullValue()))
                .andExpect(jsonPath("$.twoFactorAuthenticationMessage").isString())
                .andExpect(jsonPath("$.securityUserToken2fa").isString());
        verify(emailService, times(2)).sendMail(any(), any(), any());
    }
    @Test
    @DisplayName("should return 403 Forbidden when user logs with incorrect data")
    void shouldReturn403WhenUserLogInWithIncorrectData() throws Exception {
        var signInData = new SignInDTO("teste@email.com", "Testing12345!", "WEB", "teste");
        String json = new ObjectMapper().writeValueAsString(signInData);
        mockMvc.perform(post("/login/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("should return 200 ok when there is a valid refresh token")
    void shouldReturn200OKWhenRefreshTokenIsValid()  throws Exception{
        var token = signInService.logIntoAccount(new SignInDTO("teste@email.com", "Teste123@", "WEB", "teste"));
        String json = new ObjectMapper().writeValueAsString(new RequestRefreshTokenDTO(token.refreshToken(), "WEB", "teste"));

        mockMvc.perform(post("/login/update-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.refreshToken").isString());
    }
    @Test
    @DisplayName("should return 403 Forbidden when there is a invalid refresh token")
    void shouldReturnForbiddenWhenRefreshTokenIsInvalid()  throws Exception{

        String json = new ObjectMapper().writeValueAsString(new RequestRefreshTokenDTO("refreshTokenInvalid", "WEB", "teste"));

        mockMvc.perform(post("/login/update-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }
}