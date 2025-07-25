package phenriqued.BudgetMaster.Services.LoginService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import phenriqued.BudgetMaster.DTOs.Login.SignInDTO;
import phenriqued.BudgetMaster.DTOs.Security.Token.RequestRefreshTokenDTO;
import phenriqued.BudgetMaster.DTOs.Security.Token.TokenSignInDTO;
import phenriqued.BudgetMaster.DTOs.Security.TwoFactorAuth.RequestValid2faDTO;
import phenriqued.BudgetMaster.DTOs.Security.Token.TokenDTO;
import phenriqued.BudgetMaster.DTOs.Security.TwoFactorAuth.RequestValidSignIn2faDTO;
import phenriqued.BudgetMaster.Services.Security.TokensService.TokenService;
import phenriqued.BudgetMaster.Models.Security.Token.TokenType;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Services.Security.TwoFactorAuthServices.TwoFactorAuthService;
import phenriqued.BudgetMaster.Services.UserServices.UserService;

@Service
@AllArgsConstructor
public class SignInService{

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserService userService;
    private final TwoFactorAuthService twoFactorAuthService;


    public TokenSignInDTO logIntoAccount(SignInDTO signInDTO){
        var authenticationToken = new UsernamePasswordAuthenticationToken(signInDTO.email(), signInDTO.password());
        Authentication userAuthenticated;
        UserDetailsImpl userDetails;
        try{
            userAuthenticated = authenticationManager.authenticate(authenticationToken);
            userDetails = (UserDetailsImpl) userAuthenticated.getPrincipal();
            var user = userDetails.getUser();
            if(user.isTwoFactorAuthEnabled()){
                String twoFactorAuth = twoFactorAuthService.generatedCodeTwoFactorAuth(signInDTO);
                String securityUserToken2fa = tokenService.generatedSecurityUserToken2FA(user, signInDTO.identifier());
                return new TokenSignInDTO(null, null, twoFactorAuth, securityUserToken2fa);
            }

        }catch (DisabledException e){
            userAuthenticated = authenticationManager.authenticate(authenticationToken);
            userDetails = (UserDetailsImpl) userAuthenticated.getPrincipal();
            var user = userDetails.getUser();
            userService.activateAccount(user);
            if(user.isTwoFactorAuthEnabled()){
                String twoFactorAuth = twoFactorAuthService.generatedCodeTwoFactorAuth(signInDTO);
                String securityUserToken2fa = tokenService.generatedSecurityUserToken2FA(user, signInDTO.identifier());;
                return new TokenSignInDTO(null, null, twoFactorAuth, securityUserToken2fa);
            }
        }
        var token = tokenService.generatedRefreshTokenAndTokenJWT((UserDetailsImpl) userAuthenticated.getPrincipal(), TokenType.valueOf(signInDTO.tokenType().toUpperCase()),
                signInDTO.identifier());
        return new TokenSignInDTO(token);
    }

    public TokenDTO validation2fa(RequestValidSignIn2faDTO code) {
        var securityUserToken2FA = tokenService.findByToken(code.securityUserToken2fa());
        var twoFactorAuth = twoFactorAuthService.validationAndActivationTwoFactorAuth(securityUserToken2FA.getUser().getEmail(),
                code.code(), code.type2fa());
        tokenService.deleteToken(securityUserToken2FA);
        return tokenService.generatedRefreshTokenAndTokenJWT(new UserDetailsImpl(twoFactorAuth.getUser()),
                TokenType.valueOf(code.tokenType().toUpperCase()), code.identifier());
    }

    public TokenDTO updateTokens(RequestRefreshTokenDTO refreshTokenDTO) {
        var userDetails = tokenService.findByToken(refreshTokenDTO.refreshToken()).getUser();
        tokenService.verifySecurityUserToken(refreshTokenDTO.refreshToken());
        return tokenService.generatedRefreshTokenAndTokenJWT(new UserDetailsImpl(userDetails), TokenType.valueOf(refreshTokenDTO.tokenType().toUpperCase()),
                refreshTokenDTO.identifier());
    }
}
