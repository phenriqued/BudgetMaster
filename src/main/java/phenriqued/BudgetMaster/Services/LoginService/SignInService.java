package phenriqued.BudgetMaster.Services.LoginService;

import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import phenriqued.BudgetMaster.DTOs.Login.SignInDTO;
import phenriqued.BudgetMaster.DTOs.Token.TokenDTO;
import phenriqued.BudgetMaster.Infra.Security.Service.TokenService;
import phenriqued.BudgetMaster.Infra.Security.Token.TokenType;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Services.UserServices.UserService;

@Service
@AllArgsConstructor
public class SignInService{

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserService userService;


    public TokenDTO logIntoAccount(SignInDTO signInDTO){
        var authenticationToken = new UsernamePasswordAuthenticationToken(signInDTO.email(), signInDTO.password());
        Authentication userAuthenticated;
        try{
            userAuthenticated = authenticationManager.authenticate(authenticationToken);
        }catch (DisabledException e){
            userService.activateAccount(signInDTO.email());
            userAuthenticated = authenticationManager.authenticate(authenticationToken);
        }
        return tokenService.generatedRefreshTokenAndTokenJWT((UserDetailsImpl) userAuthenticated.getPrincipal(), TokenType.valueOf(signInDTO.tokenType().toUpperCase()),
                signInDTO.identifier());
    }


}
