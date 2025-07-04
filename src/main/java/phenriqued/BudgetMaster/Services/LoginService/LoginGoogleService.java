package phenriqued.BudgetMaster.Services.LoginService;

import com.auth0.jwt.JWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import phenriqued.BudgetMaster.DTOs.Login.LoginGoogleRequestDTO;
import phenriqued.BudgetMaster.DTOs.Login.RegisterUserDTO;
import phenriqued.BudgetMaster.DTOs.Token.TokenDTO;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BudgetMasterSecurityException;
import phenriqued.BudgetMaster.Infra.Security.Service.TokenService;
import phenriqued.BudgetMaster.Infra.Security.Token.TokenType;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Models.UserEntity.Role.Role;
import phenriqued.BudgetMaster.Models.UserEntity.Role.RoleName;
import phenriqued.BudgetMaster.Models.UserEntity.User;
import phenriqued.BudgetMaster.Repositories.RoleRepository.RoleRepository;
import phenriqued.BudgetMaster.Repositories.UserRepository.UserRepository;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class LoginGoogleService {

    @Value("${CLIENT_ID_GOOGLE}")
    private String clientId;
    @Value("${CLIENT_SECRET_GOOGLE}")
    private String clientSecret;
    private final String redirectUri = "http://localhost:8080/login/google/authorized";
    private final RestClient restClient;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TokenService tokenService;
    private final PasswordEncoder encoder;

    public LoginGoogleService(RestClient.Builder restClient, UserRepository userRepository, RoleRepository roleRepository, TokenService tokenService, PasswordEncoder encoder) {
        this.restClient = restClient.build();
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.tokenService = tokenService;
        this.encoder = encoder;
    }


    public String gerarUrl() {
        return "https://accounts.google.com/o/oauth2/v2/auth"+
                "?client_id="+clientId +
                "&redirect_uri="+redirectUri +
                "&scope=https://www.googleapis.com/auth/userinfo.email+https://www.googleapis.com/auth/userinfo.profile" +
                "&response_type=code";
    }

    @Transactional
    public TokenDTO loginGoogle(LoginGoogleRequestDTO loginGoogleDTO) {
        var userDataFromGoogle = getUserData(loginGoogleDTO);
        User user = userRepository.findByEmail(userDataFromGoogle.email()).orElse(null);
        UserDetailsImpl userDetails;
        if(Objects.isNull(user)){
            user = new User(userDataFromGoogle, encoder.encode(userDataFromGoogle.password()), getRole());
            userDetails = new UserDetailsImpl(userRepository.save(user));
        }else {
            userDetails = new UserDetailsImpl(user);
        }
        return tokenService.generatedTokens(userDetails, TokenType.valueOf(loginGoogleDTO.tokenType()), loginGoogleDTO.identifier());
    }

    public String getToken(String code){
        var responseCode = restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Map.of("code", code,"client_id", clientId, "client_secret", clientSecret,
                        "redirect_uri", redirectUri, "grant_type","authorization_code"))
                .retrieve()
                .body(Map.class);
        if(Objects.isNull(responseCode)) throw new NullPointerException("[INTERNAL ERROR]: communication with google is fail! Object is null.");
        return responseCode.get("id_token").toString();
    }

    private RegisterUserDTO getUserData(LoginGoogleRequestDTO loginGoogleDTO){
        var jwtDecoder = JWT.decode(loginGoogleDTO.token());
        return new RegisterUserDTO(jwtDecoder.getClaim("name").asString(), jwtDecoder.getClaim("email").asString(), UUID.randomUUID().toString());
    }
    private Role getRole(){
        return roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new BudgetMasterSecurityException("[INTERNAL ERROR] could not find role."));
    }

}
