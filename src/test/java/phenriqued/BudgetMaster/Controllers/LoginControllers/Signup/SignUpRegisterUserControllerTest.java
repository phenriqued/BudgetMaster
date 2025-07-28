package phenriqued.BudgetMaster.Controllers.LoginControllers.Signup;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import phenriqued.BudgetMaster.Infra.Email.SecurityEmailService;
import phenriqued.BudgetMaster.Models.UserEntity.User;
import phenriqued.BudgetMaster.Repositories.RoleRepository.RoleRepository;
import phenriqued.BudgetMaster.Repositories.UserRepository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class SignUpRegisterUserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @MockitoBean
    private SecurityEmailService securityEmailService;

    @Test
    @DisplayName("a user should be created when the data is correct")
    void shouldReturn201WhenAllUserDatasIsCorrect() throws Exception {
        String json = """
                {
                "name":"teste",
                "email":"teste@email.com",
                "password":"Teste123@"
                }
                """;

        mockMvc.perform(post("/login/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/login/activate-user"))
                .andExpect(content().string("Conta criada. Verifique seu e-mail."));

        assertTrue(userRepository.findByEmail("teste@email.com").isPresent());
        verify(securityEmailService, times(1)).sendVerificationEmail(any(User.class));
    }
    @Test
    @DisplayName("a user should not be created when the password does not meet the requirements")
    void shouldReturn400WhenPasswordInvalid() throws Exception {
        String json = """
                {
                "name":"testeII",
                "email":"testeII@email.com",
                "password":"teste"
                }
                """;

        mockMvc.perform(post("/login/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].fieldError").value("password"))
                .andExpect(jsonPath("$[0].defaultMessage")
                        .value("Password must have at least one uppercase letter, one number, one special character, and be at least 6 characters long."));

        assertFalse(userRepository.findByEmail("testeII@email.com").isPresent());
        verify(securityEmailService, times(0)).sendVerificationEmail(any(User.class));
    }
    @Test
    @DisplayName("a user should not be created when the email does not meet the requirements")
    void shouldReturn400WhenEmailInvalid() throws Exception {
        String json = """
                {
                "name":"testeIII",
                "email":"testeIIIemailcom",
                "password":"Teste123@"
                }
                """;

        mockMvc.perform(post("/login/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].fieldError").value("email"))
                .andExpect(jsonPath("$[0].defaultMessage")
                        .value("must be a well-formed email address"));

        assertFalse(userRepository.findByEmail("testeIIIemailcom").isPresent());
        verify(securityEmailService, times(0)).sendVerificationEmail(any(User.class));
    }
}