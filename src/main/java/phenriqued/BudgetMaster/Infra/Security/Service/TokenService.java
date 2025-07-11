package phenriqued.BudgetMaster.Infra.Security.Service;


import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import phenriqued.BudgetMaster.DTOs.Token.TokenDTO;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BudgetMasterSecurityException;
import phenriqued.BudgetMaster.Infra.Security.Token.SecurityUserToken;
import phenriqued.BudgetMaster.Infra.Security.Token.TokenType;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Repositories.SecurityData.SecurityUserTokenRepository;

@Service
@AllArgsConstructor
public class TokenService {

    private final JWTService jwtService;
    private final SecurityUserTokenService securityUserTokenService;
    private final SecurityUserTokenRepository tokenRepository;

    public TokenDTO generatedRefreshTokenAndTokenJWT(UserDetailsImpl user, TokenType tokenType, String deviceIdentifier) {
        var tokenJWT = jwtService.generatedTokenJWT(user);
        var refreshToken = securityUserTokenService.generatedRefreshToken(user, tokenType, deviceIdentifier);
        return new TokenDTO(tokenJWT, refreshToken);
    }

    public void tokenValidations(String tokenJWT, String securityUserToken){
        try{
            verifyToken(securityUserToken);
            jwtService.tokenJWTValidation(tokenJWT);
        }catch (BudgetMasterSecurityException e){
            throw new BudgetMasterSecurityException("[ERROR] "+e.getMessage());
        }
    }

    public void verifyToken(String code) throws BudgetMasterSecurityException{
        securityUserTokenService.tokenValidation(code);
    }

    public SecurityUserToken findByToken(String token){
        return tokenRepository.findByToken(token)
                .orElseThrow(() -> new BudgetMasterSecurityException("[ERROR] invalid code or could not find token!"));
    }

    public void deleteToken(SecurityUserToken token){
        if(!tokenRepository.existsById(token.getId())) throw new BudgetMasterSecurityException("invalid token!");
        tokenRepository.delete(token);
    }



}
