package phenriqued.BudgetMaster.Services.LoginService;

import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import phenriqued.BudgetMaster.DTOs.Login.SignInDTO;
import phenriqued.BudgetMaster.DTOs.Token.TokenDTO;
import phenriqued.BudgetMaster.Infra.Security.Service.TokenService;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;

@Service
@AllArgsConstructor
public class SignInService{

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;


    public TokenDTO logIntoAccount(SignInDTO signInDTO){
        var authenticationToken = new UsernamePasswordAuthenticationToken(signInDTO.email(), signInDTO.password());
        var userAuthenticated = authenticationManager.authenticate(authenticationToken);

        String token = tokenService.generatedTokenJWT((UserDetailsImpl) userAuthenticated.getPrincipal());
        return new TokenDTO(token);
    }


}
