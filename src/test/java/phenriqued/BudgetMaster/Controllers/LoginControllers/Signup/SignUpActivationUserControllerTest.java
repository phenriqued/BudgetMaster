package phenriqued.BudgetMaster.Controllers.LoginControllers.Signup;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import phenriqued.BudgetMaster.DTOs.Login.RegisterUserDTO;
import phenriqued.BudgetMaster.DTOs.Security.Token.RequestTokenDTO;
import phenriqued.BudgetMaster.Infra.Email.UserEmailService;
import phenriqued.BudgetMaster.Models.UserEntity.User;
import phenriqued.BudgetMaster.Repositories.Security.SecurityUserTokenRepository;
import phenriqued.BudgetMaster.Repositories.UserRepository.UserRepository;
import phenriqued.BudgetMaster.Services.LoginService.SignUpService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class SignUpActivationUserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SecurityUserTokenRepository tokenRepository;
    @Autowired
    private SignUpService signUpService;
    @MockitoBean
    private UserEmailService userEmailService;

    @AfterEach
    void afterEach(){
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("must activate a user that contains the correct OPEN ID token")
    void  shouldActivateUserSuccessfully() throws Exception {
        //arrange
        var data = new RegisterUserDTO("teste", "teste@email.com", "Teste123@");
        signUpService.registerUser(data);

        var user = userRepository.findByEmail(data.email()).orElseThrow(EntityNotFoundException::new);
        var tokenActiveUser = tokenRepository.findByIdentifierAndUser("internal-activation-user-"+user.getId(), user)
                .orElseThrow().getToken();
        String json = new ObjectMapper().writeValueAsString(new RequestTokenDTO("WEB", "teste"));

        //action and assert
        mockMvc.perform(put("/login/activate-user?code="+tokenActiveUser)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.refreshToken").isString());

        var userActivated = userRepository.findByEmail(data.email()).orElseThrow();
        assertTrue(userActivated.getIsActive());
        verify(userEmailService, times(1)).sendActivateAccount(user);
    }

    @Test
    @DisplayName("should not activate user when there is an invalid Open ID token")
    void shouldNotActivateUserWhenTokenIsInvalid() throws Exception {
        //arrange
        var data = new RegisterUserDTO("teste", "teste@email.com", "Teste123@");
        signUpService.registerUser(data);

        String json = new ObjectMapper().writeValueAsString(new RequestTokenDTO("WEB", "teste"));

        //action and assert
        mockMvc.perform(put("/login/activate-user?code=invalidToken123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("[ERROR] invalid code or could not find token!"));


        var userActivated = userRepository.findByEmail(data.email()).orElseThrow();
        assertFalse(userActivated.getIsActive());
        verify(userEmailService, never()).sendActivateAccount(any(User.class));
    }

    @Test
    @DisplayName("Should redirect with 302 when token has been exceeded for activation")
    void shouldNotActivateUserWhenTokenHasBeenExceeded() throws Exception {
        //arrange
        var data = new RegisterUserDTO("teste", "teste@email.com", "Teste123@");
        signUpService.registerUser(data);

        var user = userRepository.findByEmail(data.email()).orElseThrow(EntityNotFoundException::new);
        var tokenActiveUser = tokenRepository.findByIdentifierAndUser("internal-activation-user-"+user.getId(), user).orElseThrow();
        tokenActiveUser.setExpirationToken(LocalDateTime.now().minusMinutes(10));
        tokenRepository.save(tokenActiveUser);
        String json = new ObjectMapper().writeValueAsString(new RequestTokenDTO("WEB", "teste"));

        //action and assert
        mockMvc.perform(put("/login/activate-user?code="+tokenActiveUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "http://localhost:8080/login/resend-code?code="+tokenActiveUser.getToken()));


        var userActivated = userRepository.findByEmail(data.email()).orElseThrow();
        assertFalse(userActivated.getIsActive());
        verify(userEmailService, never()).sendActivateAccount(any(User.class));
    }

    @Test
    @DisplayName("should send a new email with a new valid code")
    void ShouldSendNewValidCode() throws Exception {
        var data = new RegisterUserDTO("teste", "teste@email.com", "Teste123@");
        signUpService.registerUser(data);

        var user = userRepository.findByEmail(data.email()).orElseThrow(EntityNotFoundException::new);
        var tokenActiveUser = tokenRepository.findByIdentifierAndUser("internal-activation-user-"+user.getId(), user).orElseThrow();
        tokenActiveUser.setExpirationToken(LocalDateTime.now().minusMinutes(10));
        tokenRepository.save(tokenActiveUser);

        mockMvc.perform(get("/login/resend-code?code="+tokenActiveUser.getToken()))
                .andExpect(status().isOk());
    }
    @Test
    @DisplayName("should not send a new email with a new valid code when code is valid")
    void ShouldNotSendNewValidCodeWhenCodeIsValid() throws Exception {
        var data = new RegisterUserDTO("teste", "teste@email.com", "Teste123@");
        signUpService.registerUser(data);

        var user = userRepository.findByEmail(data.email()).orElseThrow(EntityNotFoundException::new);
        var tokenActiveUser = tokenRepository.findByIdentifierAndUser("internal-activation-user-"+user.getId(), user).orElseThrow();

        mockMvc.perform(get("/login/resend-code?code="+tokenActiveUser.getToken()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Token está valido!"));
    }
}
