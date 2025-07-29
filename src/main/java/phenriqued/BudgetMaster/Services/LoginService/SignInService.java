package phenriqued.BudgetMaster.Services.LoginService;

import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import phenriqued.BudgetMaster.DTOs.Login.SignInDTO;
import phenriqued.BudgetMaster.DTOs.Security.Token.RequestRefreshTokenDTO;
import phenriqued.BudgetMaster.DTOs.Security.Token.TokenDTO;
import phenriqued.BudgetMaster.DTOs.Security.Token.TokenSignInDTO;
import phenriqued.BudgetMaster.DTOs.Security.TwoFactorAuth.RequestValidSignIn2faDTO;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Models.Security.Token.TokenType;
import phenriqued.BudgetMaster.Models.UserEntity.User;
import phenriqued.BudgetMaster.Services.Security.TokensService.TokenService;
import phenriqued.BudgetMaster.Services.Security.TwoFactorAuthServices.TwoFactorAuthService;
import phenriqued.BudgetMaster.Services.UserServices.UserService;

import java.util.Optional;

@Service
@AllArgsConstructor
public class SignInService{

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserService userService;
    private final TwoFactorAuthService twoFactorAuthService;

    private UserDetailsImpl authenticateUser(SignInDTO signInDTO){
        var authenticationToken = new UsernamePasswordAuthenticationToken(signInDTO.email(), signInDTO.password());
        Authentication userAuthentication = authenticationManager.authenticate(authenticationToken);
        return (UserDetailsImpl)  userAuthentication.getPrincipal();
    }
    private UserDetailsImpl handleDisableAccount(SignInDTO signInDTO){
        User user = userService.findUserByEmail(signInDTO.email());
        userService.activateAccount(user);
        return new UserDetailsImpl(user);
    }
    private Optional<TokenSignInDTO> handleTwoFactorAuthentication(User user, SignInDTO signInDTO) {
        if (user.isTwoFactorAuthEnabled()){
            String twoFactorAuth = twoFactorAuthService.generatedCodeTwoFactorAuth(signInDTO);
            String securityUserToken2fa = tokenService.generatedSecurityUserToken2FA(user);
            return Optional.of(new TokenSignInDTO(null, null, twoFactorAuth, securityUserToken2fa));
        }
        return Optional.empty();
    }

    public TokenSignInDTO logIntoAccount(SignInDTO signInDTO){
        User user;
        try {
            user = authenticateUser(signInDTO).getUser();
        }catch (DisabledException e){
            user = handleDisableAccount(signInDTO).getUser();
        }

        Optional<TokenSignInDTO> response2FA = handleTwoFactorAuthentication(user, signInDTO);
        if (response2FA.isPresent()) return response2FA.get();

        var token = tokenService.generatedRefreshTokenAndTokenJWT(new UserDetailsImpl(user), TokenType.valueOf(signInDTO.tokenType().toUpperCase()),
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
